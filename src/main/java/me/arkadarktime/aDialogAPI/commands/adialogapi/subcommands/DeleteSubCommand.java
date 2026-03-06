package me.arkadarktime.aDialogAPI.commands.adialogapi.subcommands;

import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.commands.core.SubCommand;
import me.arkadarktime.aDialogAPI.config.Messages;
import me.arkadarktime.aDialogAPI.config.Permissions;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeleteSubCommand extends SubCommand {
    public DeleteSubCommand(ADialogAPI plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "delete";
    }

    @Override
    public String getPermission() {
        return Permissions.COMMANDS.DELETE;
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sendMessage(sender, Messages.Commands.Dialog.Delete.USAGE);
            return;
        }

        String name = args[0].toLowerCase();
        if (plugin.getDialogManager().getDialog(name) == null) {
            sendMessage(sender, Messages.Commands.Dialog.Delete.NOT_FOUND, dialogNameResolver(name));
            return;
        }

        try {
            plugin.getDialogManager().deleteDialog(name);
            sendMessage(sender, Messages.Commands.Dialog.Delete.SUCCESS);
        } catch ( IOException e ) {
            plugin.getLogger().severe("Failed to delete dialog '" + name + "': " + e.getMessage());
            sendMessage(sender, Messages.Errors.GENERIC);
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterByInput(new ArrayList<>(plugin.getDialogManager().getDialogNames()), args[0]);
        }

        return List.of();
    }
}
