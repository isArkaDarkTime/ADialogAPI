package me.arkadarktime.aDialogAPI.actions.impl;

import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.actions.ButtonAction;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Map;

public final class TitleAction implements ButtonAction {

    private final String titleTemplate;
    private final String subtitleTemplate;
    private final int fadeIn;
    private final int stay;
    private final int fadeOut;

    public TitleAction(String titleTemplate, String subtitleTemplate, int fadeIn, int stay, int fadeOut) {
        this.titleTemplate = titleTemplate;
        this.subtitleTemplate = subtitleTemplate;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    @Override
    public String getTypeName() {
        return "TITLE";
    }

    @Override
    public void execute(Player player, Map<String, String> inputs, ADialogAPI plugin) {
        String titleText = applyPlaceholders(titleTemplate, player, inputs);
        String subtitleText = applyPlaceholders(subtitleTemplate, player, inputs);

        player.showTitle(Title.title(
                mm.deserialize(titleText),
                mm.deserialize(subtitleText),
                Title.Times.times(
                        Duration.ofMillis(fadeIn * 50L),
                        Duration.ofMillis(stay * 50L),
                        Duration.ofMillis(fadeOut * 50L)
                )
        ));
    }
}
