package me.arkadarktime.aDialogAPI.dialog.parsing;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.actions.ActionFactory;
import me.arkadarktime.aDialogAPI.actions.ButtonAction;
import me.arkadarktime.aDialogAPI.dialog.DialogMeta;
import me.arkadarktime.aDialogAPI.dialog.ParsedInputs;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class DialogParser {

    public static final String KEY_NAMESPACE = "adialogapi";
    public static final String KEY_PREFIX = "dialog/";

    private final ADialogAPI plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private final BodyParser bodyParser;
    private final InputParser inputParser;
    private final ActionFactory actionFactory;

    public DialogParser(ADialogAPI plugin) {
        this.plugin = plugin;
        this.bodyParser = new BodyParser(plugin);
        this.inputParser = new InputParser(plugin);
        this.actionFactory = new ActionFactory(plugin);
    }

    // Public

    public Optional<Dialog> parseDialog(String id, FileConfiguration config, Map<String, List<ButtonAction>> actions) {
        try {
            Component title = mm.deserialize(config.getString("title", "<yellow>Dialog"));
            boolean canEscape = config.getBoolean("can_close_with_escape", true);
            String type = config.getString("type", "notice").toLowerCase();

            DialogBase base = DialogBase.builder(title)
                    .body(bodyParser.parse(id, config))
                    .inputs(inputParser.parseInputs(id, config))
                    .canCloseWithEscape(canEscape)
                    .build();

            return Optional.ofNullable(buildTypedDialog(id, type, base, config, actions));
        } catch ( Exception e ) {
            plugin.getLogger().severe("[DialogParser] Error parsing '" + id + "': " + e.getMessage());
            return Optional.empty();
        }
    }

    public DialogMeta parseMeta(String name, FileConfiguration config, String relativePath) {
        return new DialogMeta(
                name,
                config.getString("type", "notice").toLowerCase(),
                relativePath,
                config.getString("open_command", "").trim(),
                config.getString("open_permission", "").trim(),
                config.getBoolean("can_close_with_escape", true),
                config.getMapList("body").size(),
                Math.max(0L, config.getLong("cooldown", 0L))
        );
    }

    public ParsedInputs parseInputDefinitions(FileConfiguration config) {
        return inputParser.parseDefinitions(config);
    }

    public Map<String, List<ButtonAction>> parseButtonActions(String id, FileConfiguration config) {
        return actionFactory.parseAll(id, config);
    }

    // Dialog type builder

    private Dialog buildTypedDialog(String id, String type, DialogBase base, FileConfiguration config, Map<String, List<ButtonAction>> actions) {
        return switch (type) {
            case "notice" -> {
                ActionButton ok = buildActionButton(
                        config.getConfigurationSection("actions.ok"),
                        "Ok",
                        actions.getOrDefault("ok", List.of()),
                        id, "ok"
                );
                yield Dialog.create(builder -> builder.empty().base(base).type(DialogType.notice(ok)));
            }
            case "confirmation" -> {
                ActionButton yes = buildActionButton(
                        config.getConfigurationSection("actions.yes"),
                        "<green>Yes",
                        actions.getOrDefault("yes", List.of()),
                        id, "yes"
                );
                ActionButton no = buildActionButton(
                        config.getConfigurationSection("actions.no"),
                        "<red>No",
                        actions.getOrDefault("no", List.of()),
                        id, "no"
                );
                yield Dialog.create(builder -> builder.empty().base(base).type(DialogType.confirmation(yes, no)));
            }
            default -> {
                plugin.getLogger().warning("[DialogParser] Unknown dialog type '" + type + "' in: " + id);
                yield null;
            }
        };
    }

    private ActionButton buildActionButton(ConfigurationSection section, String defaultLabel, List<ButtonAction> actions, String dialogId, String buttonId) {
        String labelStr = section != null ? section.getString("label", defaultLabel) : defaultLabel;
        ActionButton.Builder builder = ActionButton.builder(mm.deserialize(labelStr));

        if (section != null) {
            String tooltip = section.getString("tooltip");
            if (tooltip != null) builder.tooltip(mm.deserialize(tooltip));
        }

        if (!actions.isEmpty()) {
            Key key = buttonKey(dialogId, buttonId);
            builder.action(DialogAction.customClick(key, null));
            plugin.debug("[DialogParser] Bound button '" + labelStr + "' to key: " + key.asString());
        } else {
            plugin.debug("[DialogParser] Button '" + labelStr + "' has no after_actions — no custom click bound.");
        }

        return builder.build();
    }

    @SuppressWarnings("PatternValidation")
    public static Key buttonKey(String dialogId, String buttonId) {
        return Key.key(KEY_NAMESPACE, KEY_PREFIX + dialogId + "/" + buttonId);
    }
}
