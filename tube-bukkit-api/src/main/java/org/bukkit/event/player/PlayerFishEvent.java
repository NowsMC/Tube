package org.bukkit.event.player;

public class PlayerFishEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    private org.bukkit.entity.Entity entity;
    private boolean cancel;
    private int exp;
    private org.bukkit.event.player.PlayerFishEvent.State state;
    private org.bukkit.entity.FishHook hookEntity;
    private org.bukkit.inventory.EquipmentSlot hand;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public PlayerFishEvent(org.bukkit.entity.Player p0, org.bukkit.entity.Entity p1, org.bukkit.entity.FishHook p2, org.bukkit.inventory.EquipmentSlot p3, org.bukkit.event.player.PlayerFishEvent.State p4) {
        super(p0);
        this.cancel = false;
        this.entity = p1;
        this.hookEntity = p2;
        this.hand = p3;
        this.state = p4;
    }

    public PlayerFishEvent(org.bukkit.entity.Player p0, org.bukkit.entity.Entity p1, org.bukkit.entity.FishHook p2, org.bukkit.event.player.PlayerFishEvent.State p3) {
        this(p0, p1, p2, (org.bukkit.inventory.EquipmentSlot) null, p3);
    }

    public org.bukkit.entity.Entity getCaught() {
        return entity;
    }

    public org.bukkit.entity.FishHook getHook() {
        return hookEntity;
    }

    public boolean isCancelled() {
        return cancel;
    }

    public void setCancelled(boolean p0) {
        this.cancel = p0;
    }

    public int getExpToDrop() {
        return exp;
    }

    public void setExpToDrop(int p0) {
        this.exp = p0;
    }

    public org.bukkit.inventory.EquipmentSlot getHand() {
        return hand;
    }

    public org.bukkit.event.player.PlayerFishEvent.State getState() {
        return state;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }

    public enum State {
        FISHING,
        CAUGHT_FISH,
        CAUGHT_ENTITY,
        IN_GROUND,
        FAILED_ATTEMPT,
        REEL_IN,
        BITE
    }
}
