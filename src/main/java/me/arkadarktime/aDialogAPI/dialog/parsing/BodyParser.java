package me.arkadarktime.aDialogAPI.dialog.parsing;

import io.papermc.paper.registry.data.dialog.body.DialogBody;
import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.util.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class BodyParser {
    private final ADialogAPI plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public BodyParser(ADialogAPI plugin) {
        this.plugin = plugin;
    }

    List<DialogBody> parse(String id, FileConfiguration config) {
        List<DialogBody> result = new ArrayList<>();
        for (Map<?, ?> entry : config.getMapList("body")) {
            Object typeObj = entry.get("type");
            if (typeObj == null) continue;

            switch (typeObj.toString().toLowerCase()) {
                case "text" -> result.add(parseText(entry));
                case "item" -> parseItem(entry, id).ifPresent(result::add);
                default -> plugin.getLogger().warning("[BodyParser] Unknown body type '" + typeObj + "' in: " + id);
            }
        }
        return result;
    }

    private DialogBody parseText(Map<?, ?> entry) {
        Object content = entry.get("content");
        return DialogBody.plainMessage(content != null ? mm.deserialize(content.toString()) : Component.empty());
    }

    private Optional<DialogBody> parseItem(Map<?, ?> entry, String dialogId) {
        Object materialObj = entry.get("material");
        if (materialObj == null) {
            plugin.getLogger().warning("[BodyParser] Missing 'material' in item body of '" + dialogId + "'");
            return Optional.empty();
        }

        Material material = Material.matchMaterial(materialObj.toString());
        if (material == null) {
            plugin.getLogger().warning("[BodyParser] Unknown material '" + materialObj + "' in '" + dialogId + "'");
            return Optional.empty();
        }

        int count = Utils.parseIntSafe(entry.get("count"), 1);
        return Optional.of(DialogBody.item(new ItemStack(material, Math.max(count, 1))).build());
    }
}
