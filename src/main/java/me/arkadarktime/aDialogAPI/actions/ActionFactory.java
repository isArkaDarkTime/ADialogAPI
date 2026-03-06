package me.arkadarktime.aDialogAPI.actions;

import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.actions.impl.*;
import me.arkadarktime.aDialogAPI.util.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.function.Function;

public class ActionFactory {

    private final ADialogAPI plugin;
    Map<String, Function<ConfigNode, Optional<ButtonAction>>> registry = new LinkedHashMap<>();

    public ActionFactory(ADialogAPI plugin) {
        this.plugin = plugin;
        this.registry = buildRegistry();
    }

    public Map<String, List<ButtonAction>> parseAll(String dialogId, FileConfiguration config) {
        ConfigurationSection actionsSection = config.getConfigurationSection("actions");
        if (actionsSection == null) return Map.of();

        Map<String, List<ButtonAction>> result = new HashMap<>();

        for (String buttonKey : actionsSection.getKeys(false)) {
            ConfigurationSection buttonSection = actionsSection.getConfigurationSection(buttonKey);
            if (buttonSection == null) continue;

            List<ButtonAction> list = buttonSection.getMapList("after_actions").stream()
                    .map(entry -> create(entry, buttonKey, dialogId))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();

            if (!list.isEmpty()) result.put(buttonKey.toLowerCase(), list);
        }

        return Collections.unmodifiableMap(result);
    }

    public Optional<ButtonAction> create(Map<?, ?> entry, String buttonKey, String dialogId) {
        Object typeObj = entry.get("type");
        if (typeObj == null) {
            warn("Missing 'type' on button '" + buttonKey + "' in '" + dialogId + "'");
            return Optional.empty();
        }

        String type = typeObj.toString().toLowerCase();
        Function<ConfigNode, Optional<ButtonAction>> parser = registry.get(type);

        if (parser == null) {
            warn("Unknown action type '" + type + "' on button " + buttonKey + "' in '" + dialogId + "'");
            return Optional.empty();
        }

        return parser.apply(new ConfigNode(entry, buttonKey, dialogId));
    }

    private Map<String, Function<ConfigNode, Optional<ButtonAction>>> buildRegistry() {
        Map<String, Function<ConfigNode, Optional<ButtonAction>>> r = new LinkedHashMap<>();

        register(r, n -> requireText(n, "command").map(RunCommandAction::new),
                "command", "run_command", "player_command", "player");

        register(r, n -> requireText(n, "command").map(ConsoleCommandAction::new),
                "console", "console_command");

        register(r, n -> requireText(n, "content").map(MessageAction::new),
                "message", "send_message");

        register(r, n -> requireText(n, "content").map(BroadcastAction::new),
                "broadcast", "broadcast_message");

        register(r, n -> requireText(n, "dialog").map(ShowDialogAction::new),
                "show_dialog", "open_dialog");

        register(r, n -> parseItemAction(n, false), "give_item", "giveitem", "give", "item");
        register(r, n -> parseItemAction(n, true), "take_item", "takeitem", "remove_item");
        register(r, this::parseTitleAction, "title", "send_title");
        register(r, this::parseSoundAction, "sound", "play_sound");
        register(r, this::parsePotionAction, "effect", "potion_effect", "give_effect");
        register(r, this::parseXpAction, "xp", "give_xp", "experience");
        register(r, n -> Optional.of(new CloseAction()), "close");

        return Collections.unmodifiableMap(r);
    }

    private void register(Map<String, Function<ConfigNode, Optional<ButtonAction>>> r,
                          Function<ConfigNode, Optional<ButtonAction>> parser,
                          String... aliases) {
        for (String alias : aliases) r.put(alias, parser);
    }

    // Parsers

