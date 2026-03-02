package me.arkadarktime.aDialogAPI.commands.core;

import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.models.DialogMeta;
import me.arkadarktime.aDialogAPI.models.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class BaseCommand {

    protected final ADialogAPI plugin;

    public BaseCommand(ADialogAPI plugin) {
        this.plugin = plugin;
    }

    // Permissions

    protected boolean hasPerm(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) return true;
        sender.sendMessage(getMessage(Messages.Errors.NO_PERMISSION));
        return false;
    }

    // Messaging

    protected Component getMessage(String key, TagResolver... placeholders) {
        return plugin.getLangManager().getMessage(key, placeholders);
    }

    protected void sendMessage(CommandSender sender, String key, TagResolver... placeholders) {
        sender.sendMessage(getMessage(key, placeholders));
    }

    // Tag resolvers

    protected TagResolver.Single nameResolver(String name) {
        return Placeholder.unparsed("name", name);
    }

    protected TagResolver.Single nameResolver(Player player) {
        return Placeholder.component("name", player.displayName());
    }

    protected TagResolver.Single dialogNameResolver(String dialogName) {
        return Placeholder.unparsed("dialog", dialogName);
    }

    protected TagResolver.Single secondsResolver(long seconds) {
        return Placeholder.unparsed("seconds", String.valueOf(seconds));
    }

    protected TagResolver buildDialogRowResolvers(DialogMeta meta) {
        Component commandEntry = meta.openCommand().isEmpty()
                ? getMessage(Messages.Commands.Dialog.List.CommandEntry.EMPTY)
                : getMessage(Messages.Commands.Dialog.List.CommandEntry.COMMAND,
                Placeholder.unparsed("command", meta.openCommand()));

        Component permissionEntry = meta.openPermission().isEmpty()
                ? getMessage(Messages.Commands.Dialog.List.PermissionEntry.EMPTY)
                : getMessage(Messages.Commands.Dialog.List.PermissionEntry.PERMISSION,
                Placeholder.unparsed("permission", meta.openPermission()));

        Component escapeEntry = meta.canEscape()
                ? getMessage(Messages.Commands.Dialog.Info.VALUE_YES)
                : getMessage(Messages.Commands.Dialog.Info.VALUE_NO);

        return TagResolver.resolver(
                Placeholder.unparsed("name", meta.name()),
                Placeholder.unparsed("type", meta.type()),
                Placeholder.unparsed("file_path", meta.filePath()),
                Placeholder.unparsed("body_count", String.valueOf(meta.bodyCount())),
                Placeholder.component("command_entry", commandEntry),
                Placeholder.component("open_permission", permissionEntry),
                Placeholder.component("escape", escapeEntry)
        );
    }

    // Tab completion

    protected List<String> filterByInput(List<String> options, String input) {
        return options.stream()
                .filter(o -> o.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }
}
