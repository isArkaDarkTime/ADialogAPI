package me.arkadarktime.aDialogAPI.config;

public final class Messages {
    private Messages() {
    }

    public static final String PREFIX = "prefix";

    public static final class Errors {
        private Errors() {
        }

        public static final String NO_PERMISSION = "errors.no-permission";
        public static final String UNKNOWN_SUBCOMMAND = "errors.unknown-subcommand";
        public static final String PLAYER_ONLY = "errors.player-only";
        public static final String TARGET_NOT_FOUND = "errors.target-not-found";
        public static final String ON_COOLDOWN = "errors.on-cooldown";
        public static final String GENERIC = "errors.generic";
    }

    public static final class Commands {
        private Commands() {
        }

        public static final class Dialog {
            private Dialog() {
            }

            public static final String Help = "commands.dialog.help";

            public static final class Reload {
                private Reload() {
                }

                public static final String SUCCESS = "commands.dialog.reload.success";
                public static final String SUCCESS_DIALOG = "commands.dialog.reload.success-dialog";
                public static final String NOT_FOUND = "commands.dialog.reload.not-found";
            }

            public static final class Create {
                private Create() {
                }

                public static final String USAGE = "commands.dialog.create.usage";
                public static final String SUCCESS = "commands.dialog.create.success";
                public static final String ALREADY_EXISTS = "commands.dialog.create.already-exists";
            }

            public static final class Delete {
                private Delete() {
                }

                public static final String USAGE = "commands.dialog.delete.usage";
                public static final String SUCCESS = "commands.dialog.delete.success";
                public static final String NOT_FOUND = "commands.dialog.delete.not-found";
            }

            public static final class Show {
                private Show() {
                }

                public static final String USAGE = "commands.dialog.show.usage";
                public static final String SUCCESS = "commands.dialog.show.success";
                public static final String NOT_FOUND = "commands.dialog.show.not-found";
            }

            public static final class List {
                private List() {
                }

                public static final String HEADER = "commands.dialog.list.header";
                public static final String ROW = "commands.dialog.list.row";
                public static final String FOOTER = "commands.dialog.list.footer";
                public static final String EMPTY = "commands.dialog.list.empty";

                public static final class CommandEntry {
                    private CommandEntry() {
                    }

                    public static final String COMMAND = "commands.dialog.list.command-entry.command";
                    public static final String EMPTY = "commands.dialog.list.command-entry.empty";
                }

                public static final class PermissionEntry {
                    private PermissionEntry() {
                    }

                    public static final String PERMISSION = "commands.dialog.list.permission-entry.permission";
                    public static final String EMPTY = "commands.dialog.list.permission-entry.empty";
                }
            }

            public static final class Info {
                private Info() {
                }

                public static final String USAGE = "commands.dialog.info.usage";
                public static final String NOT_FOUND = "commands.dialog.info.not-found";
                public static final String HEADER = "commands.dialog.info.header";
                public static final String ROW = "commands.dialog.info.row";
                public static final String FOOTER = "commands.dialog.info.footer";
                public static final String VALUE_YES = "commands.dialog.info.values.true";
                public static final String VALUE_NO = "commands.dialog.info.values.false";
                public static final String VALUE_EMPTY = "commands.dialog.info.values.empty";
            }
        }
    }
}