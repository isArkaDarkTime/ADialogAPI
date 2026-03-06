package me.arkadarktime.aDialogAPI.listeners;

import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.events.DialogButtonClickEvent;
import me.arkadarktime.aDialogAPI.events.DialogCloseEvent;
import me.arkadarktime.aDialogAPI.events.DialogOpenEvent;
import me.arkadarktime.aDialogAPI.dialog.parsing.DialogParser;
import me.arkadarktime.aDialogAPI.dialog.LoadedDialog;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class DialogListener implements Listener {

    private final ADialogAPI plugin;

    public DialogListener(ADialogAPI plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDialogOpen(DialogOpenEvent event) {
        plugin.debug("[DialogListener] " + event.getPlayer().getName() + " opened dialog: " + event.getMeta().name());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDialogButtonClick(DialogButtonClickEvent event) {
        plugin.debug("[DialogListener] " + event.getPlayer().getName()
                + " clicked '" + event.getButton()
                + "' in '" + event.getMeta().name() + "'"
                + " inputs=" + event.getInputs());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDialogClose(DialogCloseEvent event) {
        plugin.debug("[DialogListener] " + event.getPlayer().getName()
                + " closed dialog '" + event.getMeta().name()
                + "' (reason=" + event.getReason() + ")");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCustomClick(PlayerCustomClickEvent event) {
        plugin.debug("[DEBUG] PlayerCustomClickEvent fired");

        Player player = null;
        if (event.getCommonConnection() instanceof Player p) {
            player = p;
        } else if (event.getCommonConnection() instanceof PlayerGameConnection connection) {
            player = connection.getPlayer();
        }

        if (player == null) {
            plugin.getLogger().warning("[DEBUG] Could not resolve Player from CommonConnection: "
                    + event.getCommonConnection().getClass().getName());
            return;
        }

        String keyStr = event.getIdentifier().asString();
        String expectedPrefix = DialogParser.KEY_NAMESPACE + ":" + DialogParser.KEY_PREFIX;

        plugin.debug("[DEBUG] Raw identifier = " + keyStr);

        if (!keyStr.startsWith(expectedPrefix)) {
            if (plugin.isDebugEnabled()) {
                plugin.getLogger().warning("[DEBUG] Identifier does not start with expected prefix");
            }
            return;
        }

        String path = keyStr.substring(expectedPrefix.length());
        int lastSlash = path.lastIndexOf('/');

        if (lastSlash < 0) {
            plugin.getLogger().warning("[DialogListener] Malformed dialog key: " + keyStr);
            return;
        }

        String dialogName = path.substring(0, lastSlash);
        String buttonId = path.substring(lastSlash + 1);

        plugin.debug("[DEBUG] dialogName=" + dialogName + " buttonId=" + buttonId);

        LoadedDialog loaded = plugin.getDialogManager().getLoaded(dialogName);
        if (loaded == null) {
            plugin.getLogger().warning("[DialogListener] Unknown dialog: " + dialogName);
            return;
        }

        Map<String, String> resolvedInputs = loaded.resolveInputsFromEvent(event);

        plugin.debug("[DEBUG] actions=" + loaded.getActions(buttonId).size()
                + " resolvedInputs=" + resolvedInputs);

        plugin.getDialogManager().executeActions(
                player,
                loaded.getActions(buttonId),
                loaded.getMeta(),
                buttonId,
                resolvedInputs
        );

        plugin.debug("[DEBUG] executeActions finished");
    }
}
