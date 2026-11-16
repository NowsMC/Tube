package org.bukkit.permissions;

import java.util.Locale;

public enum PermissionDefault {
    TRUE("true"),
    FALSE("false"),
    OP("op", "isop", "operator", "isoperator", "admin", "isadmin"),
    NOT_OP("!op", "notop", "!operator", "notoperator", "!admin", "notadmin");

    private final String[] names;

    PermissionDefault(String... names) {
        this.names = names;
    }

    public boolean getValue(boolean op) {
        return switch (this) {
            case TRUE -> true;
            case FALSE -> false;
            case OP -> op;
            case NOT_OP -> !op;
        };
    }

    public static PermissionDefault getByName(String name) {
        String lowered = name.toLowerCase(Locale.ROOT);
        for (PermissionDefault value : values()) {
            for (String candidate : value.names) {
                if (candidate.equals(lowered)) {
                    return value;
                }
            }
        }
        return null;
    }
}
