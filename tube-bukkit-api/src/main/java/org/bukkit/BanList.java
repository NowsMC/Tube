package org.bukkit;

public interface BanList<T> {

    org.bukkit.BanEntry<T> getBanEntry(java.lang.String p0);

    org.bukkit.BanEntry<T> addBan(java.lang.String p0, java.lang.String p1, java.util.Date p2, java.lang.String p3);

    java.util.Set<org.bukkit.BanEntry> getBanEntries();

    boolean isBanned(java.lang.String p0);

    void pardon(java.lang.String p0);

    public enum Type {
        NAME,
        IP,
        PROFILE
    }
}
