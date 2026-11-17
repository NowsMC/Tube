package org.bukkit.event.entity;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class PlayerDeathEvent extends EntityDeathEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private String deathMessage;
    private boolean keepInventory;

    public PlayerDeathEvent(Player player, String deathMessage) {
        this(player, new java.util.ArrayList<>(), 0, deathMessage);
    }

    public PlayerDeathEvent(Player player, java.util.List<org.bukkit.inventory.ItemStack> drops, int droppedExp, String deathMessage) {
        super(player, drops, droppedExp);
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
