package me.arkadarktime.aDialogAPI.actions.impl;

import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.actions.ButtonAction;
import org.bukkit.entity.Player;

import java.util.Map;

public final class ShowDialogAction implements ButtonAction {

    private final String dialogName;

    public ShowDialogAction(String dialogName) {
        this.dialogName = dialogName;
    }

    @Override
    public String getTypeName() {
        return "SHOW_DIALOG";
    }

    @Override
    public void execute(Player player, Map<String, String> inputs, ADialogAPI plugin) {
        boolean shown = plugin.getDialogManager().showDialog(player, dialogName);
        if (!shown) {
            plugin.getLogger().warning("[ShowDialogAction] Dialog '" + dialogName + "' not found or could not be shown.");
        }
    }
}
