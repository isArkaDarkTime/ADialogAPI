package me.arkadarktime.aDialogAPI.dialog;

public record DialogMeta(String name, String type, String filePath, String openCommand, String openPermission,
                         boolean canEscape, int bodyCount, long cooldownSeconds) {

    public DialogMeta(String name, String type, String filePath, String openCommand, String openPermission, boolean canEscape, int bodyCount) {
        this(name, type, filePath, openCommand, openPermission, canEscape, bodyCount, 0L);
    }

    public boolean hasCooldown() {
        return cooldownSeconds > 0;
    }
}
