package me.arkadarktime.aDialogAPI.events;

import me.arkadarktime.aDialogAPI.dialog.DialogMeta;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class DialogCloseEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final DialogMeta meta;
    private final CloseReason reason;

    public DialogCloseEvent(@NotNull Player player, DialogMeta meta, CloseReason reason) {
        super(player);
        this.meta = meta;
        this.reason = reason;
    }

    public DialogMeta getMeta() {
        return meta;
    }

    public CloseReason getReason() {
        return reason;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public enum CloseReason {
        ESCAPE,
        PLUGIN
    }
}
