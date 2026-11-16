package org.bukkit.event.player;

public class PlayerEggThrowEvent extends org.bukkit.event.player.PlayerEvent {
    private org.bukkit.entity.Egg egg;
    private boolean hatching;
    private org.bukkit.entity.EntityType hatchType;
    private byte numHatches;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public PlayerEggThrowEvent(org.bukkit.entity.Player p0, org.bukkit.entity.Egg p1, boolean p2, byte p3, org.bukkit.entity.EntityType p4) {
        super(p0);
        this.egg = p1;
        this.hatching = p2;
        this.numHatches = p3;
        this.hatchType = p4;
    }

    public org.bukkit.entity.Egg getEgg() {
        return egg;
    }

    public boolean isHatching() {
        return hatching;
    }

    public void setHatching(boolean p0) {
        this.hatching = p0;
    }

    public org.bukkit.entity.EntityType getHatchingType() {
        return hatchType;
    }

    public void setHatchingType(org.bukkit.entity.EntityType p0) {
        throw new UnsupportedOperationException("PlayerEggThrowEvent#setHatchingType is not supported by Tube yet");
    }

    public byte getNumHatches() {
        return numHatches;
    }

    public void setNumHatches(byte p0) {
        this.numHatches = p0;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}
