package org.bukkit.plugin;

import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class PluginLogger extends Logger {

    private final String prefix;

    public PluginLogger(Plugin plugin) {
        super(plugin.getClass().getCanonicalName(), null);
        String declared = plugin.getDescription().getPrefix();
        this.prefix = "[" + (declared != null ? declared : plugin.getDescription().getName()) + "] ";
        setParent(Logger.getLogger("Minecraft"));
        setLevel(java.util.logging.Level.ALL);
    }

    @Override
    public void log(LogRecord record) {
        record.setMessage(prefix + record.getMessage());
        super.log(record);
    }
}
