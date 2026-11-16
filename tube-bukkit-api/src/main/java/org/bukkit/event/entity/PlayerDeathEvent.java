package org.bukkit.event.entity;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class PlayerDeathEvent extends EntityDeathEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private String deathMessage;
    private boolean keepInventory;

    public PlayerDeathEvent(Player player, String deathMessage) {
        super(player);
        this.deathMessage = deathMessage;
    }

    @Override
    public Player getEntity() {
        return (Player) entity;
    }

    public String getDeathMessage() {
        return deathMessage;
    }

    public void setDeathMessage(String deathMessage) {
        this.deathMessage = deathMessage;
    }

    public boolean getKeepInventory() {
        return keepInventory;
    }

    public void setKeepInventory(boolean keepInventory) {
        this.keepInventory = keepInventory;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
