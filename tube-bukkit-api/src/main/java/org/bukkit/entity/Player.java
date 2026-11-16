package org.bukkit.entity;

import org.bukkit.OfflinePlayer;

import java.net.InetSocketAddress;

public interface Player extends HumanEntity, OfflinePlayer {

    String getDisplayName();

    String getPlayerListName();

    void kickPlayer(String message);

    boolean performCommand(String command);

    int getFoodLevel();

    void setFoodLevel(int level);

    int getLevel();

    int getPing();

    void sendPluginMessage(org.bukkit.plugin.Plugin source, String channel, byte[] message);

    java.util.Set<String> getListeningPluginChannels();

    void setLevel(int level);

    InetSocketAddress getAddress();

    @Override
    boolean isOnline();

    @Override
    Player getPlayer();
}
