package me.arkadarktime.aDialogAPI.dialog;

public record InputMeta(String type, String onTrue, String onFalse, String defaultValue) {

    public InputMeta(String type, String defaultValue) {
        this(type, "true", "false", defaultValue);
    }
}
