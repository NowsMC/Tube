package org.bukkit.metadata;

public interface MetadataValue {

    java.lang.Object value();

    boolean asBoolean();

    org.bukkit.plugin.Plugin getOwningPlugin();
}
