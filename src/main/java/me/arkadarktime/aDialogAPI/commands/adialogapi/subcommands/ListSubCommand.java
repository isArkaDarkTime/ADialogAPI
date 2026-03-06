package me.arkadarktime.aDialogAPI.commands.adialogapi.subcommands;

import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.commands.core.SubCommand;
import me.arkadarktime.aDialogAPI.dialog.DialogMeta;
import me.arkadarktime.aDialogAPI.config.Messages;
import me.arkadarktime.aDialogAPI.config.Permissions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListSubCommand extends SubCommand {
    public ListSubCommand(ADialogAPI plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getPermission() {
        return Permissions.COMMANDS.LIST;
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Collection<DialogMeta> allMetas = plugin.getDialogManager().getAllMetas();

        if (allMetas.isEmpty()) {
            sendMessage(sender, Messages.Commands.Dialog.List.EMPTY);
            return;
        }

        List<Component> lines = new ArrayList<>();
        lines.add(getMessage(Messages.Commands.Dialog.List.HEADER));

        for (DialogMeta meta : allMetas) {
            lines.add(getMessage(Messages.Commands.Dialog.List.ROW, buildDialogRowResolvers(meta)));
        }

        lines.add(getMessage(Messages.Commands.Dialog.List.FOOTER, Placeholder.unparsed("count", String.valueOf(allMetas.size()))));

        sender.sendMessage(Component.join(JoinConfiguration.newlines(), lines));
    }
}
