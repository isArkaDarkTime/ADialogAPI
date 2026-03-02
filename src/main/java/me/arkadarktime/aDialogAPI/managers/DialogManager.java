package me.arkadarktime.aDialogAPI.managers;

import io.papermc.paper.dialog.Dialog;
import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.commands.DynamicDialogCommand;
import me.arkadarktime.aDialogAPI.events.DialogButtonClickEvent;
import me.arkadarktime.aDialogAPI.events.DialogCloseEvent;
import me.arkadarktime.aDialogAPI.events.DialogOpenEvent;
import me.arkadarktime.aDialogAPI.models.ButtonAction;
import me.arkadarktime.aDialogAPI.models.DialogMeta;
import me.arkadarktime.aDialogAPI.models.LoadedDialog;
import me.arkadarktime.aDialogAPI.models.ParsedInputs;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DialogManager {

    private static final String DIALOGS_FOLDER = "dialogs";
    private static final String[] EXAMPLE_FILES = {
            "examples/notice.yml",
            "examples/confirmation.yml",
            "examples/items.yml",
            "examples/inputs.yml",
            "examples/actions.yml",
            "examples/quests/quest1.yml",
            "examples/quests/quest2.yml",
    };

    private final ADialogAPI plugin;
    private final DialogParser parser;
    private final DialogActionExecutor actionExecutor;

    private final Map<String, LoadedDialog> registry = new HashMap<>();
    private final Set<String> registeredCommands = new HashSet<>();

    public DialogManager(ADialogAPI plugin) {
        this.plugin = plugin;
        this.parser = new DialogParser(plugin);
        this.actionExecutor = new DialogActionExecutor(plugin);
    }

    // Loading

    public void loadAll() {
        clearAll();

        File root = getDialogsFolder();
        if (!root.exists()) {
            root.mkdirs();
            saveExamples();
        }

        int loaded = scanFolder(root);
        plugin.getLogger().info("Loaded " + loaded + " dialog(s).");
    }

    public boolean reloadDialog(String name) {
        name = name.toLowerCase();
        LoadedDialog existing = registry.get(name);
        File file = existing != null ? existing.getFile() : resolveFile(name);

        if (file == null || !file.exists()) {
            plugin.getLogger().warning("[DialogManager] Cannot reload '" + name + "': file not found.");
            return false;
        }

        unload(name);
        boolean success = loadFile(file);
        plugin.debug("[DialogManager] Hot-reload '" + name + "': " + (success ? "OK" : "FAILED"));
        return success;
    }

    // CRUD

    public void createDialog(String name) throws IOException {
        File file = requireValidFile(name);
        if (file.exists()) throw new IllegalStateException("Dialog already exists: " + name);

        File parent = file.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IOException("Failed to create directory: " + parent.getPath());
        }

        buildDefaultConfig(name).save(file);
        loadFile(file);
    }

    public void deleteDialog(String name) throws IOException {
        name = name.toLowerCase();
        LoadedDialog existing = registry.get(name);
        File file = existing != null ? existing.getFile() : resolveFile(name);

        if (file == null || !file.exists()) throw new IllegalStateException("Dialog file not found: " + name);
        if (!file.delete()) throw new IOException("Failed to delete file: " + file.getPath());

        unload(name);
    }

    public boolean showDialog(Player player, String name) {
        LoadedDialog loaded = registry.get(name.toLowerCase());
        if (loaded == null) return false;

        DialogMeta meta = loaded.getMeta();
        if (meta.hasCooldown()) {
            CooldownManager cm = plugin.getCooldownManager();
            if (cm.isOnCooldown(player.getUniqueId(), name, meta.cooldownSeconds())) {
                long remaining = cm.getRemainingSeconds(player.getUniqueId(), name, meta.cooldownSeconds());
                plugin.debug("[DialogManager] " + player.getName() + " is on cooldown for '" + name + "' (" + remaining + "s left)");
                return false;
            }
            cm.setCooldown(player.getUniqueId(), name);
        }

        DialogOpenEvent event = new DialogOpenEvent(player, meta);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("[DialogManager] DialogOpenEvent cancelled for '" + name + "' by a listener.");
            return false;
        }

        player.showDialog(loaded.getDialog());
        return true;
    }

    public boolean closeDialog(Player player, String dialogName) {
        LoadedDialog loaded = registry.get(dialogName.toLowerCase());
        if (loaded == null) return false;

        DialogCloseEvent event = new DialogCloseEvent(player, loaded.getMeta(), DialogCloseEvent.CloseReason.PLUGIN);
        Bukkit.getPluginManager().callEvent(event);

        player.closeInventory();
        plugin.debug("[DialogManager] Close dialog '" + dialogName + "' for " + player.getName());
        return true;
    }

    public void executeActions(Player player, List<ButtonAction> actions, DialogMeta meta, String buttonId, Map<String, String> inputs) {
        DialogButtonClickEvent event = new DialogButtonClickEvent(player, meta, buttonId, inputs);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            plugin.debug("[DialogManager] DialogButtonClickEvent cancelled for button '" + buttonId
                    + "' in '" + meta.name() + "' by a listener. Actions skipped.");
            return;
        }

        plugin.debug("[DialogManager] " + player.getName()
                + " clicked '" + buttonId + "' in '" + meta.name()
                + "' - " + actions.size() + " action(s), inputs: " + inputs);

        actionExecutor.executeAll(player, actions, inputs);
    }

    // Getters

    public LoadedDialog getLoaded(String name) {
        return registry.get(name.toLowerCase());
    }

    public Dialog getDialog(String name) {
        return Optional.ofNullable(registry.get(name.toLowerCase())).map(LoadedDialog::getDialog).orElse(null);
    }

    public DialogMeta getMeta(String name) {
        return Optional.ofNullable(registry.get(name.toLowerCase())).map(LoadedDialog::getMeta).orElse(null);
    }

    public Set<String> getDialogNames() {
        return Collections.unmodifiableSet(registry.keySet());
    }

    public Collection<DialogMeta> getAllMetas() {
        return registry.values().stream().map(LoadedDialog::getMeta).toList();
    }

    public File getDialogsFolder() {
        return new File(plugin.getDataFolder(), DIALOGS_FOLDER);
    }

    // Internal loading

    private int scanFolder(File folder) {
        File[] entries = folder.listFiles();
        if (entries == null) return 0;

        int count = 0;
        for (File entry : entries) {
            if (entry.isDirectory()) {
                count += scanFolder(entry);
            } else if (entry.getName().endsWith(".yml")) {
                if (loadFile(entry)) count++;
            }
        }
        return count;
    }

    private boolean loadFile(File file) {
        String name = toRelativeName(file);
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        Map<String, List<ButtonAction>> actions = parser.parseButtonActions(name, config);
        DialogMeta meta = parser.parseMeta(name, config, name);
        ParsedInputs inputs = parser.parseInputDefinitions(config);

        Dialog dialog = parser.parseDialog(name, config, actions).orElse(null);
        if (dialog == null) {
            plugin.getLogger().warning("[DialogManager] Failed to parse: " + name);
            return false;
        }

        LoadedDialog loaded = new LoadedDialog(dialog, meta, file, actions, inputs.defaults(), inputs.metas());
        registry.put(name, loaded);

        if (loaded.hasOpenCommand()) {
            registerDynamicCommand(loaded.getOpenCommand(), name, loaded.getOpenPermission());
        }

        plugin.debug("[DialogManager] Loaded: " + loaded);
        return true;
    }

    // Dynamic command registration

    private void registerDynamicCommand(String commandName, String dialogName, String permission) {
        Bukkit.getCommandMap().register(
                plugin.getName().toLowerCase(),
                new DynamicDialogCommand(plugin, commandName, dialogName, permission)
        );
        registeredCommands.add(commandName);
        plugin.debug("[DialogManager] Registered /" + commandName + " → " + dialogName);
    }

    private void unregisterCommand(String commandName) {
        String prefixed = plugin.getName().toLowerCase() + ":" + commandName;
        Map<String, Command> known = Bukkit.getCommandMap().getKnownCommands();
        known.remove(commandName);
        known.remove(prefixed);
        registeredCommands.remove(commandName);
    }

    // Helpers

    private void clearAll() {
        registry.clear();
        new HashSet<>(registeredCommands).forEach(this::unregisterCommand);
        plugin.getCooldownManager().clearAll();
    }

    private void unload(String name) {
        LoadedDialog loaded = registry.remove(name);
        if (loaded != null && loaded.hasOpenCommand()) {
            unregisterCommand(loaded.getOpenCommand());
        }
    }

    private String toRelativeName(File file) {
        String root = getDialogsFolder().getAbsolutePath() + File.separator;
        return file.getAbsolutePath()
                .replace(root, "")
                .replace(File.separator, "/")
                .replaceAll("\\.yml$", "")
                .toLowerCase();
    }

    private File resolveFile(String name) {
        if (name == null || name.isBlank()) return null;
        return new File(getDialogsFolder(), name.replace("/", File.separator) + ".yml");
    }

    private File requireValidFile(String name) {
        File file = resolveFile(name);
        if (file == null) throw new IllegalArgumentException("Invalid dialog name: " + name);
        return file;
    }

    private void saveExamples() {
        for (String fileName : EXAMPLE_FILES) {
            plugin.saveResource(DIALOGS_FOLDER + "/" + fileName, false);
        }
    }

    private FileConfiguration buildDefaultConfig(String name) {
        FileConfiguration config = new YamlConfiguration();
        config.set("title", "<green>New Dialog - " + name);
        config.set("can_close_with_escape", true);
        config.set("cooldown", 0);
        config.set("type", "notice");
        config.set("open_command", "");
        config.set("open_permission", "");
        config.set("body", List.of(Map.of("type", "text", "content", "<gray>Hello world!")));
        return config;
    }
}
