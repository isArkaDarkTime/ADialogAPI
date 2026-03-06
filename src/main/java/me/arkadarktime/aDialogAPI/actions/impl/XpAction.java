package me.arkadarktime.aDialogAPI.actions.impl;

import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.actions.ButtonAction;
import org.bukkit.entity.Player;

import java.util.Map;

public final class XpAction extends ButtonAction {

    private final int amount;
    private final boolean levels;

    public XpAction(int amount, boolean levels) {
        this.amount = amount;
        this.levels = levels;
    }

    @Override
    public String getTypeName() {
        return "XP";
    }

    @Override
    public void execute(Player player, Map<String, String> inputs, ADialogAPI plugin) {
        if (levels) {
            player.giveExpLevels(amount);
        } else {
            player.giveExp(amount);
        }
    }
}
