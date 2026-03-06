package me.arkadarktime.aDialogAPI.events;

import me.arkadarktime.aDialogAPI.dialog.DialogMeta;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

public class DialogButtonClickEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final DialogMeta meta;
    private final String button;
    private final Map<String, String> inputs;
    private boolean cancelled = false;

    public DialogButtonClickEvent(@NotNull Player player, DialogMeta meta, String button, Map<String, String> inputs) {
        super(player);
        this.meta = meta;
        this.button = button;
        this.inputs = Collections.unmodifiableMap(inputs);
    }

    public DialogMeta getMeta() {
        return meta;
    }

    public String getButton() {
        return button;
    }

    public Map<String, String> getInputs() {
        return inputs;
    }

    public String getInput(String key) {
        return inputs.getOrDefault(key, "");
    }

    public boolean hasInput(String key) {
        return inputs.containsKey(key);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
