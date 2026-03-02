package me.arkadarktime.aDialogAPI.models;

public record ButtonAction(Type type, String value) {
    public ButtonAction(Type type) {
        this(type, "");
    }

    public enum Type {
        RUN_COMMAND,
        CONSOLE_COMMAND,
        MESSAGE,
        BROADCAST,
        SHOW_DIALOG,
        GIVE_ITEM,
        TAKE_ITEM,
        TITLE,
        SOUND,
        POTION_EFFECT,
        XP,
        CLOSE;

        public static Type fromString(String raw) {
            return switch (raw.toLowerCase()) {
                case "player_command", "command", "player", "run_command" -> RUN_COMMAND;
                case "console_command", "console" -> CONSOLE_COMMAND;
                case "send_message", "message" -> MESSAGE;
                case "broadcast", "broadcast_message" -> BROADCAST;
                case "show_dialog", "open_dialog" -> SHOW_DIALOG;
                case "give_item", "giveitem", "give", "item" -> GIVE_ITEM;
                case "take_item", "takeitem", "remove_item" -> TAKE_ITEM;
                case "title", "send_title" -> TITLE;
                case "sound", "play_sound" -> SOUND;
                case "potion_effect", "effect", "give_effect" -> POTION_EFFECT;
                case "xp", "give_xp", "experience" -> XP;
                case "close" -> CLOSE;
                default -> null;
            };
        }
    }
}