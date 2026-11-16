package org.bukkit;

public interface Tag<T extends org.bukkit.Keyed> extends org.bukkit.Keyed {

    boolean isTagged(T p0);

    java.util.Set<T> getValues();
}
