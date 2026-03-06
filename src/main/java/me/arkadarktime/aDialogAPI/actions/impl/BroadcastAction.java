package me.arkadarktime.aDialogAPI.actions.impl;

import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.actions.ButtonAction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;

public final class BroadcastAction implements ButtonAction {

    private final String contentTemplate;

    public BroadcastAction(String contentTemplate) {
        this.contentTemplate = contentTemplate;
    }

    @Override
    public String getTypeName() {
        return "BROADCAST";
    }

    @Override
    public void execute(Player player, Map<String, String> inputs, ADialogAPI plugin) {
        String text = applyPlaceholders(contentTemplate, player, inputs);
        Bukkit.broadcast(mm.deserialize(text));
    }
}
