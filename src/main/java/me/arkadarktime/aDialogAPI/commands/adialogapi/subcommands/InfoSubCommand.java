package me.arkadarktime.aDialogAPI.commands.adialogapi.subcommands;

import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.commands.core.SubCommand;
import me.arkadarktime.aDialogAPI.dialog.DialogMeta;
import me.arkadarktime.aDialogAPI.config.Messages;
import me.arkadarktime.aDialogAPI.config.Permissions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class InfoSubCommand extends SubCommand {

    public InfoSubCommand(ADialogAPI plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getPermission() {
        return Permissions.COMMANDS.INFO;
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sendMessage(sender, Messages.Commands.Dialog.Info.USAGE);
            return;
        }

        String name = args[0].toLowerCase();
        DialogMeta meta = plugin.getDialogManager().getMeta(name);

        if (meta == null) {
            sendMessage(sender, Messages.Commands.Dialog.Info.NOT_FOUND, dialogNameResolver(name));
            return;
        }

        sender.sendMessage(Component.join(JoinConfiguration.newlines(),
                getMessage(Messages.Commands.Dialog.Info.HEADER, dialogNameResolver(name)),
                getMessage(Messages.Commands.Dialog.Info.ROW, buildDialogRowResolvers(meta)),
                getMessage(Messages.Commands.Dialog.Info.FOOTER)
        ));
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterByInput(new ArrayList<>(plugin.getDialogManager().getDialogNames()), args[0]);
        }
        return List.of();
    }
}