package me.arkadarktime.aDialogAPI.managers;

import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.models.ButtonAction;
import me.arkadarktime.aDialogAPI.models.Utils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class DialogActionExecutor {

    private final ADialogAPI plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public DialogActionExecutor(ADialogAPI plugin) {
        this.plugin = plugin;
    }

    // Public

    public void executeAll(Player player, List<ButtonAction> actions, Map<String, String> inputs) {
        for (ButtonAction action : actions) {
            execute(player, action, inputs);
        }
    }

    // Dispatcher

    private void execute(Player player, ButtonAction action, Map<String, String> inputs) {
        plugin.debug("[ActionExecutor] " + action.type()
                + " | value=\"" + action.value() + "\""
                + " | player=" + player.getName());

        switch (action.type()) {
            case RUN_COMMAND -> runCommand(player, action.value(), inputs);
            case CONSOLE_COMMAND -> consoleCommand(player, action.value(), inputs);
            case MESSAGE -> sendMessage(player, action.value(), inputs);
            case BROADCAST -> broadcast(player, action.value(), inputs);
            case SHOW_DIALOG -> showDialog(player, action.value());
            case GIVE_ITEM -> modifyInventory(player, action.value(), false);
            case TAKE_ITEM -> modifyInventory(player, action.value(), true);
            case TITLE -> showTitle(player, action.value(), inputs);
            case SOUND -> playSound(player, action.value());
            case POTION_EFFECT -> applyPotionEffect(player, action.value());
            case XP -> giveXp(player, action.value());
            case CLOSE -> player.closeInventory();
        }
    }

    // Implementations

    private void runCommand(Player player, String value, Map<String, String> inputs) {
        String cmd = applyPlaceholders(value, player, inputs);
        player.performCommand(cmd.startsWith("/") ? cmd.substring(1) : cmd);
    }

    private void consoleCommand(Player player, String value, Map<String, String> inputs) {
        String cmd = applyPlaceholders(value, player, inputs);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                cmd.startsWith("/") ? cmd.substring(1) : cmd);
    }

    private void sendMessage(Player player, String value, Map<String, String> inputs) {
        player.sendMessage(mm.deserialize(applyPlaceholders(value, player, inputs)));
    }

    private void broadcast(Player player, String value, Map<String, String> inputs) {
        Bukkit.broadcast(mm.deserialize(applyPlaceholders(value, player, inputs)));
    }

    private void showDialog(Player player, String dialogName) {
        boolean shown = plugin.getDialogManager().showDialog(player, dialogName);
        if (!shown && plugin.isDebugEnabled()) {
            plugin.getLogger().warning("[ActionExecutor] show_dialog: '" + dialogName + "' not found.");
        }
    }

    private void modifyInventory(Player player, String value, boolean take) {
        // MATERIAL:COUNT
        String[] parts = value.split(":", 2);

        Material material = Material.matchMaterial(parts[0]);
        if (material == null) {
            plugin.getLogger().warning("[ActionExecutor] Unknown material '" + parts[0] + "'");
            return;
        }

        int count = (parts.length == 2) ? Utils.parseIntSafe(parts[1], 1) : 1;

        if (take) {
            int remaining = count;
            ItemStack[] contents = player.getInventory().getContents();
            for (int i = 0; i < contents.length && remaining > 0; i++) {
                ItemStack slot = contents[i];
                if (slot == null || slot.getType() != material) continue;

                if (slot.getAmount() <= remaining) {
                    remaining -= slot.getAmount();
                    player.getInventory().setItem(i, null);
                } else {
                    slot.setAmount(slot.getAmount() - remaining);
                    remaining = 0;
                }
            }
            if (remaining > 0 && plugin.isDebugEnabled()) {
                plugin.getLogger().warning("[ActionExecutor] take_item: " + player.getName()
                        + " didn't have enough " + material + " (missing " + remaining + ")");
            }
        } else {
            Map<Integer, ItemStack> overflow = player.getInventory().addItem(new ItemStack(material, count));
            overflow.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        }
    }

    private void showTitle(Player player, String value, Map<String, String> inputs) {
        // title;subtitle;fadeIn;stay;fadeOut
        String[] parts = value.split(";", 5);
        String titleText = applyPlaceholders(parts.length > 0 ? parts[0] : "", player, inputs);
        String subtitleText = applyPlaceholders(parts.length > 1 ? parts[1] : "", player, inputs);
        int fadeIn = parts.length > 2 ? Utils.parseIntSafe(parts[2], 10) : 10;
        int stay = parts.length > 3 ? Utils.parseIntSafe(parts[3], 70) : 70;
        int fadeOut = parts.length > 4 ? Utils.parseIntSafe(parts[4], 20) : 20;

        player.showTitle(Title.title(
                mm.deserialize(titleText),
                mm.deserialize(subtitleText),
                Title.Times.times(
                        Duration.ofMillis(fadeIn * 50L),
                        Duration.ofMillis(stay * 50L),
                        Duration.ofMillis(fadeOut * 50L)
                )
        ));
    }

    @SuppressWarnings("PatternValidation")
    private void playSound(Player player, String value) {
        // minecraft:sound.key;volume;pitch
        String[] parts = value.split(";", 3);
        String soundName = parts[0].trim();
        float volume = parts.length > 1 ? Utils.parseFloatSafe(parts[1], 1.0f) : 1.0f;
        float pitch = parts.length > 2 ? Utils.parseFloatSafe(parts[2], 1.0f) : 1.0f;

        try {
            Key soundKey = Key.key(soundName.toLowerCase().replace("_", "."));
            player.playSound(
                    Sound.sound(soundKey, Sound.Source.MASTER, volume, pitch),
                    Sound.Emitter.self()
            );
        } catch ( Exception e ) {
            plugin.getLogger().warning("[ActionExecutor] Invalid sound '" + soundName + "': " + e.getMessage());
        }
    }

    private void applyPotionEffect(Player player, String value) {
        // EFFECT_TYPE;duration;amplifier
        String[] parts = value.split(";", 3);
        String effectName = parts[0].trim();
        int duration = parts.length > 1 ? Utils.parseIntSafe(parts[1], 200) : 200;
        int amplifier = parts.length > 2 ? Utils.parseIntSafe(parts[2], 0) : 0;

        PotionEffectType effectType = PotionEffectType.getByName(effectName);
        if (effectType == null) {
            plugin.getLogger().warning("[ActionExecutor] Unknown potion effect: '" + effectName + "'");
            return;
        }

        player.addPotionEffect(new PotionEffect(effectType, duration, amplifier));
    }

    private void giveXp(Player player, String value) {
        // L:5 - levels | P:100 - points
        if (value.startsWith("L:")) {
            int levels = Utils.parseIntSafe(value.substring(2), 0);
            player.giveExpLevels(levels);
        } else if (value.startsWith("P:")) {
            int points = Utils.parseIntSafe(value.substring(2), 0);
            player.giveExp(points);
        } else {
            player.giveExp(Utils.parseIntSafe(value, 0));
        }
    }

    // Placeholder

    private String applyPlaceholders(String input, Player player, Map<String, String> inputs) {
        String result = input.replace("<player>", player.getName());
        for (Map.Entry<String, String> entry : inputs.entrySet()) {
            result = result.replace("<input:" + entry.getKey() + ">", entry.getValue());
        }
        return result;
    }
}