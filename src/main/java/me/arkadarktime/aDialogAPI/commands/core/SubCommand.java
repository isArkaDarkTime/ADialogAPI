package me.arkadarktime.aDialogAPI.commands.core;

import me.arkadarktime.aDialogAPI.ADialogAPI;
import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class SubCommand extends BaseCommand {
    public SubCommand(ADialogAPI plugin) {
        super(plugin);
    }

    public abstract String getName();

    public abstract String getPermission();

    public abstract boolean isPlayerOnly();

    public abstract void execute(CommandSender sender, String[] args);

    public List<String> complete(CommandSender sender, String[] args) {
        return List.of();
    }
}
