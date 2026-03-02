package me.arkadarktime.aDialogAPI.models;

public final class Permissions {
    private Permissions() {
    }

    public static final String ADMIN = "adialogapi.*";

    public static final class COMMANDS {
        public static final String RELOAD = "adialogapi.command.reload";
        public static final String CREATE = "adialogapi.command.create";
        public static final String DELETE = "adialogapi.command.delete";
        public static final String SHOW = "adialogapi.command.show";
        public static final String LIST = "adialogapi.command.list";
        public static final String INFO = "adialogapi.command.info";
        public static final String HELP = "adialogapi.command.help";
    }
}
