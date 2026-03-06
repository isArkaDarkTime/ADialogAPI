package me.arkadarktime.aDialogAPI.commands.adialogapi.subcommands;

import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.commands.core.SubCommand;
import me.arkadarktime.aDialogAPI.config.Messages;
import me.arkadarktime.aDialogAPI.config.Permissions;
import org.bukkit.command.CommandSender;

public class HelpSubCommand extends SubCommand {
    public HelpSubCommand(ADialogAPI plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getPermission() {
        return Permissions.COMMANDS.HELP;
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sendMessage(sender, Messages.Commands.Dialog.Help);
    }
}
