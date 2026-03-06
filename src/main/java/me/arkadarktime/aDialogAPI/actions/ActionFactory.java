package me.arkadarktime.aDialogAPI.actions;

import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.actions.impl.*;
import me.arkadarktime.aDialogAPI.models.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class ActionFactory {

    private final ADialogAPI plugin;

    public ActionFactory(ADialogAPI plugin) {
        this.plugin = plugin;
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
            plugin.getLogger().warning("[ActionFactory] Missing 'type' on button '" + buttonKey
                    + "' in '" + dialogId + "'");
            return Optional.empty();
        }

        return switch (typeObj.toString().toLowerCase()) {
            case "command", "player_command", "player", "run_command" ->
                    requireText(entry, "command", buttonKey, dialogId).map(RunCommandAction::new);

            case "console", "console_command" ->
                    requireText(entry, "command", buttonKey, dialogId).map(ConsoleCommandAction::new);

            case "message", "send_message" ->
                    requireText(entry, "content", buttonKey, dialogId).map(MessageAction::new);

            case "broadcast", "broadcast_message" ->
                    requireText(entry, "content", buttonKey, dialogId).map(BroadcastAction::new);

            case "show_dialog", "open_dialog" ->
                    requireText(entry, "dialog", buttonKey, dialogId).map(ShowDialogAction::new);

            case "give_item", "giveitem", "give", "item" -> parseItemAction(entry, buttonKey, dialogId, false);

            case "take_item", "takeitem", "remove_item" -> parseItemAction(entry, buttonKey, dialogId, true);

            case "title", "send_title" -> parseTitleAction(entry);
            case "sound", "play_sound" -> parseSoundAction(entry, buttonKey, dialogId);
            case "effect", "potion_effect", "give_effect" -> parsePotionAction(entry, buttonKey, dialogId);
            case "xp", "give_xp", "experience" -> parseXpAction(entry, buttonKey, dialogId);
            case "close" -> Optional.of(new CloseAction());

            default -> {
                plugin.getLogger().warning("[ActionFactory] Unknown action type '" + typeObj + "' on button '" + buttonKey
                        + "' in '" + dialogId + "'");
                yield Optional.empty();
            }
        };
    }

    private Optional<ButtonAction> parseItemAction(Map<?, ?> entry, String buttonKey, String dialogId, boolean take) {
        Object matObj = entry.get("material");
        if (matObj == null || matObj.toString().isBlank()) {
            plugin.getLogger().warning("[ActionFactory] Item action missing 'material' on button '" + buttonKey
                    + "' in '" + dialogId + "'");
            return Optional.empty();
        }

        Material material = Material.matchMaterial(matObj.toString());
        if (material == null) {
            plugin.getLogger().warning("[ActionFactory] Unknown material '" + matObj
                    + "' on button '" + buttonKey
                    + "' in '" + dialogId + "'");
            return Optional.empty();
        }

        int count = Utils.parseIntSafe(entry.get("count"), 1);
        return Optional.of(take ? new TakeItemAction(material, count) : new GiveItemAction(material, count));
    }

    private Optional<ButtonAction> parseTitleAction(Map<?, ?> entry) {
        String title = Utils.resolveString(entry, "title", "value");
        String subtitle = entry.containsKey("subtitle") ? entry.get("subtitle").toString() : "";
        int fadeIn = Utils.parseIntSafe(entry.get("fade_in"), 10);
        int stay = Utils.parseIntSafe(entry.get("stay"), 70);
        int fadeOut = Utils.parseIntSafe(entry.get("fade_out"), 20);
        return Optional.of(new TitleAction(title != null ? title : "", subtitle, fadeIn, stay, fadeOut));
    }

    private Optional<ButtonAction> parseSoundAction(Map<?, ?> entry, String buttonKey, String dialogId) {
        String sound = Utils.resolveString(entry, "sound", "value");
        if (sound == null || sound.isBlank()) {
            plugin.getLogger().warning("[ActionFactory] 'sound' missing on button '" + buttonKey
                    + "' in '" + dialogId + "'");
            return Optional.empty();
        }
        float volume = Utils.parseFloatSafe(entry.get("volume"), 1.0f);
        float pitch = Utils.parseFloatSafe(entry.get("pitch"), 1.0f);
        return Optional.of(new SoundAction(sound, volume, pitch));
    }

    private Optional<ButtonAction> parsePotionAction(Map<?, ?> entry, String buttonKey, String dialogId) {
        String effectName = Utils.resolveString(entry, "effect", "value");
        if (effectName == null || effectName.isBlank()) {
            plugin.getLogger().warning("[ActionFactory] 'effect' missing on button '" + buttonKey
                    + "' in '" + dialogId + "'");
            return Optional.empty();
        }

        @SuppressWarnings("deprecation")
        PotionEffectType effectType = PotionEffectType.getByName(effectName.toUpperCase());
        if (effectType == null) {
            plugin.getLogger().warning("[ActionFactory] Unknown potion effect '" + effectName
                    + "' on button '" + buttonKey
                    + "' in '" + dialogId + "'");
            return Optional.empty();
        }

        int duration = Utils.parseIntSafe(entry.get("duration"), 200);
        int amplifier = Utils.parseIntSafe(entry.get("amplifier"), 0);
        return Optional.of(new PotionEffectAction(effectType, duration, amplifier));
    }

    private Optional<ButtonAction> parseXpAction(Map<?, ?> entry, String buttonKey, String dialogId) {
        Object amount = entry.containsKey("amount") ? entry.get("amount") : entry.get("value");
        if (amount == null || amount.toString().isBlank()) {
            plugin.getLogger().warning("[ActionFactory] 'amount' missing on button '"
                    + buttonKey + "' in '"
                    + dialogId + "'");
            return Optional.empty();
        }
        boolean levels = Utils.parseBoolSafe(entry.get("levels"), false);
        int xp = Utils.parseIntSafe(amount, 0);
        return Optional.of(new XpAction(xp, levels));
    }

    private Optional<String> requireText(Map<?, ?> entry, String primaryKey, String buttonKey, String dialogId) {
        String value = Utils.resolveString(entry, primaryKey, "value");
        if (value == null || value.isBlank()) {
            plugin.getLogger().warning("[ActionFactory] Missing '" + primaryKey
                    + "' on button '" + buttonKey
                    + "' in '" + dialogId + "'");
            return Optional.empty();
        }
        return Optional.of(value);
    }
}
