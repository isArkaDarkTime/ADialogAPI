package me.arkadarktime.aDialogAPI.actions.impl;

import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.actions.ButtonAction;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

public final class PotionEffectAction extends ButtonAction {

    private final PotionEffectType effectType;
    private final int duration;
    private final int amplifier;

    public PotionEffectAction(PotionEffectType effectType, int duration, int amplifier) {
        this.effectType = effectType;
        this.duration = duration;
        this.amplifier = amplifier;
    }

    @Override
    public String getTypeName() {
        return "POTION_EFFECT";
    }

    @Override
    public void execute(Player player, Map<String, String> inputs, ADialogAPI plugin) {
        player.addPotionEffect(new PotionEffect(effectType, duration, amplifier));
    }
}
