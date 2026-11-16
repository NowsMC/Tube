package org.bukkit.plugin;

public class IllegalPluginAccessException extends RuntimeException {

    public IllegalPluginAccessException() {
    }

    public IllegalPluginAccessException(String message) {
        super(message);
    }
}
