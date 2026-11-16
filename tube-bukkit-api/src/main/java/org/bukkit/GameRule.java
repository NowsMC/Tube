package org.bukkit;

public interface GameRule<T> extends org.bukkit.Keyed, org.bukkit.registry.RegistryAware {

    java.lang.String getName();

    java.lang.Class<T> getType();
}
