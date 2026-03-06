package me.arkadarktime.aDialogAPI.commands.adialogapi.subcommands;

import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.commands.core.SubCommand;
import me.arkadarktime.aDialogAPI.config.Messages;
import me.arkadarktime.aDialogAPI.config.Permissions;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ReloadSubCommand extends SubCommand {
    public ReloadSubCommand(ADialogAPI plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getPermission() {
        return Permissions.COMMANDS.RELOAD;
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length >= 1) {
            String name = args[0].toLowerCase();
            boolean success = plugin.getDialogManager().reloadDialog(name);

            if (!success) {
                sendMessage(sender, Messages.Commands.Dialog.Reload.NOT_FOUND, dialogNameResolver(name));
                return;
            }

            sendMessage(sender, Messages.Commands.Dialog.Reload.SUCCESS_DIALOG, dialogNameResolver(name));
            return;
        }

        plugin.reloadConfig();
        plugin.getLangManager().reload();
        plugin.getDialogManager().loadAll();
        sendMessage(sender, Messages.Commands.Dialog.Reload.SUCCESS);
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterByInput(new ArrayList<>(plugin.getDialogManager().getDialogNames()), args[0]);
        }
        return List.of();
    }
}
