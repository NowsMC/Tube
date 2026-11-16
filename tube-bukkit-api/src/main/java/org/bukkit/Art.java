package org.bukkit;

public interface Art extends org.bukkit.util.OldEnum<org.bukkit.Art>, org.bukkit.Keyed, org.bukkit.registry.RegistryAware {

    int getBlockWidth();

    int getBlockHeight();

    int getId();
}