    private Optional<ButtonAction> parseItemAction(ConfigNode configNode, boolean take) {
        Object matObj = configNode.entry().get("material");
        if (matObj == null || matObj.toString().isBlank()) {
            warn("Item action missing 'material' on button '" + configNode.buttonKey() + "' in '" + configNode.dialogId() + "'");
            return Optional.empty();
        }

        Material material = Material.matchMaterial(matObj.toString());
        if (material == null) {
            warn("Unknown material '" + matObj + "' on button '" + configNode.buttonKey() + "' in '" + configNode.dialogId() + "'");
            return Optional.empty();
        }

        int count = Utils.parseIntSafe(configNode.entry().get("count"), 1);
        return Optional.of(take ? new TakeItemAction(material, count) : new GiveItemAction(material, count));
    }

    private Optional<ButtonAction> parseTitleAction(ConfigNode configNode) {
        String title = Utils.resolveString(configNode.entry(), "title", "value");
        String subtitle = configNode.entry().containsKey("subtitle") ? configNode.entry().get("subtitle").toString() : "";
        int fadeIn = Utils.parseIntSafe(configNode.entry().get("fade_in"), 10);
        int stay = Utils.parseIntSafe(configNode.entry().get("stay"), 70);
        int fadeOut = Utils.parseIntSafe(configNode.entry().get("fade_out"), 20);
        return Optional.of(new TitleAction(title != null ? title : "", subtitle, fadeIn, stay, fadeOut));
    }

    private Optional<ButtonAction> parseSoundAction(ConfigNode configNode) {
        String sound = Utils.resolveString(configNode.entry(), "sound", "value");
        if (sound == null || sound.isBlank()) {
            warn("'sound' missing on button '" + configNode.buttonKey() + "' in '" + configNode.dialogId() + "'");
            return Optional.empty();
        }

        float volume = Utils.parseFloatSafe(configNode.entry().get("volume"), 1.0f);
        float pitch = Utils.parseFloatSafe(configNode.entry().get("pitch"), 1.0f);
        return Optional.of(new SoundAction(sound, volume, pitch));
    }

    private Optional<ButtonAction> parsePotionAction(ConfigNode configNode) {
        String effectName = Utils.resolveString(configNode.entry(), "effect", "value");
        if (effectName == null || effectName.isBlank()) {
            warn("'effect' missing on button '" + configNode.buttonKey() + "' in '" + configNode.dialogId() + "'");
            return Optional.empty();
        }

        @SuppressWarnings("deprecation")
        PotionEffectType effectType = PotionEffectType.getByName(effectName.toUpperCase());
        if (effectType == null) {
            warn("Unknown potion effect '" + effectName + "' on button '" + configNode.buttonKey() + "' in '" + configNode.dialogId() + "'");
            return Optional.empty();
        }

        int duration = Utils.parseIntSafe(configNode.entry().get("duration"), 200);
        int amplifier = Utils.parseIntSafe(configNode.entry().get("amplifier"), 0);
        return Optional.of(new PotionEffectAction(effectType, duration, amplifier));
    }

    private Optional<ButtonAction> parseXpAction(ConfigNode configNode) {
        Object amount = configNode.entry().containsKey("amount") ? configNode.entry().get("amount") : configNode.entry().get("value");
        if (amount == null || amount.toString().isBlank()) {
            warn("'amount' missing on button '" + configNode.buttonKey() + "' in '" + configNode.dialogId() + "'");
            return Optional.empty();
        }
        boolean levels = Utils.parseBoolSafe(configNode.entry().get("levels"), false);
        return Optional.of(new XpAction(Utils.parseIntSafe(amount, 0), levels));
    }

    // Helpers

    private Optional<String> requireText(ConfigNode configNode, String primaryKey) {
        String value = Utils.resolveString(configNode.entry(), primaryKey, "value");
        if (value == null || value.isBlank()) {
            warn("Missing '" + primaryKey + "' on button '" + configNode.buttonKey() + "' in '" + configNode.dialogId() + "'");
            return Optional.empty();
        }
        return Optional.of(value);
    }

    private void warn(String message) {
        plugin.getLogger().warning("[ActionFactory] " + message);
    }

    // ConfigNode

    record ConfigNode(Map<?, ?> entry, String buttonKey, String dialogId) {
    }
}
