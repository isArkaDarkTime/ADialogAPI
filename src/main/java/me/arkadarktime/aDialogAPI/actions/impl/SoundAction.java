package me.arkadarktime.aDialogAPI.actions.impl;

import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.actions.ButtonAction;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;

import java.util.Map;

public final class SoundAction implements ButtonAction {

    private final String soundName;
    private final float volume;
    private final float pitch;

    public SoundAction(String soundName, float volume, float pitch) {
        this.soundName = soundName;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public String getTypeName() {
        return "SOUND";
    }

    @SuppressWarnings("PatternValidation")
    @Override
    public void execute(Player player, Map<String, String> inputs, ADialogAPI plugin) {
        try {
            Key key = Key.key(soundName.toLowerCase().replace("_", "."));
            player.playSound(Sound.sound(key, Sound.Source.MASTER, volume, pitch), Sound.Emitter.self());
        } catch ( Exception e ) {
            plugin.getLogger().warning("[SoundAction] Invalid sound '" + soundName + "': " + e.getMessage());
        }
    }
}
