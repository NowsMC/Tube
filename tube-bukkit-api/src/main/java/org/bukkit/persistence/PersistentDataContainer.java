package org.bukkit.persistence;

public interface PersistentDataContainer {

    <P, C> void set(org.bukkit.NamespacedKey p0, org.bukkit.persistence.PersistentDataType<P, C> p1, C p2);

    <P, C> boolean has(org.bukkit.NamespacedKey p0, org.bukkit.persistence.PersistentDataType<P, C> p1);

    <P, C> C get(org.bukkit.NamespacedKey p0, org.bukkit.persistence.PersistentDataType<P, C> p1);

    <P, C> C getOrDefault(org.bukkit.NamespacedKey p0, org.bukkit.persistence.PersistentDataType<P, C> p1, C p2);

    void remove(org.bukkit.NamespacedKey p0);
}
