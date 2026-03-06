package me.arkadarktime.aDialogAPI.actions.impl;

import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.actions.ButtonAction;
import org.bukkit.entity.Player;

import java.util.Map;

public final class CloseAction extends ButtonAction {

    @Override
    public String getTypeName() {
        return "CLOSE";
    }

    @Override
    public void execute(Player player, Map<String, String> inputs, ADialogAPI plugin) {
        player.closeInventory();
    }
}
