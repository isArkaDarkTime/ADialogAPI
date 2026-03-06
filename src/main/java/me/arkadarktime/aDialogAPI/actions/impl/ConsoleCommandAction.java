package me.arkadarktime.aDialogAPI.actions.impl;

import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.actions.ButtonAction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;

public class ConsoleCommandAction extends ButtonAction {

    private final String commandTemplate;

    public ConsoleCommandAction(String commandTemplate) {
        this.commandTemplate = commandTemplate;
    }

    @Override
    public String getTypeName() {
        return "CONSOLE_COMMAND";
    }

    @Override
    public void execute(Player player, Map<String, String> inputs, ADialogAPI plugin) {
        String cmd = applyPlaceholders(commandTemplate, player, inputs);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.startsWith("/") ? cmd.substring(1) : cmd);
    }
}
