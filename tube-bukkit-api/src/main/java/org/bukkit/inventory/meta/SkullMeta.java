package org.bukkit.inventory.meta;

public interface SkullMeta extends org.bukkit.inventory.meta.ItemMeta {

    java.lang.String getOwner();

    boolean hasOwner();

    boolean setOwner(java.lang.String p0);

    boolean setOwningPlayer(org.bukkit.OfflinePlayer p0);

    org.bukkit.profile.PlayerProfile getOwnerProfile();

    void setOwnerProfile(org.bukkit.profile.PlayerProfile p0);
}
