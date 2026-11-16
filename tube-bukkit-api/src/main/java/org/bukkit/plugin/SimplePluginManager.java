package org.bukkit.plugin;

import org.bukkit.Server;
import org.bukkit.command.SimpleCommandMap;

public abstract class SimplePluginManager implements PluginManager {

    private final Server server;
    private final SimpleCommandMap commandMap;

    protected SimplePluginManager(Server server, SimpleCommandMap commandMap) {
        this.server = server;
        this.commandMap = commandMap;
    }

    protected Server getServer() {
        return server;
    }

    protected SimpleCommandMap getCommandMap() {
        return commandMap;
    }
}
