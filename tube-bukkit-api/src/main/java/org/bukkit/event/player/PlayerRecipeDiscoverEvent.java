package org.bukkit.event.player;

public class PlayerRecipeDiscoverEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    private boolean cancel;
    private org.bukkit.NamespacedKey recipe;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public PlayerRecipeDiscoverEvent(org.bukkit.entity.Player p0, org.bukkit.NamespacedKey p1) {
        super(p0);
        this.cancel = false;
        this.recipe = p1;
    }

    public org.bukkit.NamespacedKey getRecipe() {
        return recipe;
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
}
