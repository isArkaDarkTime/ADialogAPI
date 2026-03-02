package me.arkadarktime.aDialogAPI.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public long getRemainingSeconds(UUID playerUuid, String dialogName, long cooldownSeconds) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerUuid);
        if (playerCooldowns == null) return 0;

        Long lastUsed = playerCooldowns.get(dialogName);
        if (lastUsed == null) return 0;

        long elapsed = (System.currentTimeMillis() - lastUsed) / 1000;
        long remaining = cooldownSeconds - elapsed;
        return Math.max(0, remaining);
    }

    public boolean isOnCooldown(UUID playerUuid, String dialogName, long cooldownSeconds) {
        return getRemainingSeconds(playerUuid, dialogName, cooldownSeconds) > 0;
    }

    public void setCooldown(UUID playerUuid, String dialogName) {
        cooldowns.computeIfAbsent(playerUuid, k -> new HashMap<>()).put(dialogName, System.currentTimeMillis());
    }

    public void clearCooldown(UUID playerUuid, String dialogName) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerUuid);
        if (playerCooldowns != null) {
            playerCooldowns.remove(dialogName);
        }
    }

    public void clearPlayer(UUID playerUuid) {
        cooldowns.remove(playerUuid);
    }

    public void clearAll() {
        cooldowns.clear();
    }
}
