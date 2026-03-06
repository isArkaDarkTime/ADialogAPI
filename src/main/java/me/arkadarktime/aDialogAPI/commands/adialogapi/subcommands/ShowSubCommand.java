package me.arkadarktime.aDialogAPI.commands.adialogapi.subcommands;

import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.commands.core.SubCommand;
import me.arkadarktime.aDialogAPI.config.Messages;
import me.arkadarktime.aDialogAPI.config.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShowSubCommand extends SubCommand {
    public ShowSubCommand(ADialogAPI plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "show";
    }

    @Override
    public String getPermission() {
        return Permissions.COMMANDS.SHOW;
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sendMessage(sender, Messages.Commands.Dialog.Show.USAGE);
            return;
        }

        String dialogName = args[0].toLowerCase();
        if (plugin.getDialogManager().getDialog(dialogName) == null) {
            sendMessage(sender, Messages.Commands.Dialog.Show.NOT_FOUND, dialogNameResolver(dialogName));
            return;
        }

        Player target = resolveTarget(sender, args);
        if (target == null) return;

        boolean shown = plugin.getDialogManager().showDialog(target, dialogName);
        if (!shown) {
            sendMessage(sender, Messages.Commands.Dialog.Show.NOT_FOUND, dialogNameResolver(dialogName));
            return;
        }

        sendMessage(sender, Messages.Commands.Dialog.Show.SUCCESS, nameResolver(target), dialogNameResolver(dialogName));
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterByInput(new ArrayList<>(plugin.getDialogManager().getDialogNames()), args[0]);
        }

        if (args.length == 2) {
            return filterByInput(
                    Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .collect(Collectors.toList()),
                    args[1]);
        }

        return List.of();
    }

    private Player resolveTarget(CommandSender sender, String[] args) {
        if (args.length >= 2) {
            String targetName = args[1];
            Player target = Bukkit.getPlayerExact(targetName);
            if (target == null) {
                sendMessage(sender, Messages.Errors.TARGET_NOT_FOUND, nameResolver(targetName));
                return null;
            }
            return target;
        }

        if (sender instanceof Player player) {
            return player;
        }

        sendMessage(sender, Messages.Errors.PLAYER_ONLY);
        return null;
    }
}
