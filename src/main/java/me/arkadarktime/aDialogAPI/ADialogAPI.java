package me.arkadarktime.aDialogAPI;

import me.arkadarktime.aDialogAPI.commands.adialogapi.DialogCommand;
import me.arkadarktime.aDialogAPI.listeners.DialogListener;
import me.arkadarktime.aDialogAPI.managers.CooldownManager;
import me.arkadarktime.aDialogAPI.managers.DialogManager;
import me.arkadarktime.aDialogAPI.managers.LangManager;
import me.arkadarktime.aDialogAPI.config.Constants;
import org.bukkit.plugin.java.JavaPlugin;

public final class ADialogAPI extends JavaPlugin {

    private boolean isDebugEnabled;
    private LangManager langManager;
    private CooldownManager cooldownManager;
    private DialogManager dialogManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        isDebugEnabled = getConfig().getBoolean(Constants.DEBUG);

        langManager = new LangManager(this);
        cooldownManager = new CooldownManager();
        dialogManager = new DialogManager(this);
        dialogManager.loadAll();

        DialogCommand dialogCommand = new DialogCommand(this);
        getCommand("adialogapi").setExecutor(dialogCommand);
        getCommand("adialogapi").setTabCompleter(dialogCommand);

        getServer().getPluginManager().registerEvents(new DialogListener(this), this);

        getLogger().info("ADialogAPI enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ADialogAPI disabled!");
    }

    public void debug(String message) {
        if (isDebugEnabled()) getLogger().info(message);
    }

    public boolean isDebugEnabled() {
        return isDebugEnabled;
    }

    public LangManager getLangManager() {
        return langManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public DialogManager getDialogManager() {
        return dialogManager;
    }
}
