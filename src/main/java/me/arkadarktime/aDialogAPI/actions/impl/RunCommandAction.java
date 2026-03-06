package me.arkadarktime.aDialogAPI.actions.impl;

import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.actions.ButtonAction;
import org.bukkit.entity.Player;

import java.util.Map;

public class RunCommandAction implements ButtonAction {

    private final String commandTemplate;

    public RunCommandAction(String commandTemplate) {
        this.commandTemplate = commandTemplate;
    }

    @Override
    public String getTypeName() {
        return "RUN_COMMAND";
    }

    @Override
    public void execute(Player player, Map<String, String> inputs, ADialogAPI plugin) {
        String cmd = applyPlaceholders(commandTemplate, player, inputs);
        player.performCommand(cmd.startsWith("/") ? cmd.substring(1) : cmd);
    }
}
