package org.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.permissions.ServerOperator;

import java.util.UUID;

public interface OfflinePlayer extends ServerOperator {

    boolean isOnline();

    String getName();

    UUID getUniqueId();

    boolean isBanned();

    boolean isWhitelisted();

    void setWhitelisted(boolean whitelisted);

    Player getPlayer();

    long getFirstPlayed();

    long getLastPlayed();

    boolean hasPlayedBefore();
}
