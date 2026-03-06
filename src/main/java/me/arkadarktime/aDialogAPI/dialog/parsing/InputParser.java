package me.arkadarktime.aDialogAPI.dialog.parsing;

import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.dialog.InputMeta;
import me.arkadarktime.aDialogAPI.dialog.ParsedInputs;
import me.arkadarktime.aDialogAPI.util.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class InputParser {

    private final ADialogAPI plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public InputParser(ADialogAPI plugin) {
        this.plugin = plugin;
    }

    List<DialogInput> parseInputs(String id, FileConfiguration config) {
        List<DialogInput> result = new ArrayList<>();
        for (Map<?, ?> entry : config.getMapList("inputs")) {
            parseInput(id, entry).ifPresent(result::add);
        }
        return result;
    }

    ParsedInputs parseDefinitions(FileConfiguration config) {
        Map<String, String> defaults = new LinkedHashMap<>();
        Map<String, InputMeta> metas = new LinkedHashMap<>();

        for (Map<?, ?> entry : config.getMapList("inputs")) {
            Object keyObj = entry.get("key");
            Object typeObj = entry.get("type");
            if (keyObj == null || typeObj == null) continue;

            String key = keyObj.toString();
            String type = typeObj.toString().toLowerCase();

            switch (type) {
                case "text" -> {
                    String def = entry.containsKey("initial_value") ? entry.get("initial_value").toString() : "";
                    defaults.put(key, def);
                    metas.put(key, new InputMeta(type, def));
                }
                case "bool" -> {
                    boolean checked = Utils.parseBoolSafe(entry.get("initial_value"), false);
                    String onTrue = entry.containsKey("on_true") ? entry.get("on_true").toString() : "true";
                    String onFalse = entry.containsKey("on_false") ? entry.get("on_false").toString() : "false";
                    String def = checked ? onTrue : onFalse;
                    defaults.put(key, def);
                    metas.put(key, new InputMeta(type, onTrue, onFalse, def));
                }
                case "number_range" -> {
                    String def = entry.containsKey("initial_value") ? entry.get("initial_value").toString()
                            : entry.containsKey("min") ? entry.get("min").toString() : "0";
                    defaults.put(key, def);
                    metas.put(key, new InputMeta(type, def));
                }
                case "single_option" -> {
                    String def = "";
                    Object opts = entry.get("options");
                    if (opts instanceof List<?> list && !list.isEmpty()) {
                        Object first = list.getFirst();
                        def = (first instanceof Map<?, ?> m && m.containsKey("id"))
                                ? m.get("id").toString() : first.toString();
                    }
                    defaults.put(key, def);
                    metas.put(key, new InputMeta(type, def));
                }
                default -> {
                    defaults.put(key, "");
                    metas.put(key, new InputMeta(type, ""));
                }
            }
        }
        return new ParsedInputs(defaults, metas);
    }

    private Optional<DialogInput> parseInput(String id, Map<?, ?> entry) {
        Object typeObj = entry.get("type");
        Object keyObj = entry.get("key");
        if (typeObj == null || keyObj == null) {
            plugin.getLogger().warning("[InputParser] Input missing 'type' or 'key' in '" + id + "', skipping.");
            return Optional.empty();
        }

        String type = typeObj.toString().toLowerCase();
        String key = keyObj.toString();
        Component label = entry.containsKey("label")
                ? mm.deserialize(entry.get("label").toString())
                : Component.text(key);
        int width = Utils.parseIntSafe(entry.get("width"), 200);
        boolean labeled = Utils.parseBoolSafe(entry.get("labeled"), true);

        try {
            DialogInput input = switch (type) {
                case "text" -> parseText(key, width, label, labeled, entry);
                case "bool" -> parseBool(key, label, entry);
                case "number_range" -> parseNumberRange(key, width, label, entry, id);
                case "single_option" -> parseSingleOption(key, width, label, labeled, entry, id);
                default -> {
                    plugin.getLogger().warning("[InputParser] Unknown input type '" + type + "' in '" + id + "'");
                    yield null;
                }
            };
            return Optional.ofNullable(input);
        } catch ( Exception e ) {
            plugin.getLogger().warning("[InputParser] Failed to parse input '" + key + "' in '" + id + "': " + e.getMessage());
            return Optional.empty();
        }
    }

    private DialogInput parseText(String key, int width, Component label, boolean labeled, Map<?, ?> entry) {
        String value = entry.containsKey("initial_value") ? entry.get("initial_value").toString() : "";
        int maxLength = Utils.parseIntSafe(entry.get("max_length"), 32);
        // TODO: Make multilineOptions configurable
        return DialogInput.text(key, width, label, labeled, value, maxLength, null);
    }

    private DialogInput parseBool(String key, Component label, Map<?, ?> entry) {
        boolean value = Utils.parseBoolSafe(entry.get("initial_value"), false);
        String onTrue = entry.containsKey("on_true") ? entry.get("on_true").toString() : "true";
        String onFalse = entry.containsKey("on_false") ? entry.get("on_false").toString() : "false";
        return DialogInput.bool(key, label, value, onTrue, onFalse);
    }

    private DialogInput parseNumberRange(String key, int width, Component label, Map<?, ?> entry, String id) {
        Object minObj = entry.get("min");
        Object maxObj = entry.get("max");
        if (minObj == null || maxObj == null) {
            plugin.getLogger().warning("[InputParser] number_range '" + key + "' in '" + id + "' missing 'min'/'max', skipping.");
            return null;
        }
        float min = Float.parseFloat(minObj.toString());
        float max = Float.parseFloat(maxObj.toString());
        Float value = entry.containsKey("initial_value") ? Float.parseFloat(entry.get("initial_value").toString()) : null;
        String suffix = entry.containsKey("suffix") ? entry.get("suffix").toString() : "%s: %s";
        float step = entry.containsKey("step") ? Float.parseFloat(entry.get("step").toString()) : 1.0f;
        return DialogInput.numberRange(key, width, label, suffix, min, max, value, step);
    }

    private DialogInput parseSingleOption(String key, int width, Component label, boolean labeled, Map<?, ?> entry, String id) {
        Object optionsObj = entry.get("options");
        if (!(optionsObj instanceof List<?> rawOptions) || rawOptions.isEmpty()) {
            plugin.getLogger().warning("[InputParser] single_option '" + key + "' in '" + id + "' has no 'options', skipping.");
            return null;
        }

        List<SingleOptionDialogInput.OptionEntry> options = rawOptions.stream()
                .map(this::buildOptionEntry)
                .toList();

        return DialogInput.singleOption(key, width, options, label, labeled);
    }

    private SingleOptionDialogInput.OptionEntry buildOptionEntry(Object raw) {
        if (raw instanceof Map<?, ?> map) {
            String optId = map.containsKey("id") ? map.get("id").toString() : raw.toString();
            String optLabel = map.containsKey("label") ? map.get("label").toString() : optId;
            return SingleOptionDialogInput.OptionEntry.create(optId, mm.deserialize(optLabel), false);
        }
        String optId = raw.toString();
        return SingleOptionDialogInput.OptionEntry.create(optId, Component.text(optId), false);
    }
}
