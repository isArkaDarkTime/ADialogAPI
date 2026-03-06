package me.arkadarktime.aDialogAPI.commands.adialogapi.subcommands;

import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.commands.core.SubCommand;
import me.arkadarktime.aDialogAPI.config.Messages;
import me.arkadarktime.aDialogAPI.config.Permissions;
import org.bukkit.command.CommandSender;

import java.io.IOException;

public class CreateSubCommand extends SubCommand {
    public CreateSubCommand(ADialogAPI plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getPermission() {
        return Permissions.COMMANDS.CREATE;
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sendMessage(sender, Messages.Commands.Dialog.Create.USAGE);
            return;
        }

        String name = args[0].toLowerCase();
        if (plugin.getDialogManager().getDialog(name) != null) {
            sendMessage(sender, Messages.Commands.Dialog.Create.ALREADY_EXISTS, dialogNameResolver(name));
            return;
        }

        try {
            plugin.getDialogManager().createDialog(name);
            sendMessage(sender, Messages.Commands.Dialog.Create.SUCCESS);
        } catch ( IOException e ) {
            plugin.getLogger().severe("Failed to create dialog '" + name + "': " + e.getMessage());
            sendMessage(sender, Messages.Errors.GENERIC);
        }
    }
}
