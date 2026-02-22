package me.arkadarktime.aDialogAPI;

import org.bukkit.plugin.java.JavaPlugin;

public final class ADialogAPI extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("ADialogAPI enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ADialogAPI disabled!");
    }
}
