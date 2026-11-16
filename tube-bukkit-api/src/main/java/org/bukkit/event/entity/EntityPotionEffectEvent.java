package org.bukkit.event.entity;

public class EntityPotionEffectEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    private boolean cancel;
    private org.bukkit.potion.PotionEffect oldEffect;
    private org.bukkit.potion.PotionEffect newEffect;
    private org.bukkit.event.entity.EntityPotionEffectEvent.Cause cause;
    private org.bukkit.event.entity.EntityPotionEffectEvent.Action action;
    private boolean override;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public EntityPotionEffectEvent(org.bukkit.entity.LivingEntity p0, org.bukkit.potion.PotionEffect p1, org.bukkit.potion.PotionEffect p2, org.bukkit.event.entity.EntityPotionEffectEvent.Cause p3, org.bukkit.event.entity.EntityPotionEffectEvent.Action p4, boolean p5) {
        super(p0);
        this.oldEffect = p1;
        this.newEffect = p2;
        this.cause = p3;
        this.action = p4;
        this.override = p5;
    }

    public org.bukkit.potion.PotionEffect getOldEffect() {
        return oldEffect;
    }

    public org.bukkit.potion.PotionEffect getNewEffect() {
        return newEffect;
    }

    public org.bukkit.event.entity.EntityPotionEffectEvent.Cause getCause() {
        return cause;
    }

    public org.bukkit.event.entity.EntityPotionEffectEvent.Action getAction() {
        return action;
    }

    public org.bukkit.potion.PotionEffectType getModifiedType() {
        throw new UnsupportedOperationException("EntityPotionEffectEvent#getModifiedType is not supported by Tube yet");
    }

    public boolean isOverride() {
        return override;
    }

    public void setOverride(boolean p0) {
        this.override = p0;
    }

    public boolean isCancelled() {
        return cancel;
    }

    public void setCancelled(boolean p0) {
        this.cancel = p0;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }

    public enum Cause {
        AREA_EFFECT_CLOUD,
        ARROW,
        ATTACK,
        AXOLOTL,
        BEACON,
        COMMAND,
        CONDUIT,
        CONVERSION,
        DEATH,
        DOLPHIN,
        EXPIRATION,
        FOOD,
        ILLUSION,
        MILK,
        PATROL_CAPTAIN,
        PLUGIN,
        POTION_DRINK,
        POTION_SPLASH,
        SPIDER_SPAWN,
        TOTEM,
        TURTLE_HELMET,
        UNKNOWN,
        VILLAGER_TRADE,
        WARDEN,
        WITHER_ROSE
    }

    public enum Action {
        ADDED,
        CHANGED,
        CLEARED,
        REMOVED
    }
}
