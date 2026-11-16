package org.bukkit.event.entity;

public class EntityTargetEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    private boolean cancel;
    private org.bukkit.entity.Entity target;
    private org.bukkit.event.entity.EntityTargetEvent.TargetReason reason;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public EntityTargetEvent(org.bukkit.entity.Entity p0, org.bukkit.entity.Entity p1, org.bukkit.event.entity.EntityTargetEvent.TargetReason p2) {
        super(p0);
        this.cancel = false;
        this.target = p1;
        this.reason = p2;
    }

    public boolean isCancelled() {
        return cancel;
    }

    public void setCancelled(boolean p0) {
        this.cancel = p0;
    }

    public org.bukkit.event.entity.EntityTargetEvent.TargetReason getReason() {
        return reason;
    }

    public org.bukkit.entity.Entity getTarget() {
        return target;
    }

    public void setTarget(org.bukkit.entity.Entity p0) {
        this.target = p0;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }

    public enum TargetReason {
        TARGET_DIED,
        CLOSEST_PLAYER,
        TARGET_ATTACKED_ENTITY,
        PIG_ZOMBIE_TARGET,
        FORGOT_TARGET,
        TARGET_ATTACKED_OWNER,
        OWNER_ATTACKED_TARGET,
        RANDOM_TARGET,
        DEFEND_VILLAGE,
        TARGET_ATTACKED_NEARBY_ENTITY,
        REINFORCEMENT_TARGET,
        COLLISION,
        CUSTOM,
        CLOSEST_ENTITY,
        FOLLOW_LEADER,
        TEMPT,
        UNKNOWN
    }
}
