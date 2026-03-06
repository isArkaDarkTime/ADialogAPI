package me.arkadarktime.aDialogAPI.actions;

import me.arkadarktime.aDialogAPI.ADialogAPI;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class DialogActionExecutor {

    private final ADialogAPI plugin;

    public DialogActionExecutor(ADialogAPI plugin) {
        this.plugin = plugin;
    }

    public void executeAll(Player player, List<ButtonAction> actions, Map<String, String> inputs) {
        for (ButtonAction action : actions) {
            plugin.debug("[ActionExecutor] " + action.getTypeName() + " | player=" + player.getName());
            action.execute(player, inputs, plugin);
        }
    }
}