package org.bukkit;

public interface BanEntry<T> {

    java.lang.String getTarget();

    java.util.Date getExpiration();

    java.lang.String getReason();
}
