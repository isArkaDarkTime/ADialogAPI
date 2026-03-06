package me.arkadarktime.aDialogAPI.events;

import me.arkadarktime.aDialogAPI.dialog.DialogMeta;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class DialogOpenEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled = false;

    private final DialogMeta meta;

    public DialogOpenEvent(@NotNull Player player, DialogMeta meta) {
        super(player);
        this.meta = meta;
    }

    public DialogMeta getMeta() {
        return meta;
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
