package me.arkadarktime.aDialogAPI.commands;

import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.models.DialogMeta;
import me.arkadarktime.aDialogAPI.models.Messages;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DynamicDialogCommand extends Command {

    private final ADialogAPI plugin;
    private final String dialogName;


    public DynamicDialogCommand(ADialogAPI plugin, String commandName, String dialogName, String permission) {
        super(commandName);
        this.plugin = plugin;
        this.dialogName = dialogName;
        if (!permission.isEmpty()) setPermission(permission);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLangManager().getMessage(Messages.Errors.PLAYER_ONLY));
            return true;
        }

        DialogMeta meta = plugin.getDialogManager().getMeta(dialogName);
        if (meta != null && meta.hasCooldown()) {
            long remaining = plugin.getCooldownManager()
                    .getRemainingSeconds(player.getUniqueId(), dialogName, meta.cooldownSeconds());
            if (remaining > 0) {
                player.sendMessage(plugin.getLangManager().getMessage(
                        Messages.Errors.ON_COOLDOWN,
                        Placeholder.unparsed("seconds", String.valueOf(remaining))
                ));
                return true;
            }
        }

        if (!plugin.getDialogManager().showDialog(player, dialogName)) {
            sender.sendMessage(plugin.getLangManager().getMessage(Messages.Commands.Dialog.Show.NOT_FOUND));
        }

        return true;
    }
}
