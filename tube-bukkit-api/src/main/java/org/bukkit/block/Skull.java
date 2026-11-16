package org.bukkit.block;

public interface Skull extends org.bukkit.block.TileState {

    org.bukkit.OfflinePlayer getOwningPlayer();

    void setOwningPlayer(org.bukkit.OfflinePlayer p0);
}
