package me.arkadarktime.aDialogAPI.actions.impl;

import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.actions.ButtonAction;
import org.bukkit.entity.Player;

import java.util.Map;

public final class MessageAction extends ButtonAction {

    private final String contentTemplate;

    public MessageAction(String contentTemplate) {
        this.contentTemplate = contentTemplate;
    }

    @Override
    public String getTypeName() {
        return "MESSAGE";
    }

    @Override
    public void execute(Player player, Map<String, String> inputs, ADialogAPI plugin) {
        String text = applyPlaceholders(contentTemplate, player, inputs);
        player.sendMessage(mm.deserialize(text));
    }
}
