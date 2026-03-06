package me.arkadarktime.aDialogAPI.managers;

import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.config.Constants;
import me.arkadarktime.aDialogAPI.config.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LangManager {

    private final ADialogAPI plugin;
    private final Map<String, Object> rawMessages = new HashMap<>();
    private final MiniMessage mm = MiniMessage.miniMessage();
    private Component prefix;

    public LangManager(ADialogAPI plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        rawMessages.clear();

        String lang = plugin.getConfig().getString(Constants.LANGUAGE, "en");
        String langFilePath = "lang/" + lang + ".yml";
        File langFile = new File(plugin.getDataFolder(), langFilePath);

        if (!langFile.exists()) {
            plugin.saveResource(langFilePath, false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(langFile);

        for (String key : config.getKeys(true)) {
            if (config.isString(key)) {
                rawMessages.put(key, config.getString(key));
            } else if (config.isList(key)) {
                rawMessages.put(key, config.getStringList(key));
            }

            plugin.debug("Loaded " + key + " message.");
        }

        this.prefix = mm.deserialize((String) rawMessages.getOrDefault(Messages.PREFIX, "<color:#33ff77>ADialogAPI</color> <gray>•</gray><reset>"));
        plugin.getLogger().info("Loaded " + rawMessages.size() + " message(s).");
    }

    public Component getMessage(@NotNull String key, TagResolver... placeholders) {
        Object object = rawMessages.get(key);
        if (object == null) {
            plugin.getLogger().warning("Missing lang key: " + key);
            return Component.text("Missing key: " + key).color(NamedTextColor.RED);
        }

        TagResolver tagResolver = TagResolver.resolver(Placeholder.component("prefix", prefix), TagResolver.resolver(placeholders));

        if (object instanceof String text) {
            return mm.deserialize(text, tagResolver);
        }

        if (object instanceof List<?> list) {
            Component finalComponent = Component.empty();
            boolean first = true;

            for (Object line : list) {
                if (!first) {
                    finalComponent = finalComponent.append(Component.newline());
                }

                finalComponent = finalComponent.append(mm.deserialize(line.toString(), tagResolver));
                first = false;
            }

            return finalComponent;
        }

        return Component.text("<red>Invalid format for key: " + key).color(NamedTextColor.RED);
    }
}
