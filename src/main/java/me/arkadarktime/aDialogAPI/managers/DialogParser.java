package me.arkadarktime.aDialogAPI.managers;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.actions.ActionFactory;
import me.arkadarktime.aDialogAPI.actions.ButtonAction;
import me.arkadarktime.aDialogAPI.models.DialogMeta;
import me.arkadarktime.aDialogAPI.models.InputMeta;
import me.arkadarktime.aDialogAPI.models.ParsedInputs;
import me.arkadarktime.aDialogAPI.models.Utils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class DialogParser {

    public static final String KEY_NAMESPACE = "adialogapi";
    public static final String KEY_PREFIX = "dialog/";

    private final ADialogAPI plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private final BodyParser bodyParser;
    private final InputParser inputParser;
    private final ActionFactory actionFactory;

    public DialogParser(ADialogAPI plugin) {
        this.plugin = plugin;
        this.bodyParser = new BodyParser(plugin, mm);
        this.inputParser = new InputParser(plugin, mm);
        this.actionFactory = new ActionFactory(plugin);
    }

    // Public

    public Optional<Dialog> parseDialog(String id, FileConfiguration config, Map<String, List<ButtonAction>> actions) {
        try {
            Component title = mm.deserialize(config.getString("title", "<yellow>Dialog"));
            boolean canEscape = config.getBoolean("can_close_with_escape", true);
            String type = config.getString("type", "notice").toLowerCase();

            DialogBase base = DialogBase.builder(title)
                    .body(bodyParser.parse(id, config))
                    .inputs(inputParser.parseInputs(id, config))
                    .canCloseWithEscape(canEscape)
                    .build();

            return Optional.ofNullable(buildTypedDialog(id, type, base, config, actions));
        } catch ( Exception e ) {
            plugin.getLogger().severe("[DialogParser] Error parsing '" + id + "': " + e.getMessage());
            return Optional.empty();
        }
    }

    public DialogMeta parseMeta(String name, FileConfiguration config, String relativePath) {
        return new DialogMeta(
                name,
                config.getString("type", "notice").toLowerCase(),
                relativePath,
                config.getString("open_command", "").trim(),
                config.getString("open_permission", "").trim(),
                config.getBoolean("can_close_with_escape", true),
                config.getMapList("body").size(),
                Math.max(0L, config.getLong("cooldown", 0L))
        );
    }

    public ParsedInputs parseInputDefinitions(FileConfiguration config) {
        return inputParser.parseDefinitions(config);
    }

    public Map<String, List<ButtonAction>> parseButtonActions(String id, FileConfiguration config) {
        return actionFactory.parseAll(id, config);
    }

    // Dialog type builder

    private Dialog buildTypedDialog(String id, String type, DialogBase base, FileConfiguration config, Map<String, List<ButtonAction>> actions) {
        return switch (type) {
            case "notice" -> {
                ActionButton ok = buildActionButton(
                        config.getConfigurationSection("actions.ok"),
                        "Ok",
                        actions.getOrDefault("ok", List.of()),
                        id, "ok"
                );
                yield Dialog.create(builder -> builder.empty().base(base).type(DialogType.notice(ok)));
            }
            case "confirmation" -> {
                ActionButton yes = buildActionButton(
                        config.getConfigurationSection("actions.yes"),
                        "<green>Yes",
                        actions.getOrDefault("yes", List.of()),
                        id, "yes"
                );
                ActionButton no = buildActionButton(
                        config.getConfigurationSection("actions.no"),
                        "<red>No",
                        actions.getOrDefault("no", List.of()),
                        id, "no"
                );
                yield Dialog.create(builder -> builder.empty().base(base).type(DialogType.confirmation(yes, no)));
            }
            default -> {
                plugin.getLogger().warning("[DialogParser] Unknown dialog type '" + type + "' in: " + id);
                yield null;
            }
        };
    }

    private ActionButton buildActionButton(ConfigurationSection section, String defaultLabel, List<ButtonAction> actions, String dialogId, String buttonId) {
        String labelStr = section != null ? section.getString("label", defaultLabel) : defaultLabel;
        ActionButton.Builder builder = ActionButton.builder(mm.deserialize(labelStr));

        if (section != null) {
            String tooltip = section.getString("tooltip");
            if (tooltip != null) builder.tooltip(mm.deserialize(tooltip));
        }

        if (!actions.isEmpty()) {
            Key key = buttonKey(dialogId, buttonId);
            builder.action(DialogAction.customClick(key, null));
            plugin.debug("[DialogParser] Bound button '" + labelStr + "' to key: " + key.asString());
        } else {
            plugin.debug("[DialogParser] Button '" + labelStr + "' has no after_actions — no custom click bound.");
        }

        return builder.build();
    }

    @SuppressWarnings("PatternValidation")
    public static Key buttonKey(String dialogId, String buttonId) {
        return Key.key(KEY_NAMESPACE, KEY_PREFIX + dialogId + "/" + buttonId);
    }

    // Body parsing

    static final class BodyParser {
        private final ADialogAPI plugin;
        private final MiniMessage mm;

        BodyParser(ADialogAPI plugin, MiniMessage mm) {
            this.plugin = plugin;
            this.mm = mm;
        }

        List<DialogBody> parse(String id, FileConfiguration config) {
            List<DialogBody> result = new ArrayList<>();
            for (Map<?, ?> entry : config.getMapList("body")) {
                Object typeObj = entry.get("type");
                if (typeObj == null) continue;

                switch (typeObj.toString().toLowerCase()) {
                    case "text" -> result.add(parseText(entry));
                    case "item" -> parseItem(entry, id).ifPresent(result::add);
                    default -> plugin.getLogger().warning("[BodyParser] Unknown body type '" + typeObj + "' in: " + id);
                }
            }
            return result;
        }

        private DialogBody parseText(Map<?, ?> entry) {
            Object content = entry.get("content");
            return DialogBody.plainMessage(content != null ? mm.deserialize(content.toString()) : Component.empty());
        }

        private Optional<DialogBody> parseItem(Map<?, ?> entry, String dialogId) {
            Object materialObj = entry.get("material");
            if (materialObj == null) {
                plugin.getLogger().warning("[BodyParser] Missing 'material' in item body of '" + dialogId + "'");
                return Optional.empty();
            }

            Material material = Material.matchMaterial(materialObj.toString());
            if (material == null) {
                plugin.getLogger().warning("[BodyParser] Unknown material '" + materialObj + "' in '" + dialogId + "'");
                return Optional.empty();
            }

            int count = Utils.parseIntSafe(entry.get("count"), 1);
            return Optional.of(DialogBody.item(new ItemStack(material, Math.max(count, 1))).build());
        }
    }

    // Input parsing

    static final class InputParser {
        private final ADialogAPI plugin;
        private final MiniMessage mm;

        InputParser(ADialogAPI plugin, MiniMessage mm) {
            this.plugin = plugin;
            this.mm = mm;
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
}
