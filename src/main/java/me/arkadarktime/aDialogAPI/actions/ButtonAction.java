package me.arkadarktime.aDialogAPI.actions;

import me.arkadarktime.aDialogAPI.ADialogAPI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public interface ButtonAction {

    String getTypeName();

    void execute(Player player, Map<String, String> inputs, ADialogAPI plugin);

    MiniMessage mm = MiniMessage.miniMessage();

    default String applyPlaceholders(String input, Player player, Map<String, String> inputs) {
        for (Map.Entry<String, String> entry : inputs.entrySet()) {
            input = input.replace("<input:" + entry.getKey() + ">", entry.getValue());
        }
        return input.replace("<player>", player.getName());
    }

    static void executeAll(Player player, List<ButtonAction> actions, Map<String, String> inputs, ADialogAPI plugin) {
        for (ButtonAction action : actions) {
            plugin.debug("[ActionExecutor] " + action.getTypeName() + " | player=" + player.getName());
            action.execute(player, inputs, plugin);
        }
    }
}
