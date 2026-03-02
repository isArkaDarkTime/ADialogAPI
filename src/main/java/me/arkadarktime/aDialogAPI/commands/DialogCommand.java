package me.arkadarktime.aDialogAPI.commands;

import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.commands.core.BaseCommand;
import me.arkadarktime.aDialogAPI.commands.core.SubCommand;
import me.arkadarktime.aDialogAPI.commands.subcommands.adialogapi.*;
import me.arkadarktime.aDialogAPI.models.Messages;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DialogCommand extends BaseCommand implements CommandExecutor, TabCompleter {

    private final Map<String, SubCommand> subcommands = new HashMap<>();

    public DialogCommand(ADialogAPI plugin) {
        super(plugin);
        register(new ReloadSubCommand(plugin));
        register(new CreateSubCommand(plugin));
        register(new DeleteSubCommand(plugin));
        register(new ShowSubCommand(plugin));
        register(new ListSubCommand(plugin));
        register(new InfoSubCommand(plugin));
        register(new HelpSubCommand(plugin));
    }

    private void register(SubCommand subcommand) {
        subcommands.put(subcommand.getName().toLowerCase(), subcommand);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            subcommands.get("help").execute(sender, args);
            return true;
        }

        String sub = args[0].toLowerCase();
        SubCommand subcommand = subcommands.get(sub);
        if (subcommand == null) {
            sendMessage(sender, Messages.Errors.UNKNOWN_SUBCOMMAND, Placeholder.unparsed("subcommand", sub));
            return true;
        }

        if (subcommand.isPlayerOnly() && !(sender instanceof Player)) {
            sendMessage(sender, Messages.Errors.PLAYER_ONLY);
            return true;
        }

        if (!hasPerm(sender, subcommand.getPermission())) return true;

        subcommand.execute(sender, Arrays.copyOfRange(args, 1, args.length));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return filterByInput(new ArrayList<>(subcommands.keySet()), args[0]);
        }

        if (args.length >= 2) {
            SubCommand subcommand = subcommands.get(args[0].toLowerCase());
            if (subcommand != null && sender.hasPermission(subcommand.getPermission())) {
                return subcommand.complete(sender, Arrays.copyOfRange(args, 1, args.length));
            }
        }

        return List.of();
    }
}
