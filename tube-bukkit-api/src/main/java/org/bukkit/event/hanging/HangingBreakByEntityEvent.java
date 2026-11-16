package org.bukkit.event.hanging;

public class HangingBreakByEntityEvent extends org.bukkit.event.hanging.HangingBreakEvent {
    private org.bukkit.entity.Entity remover;

    public HangingBreakByEntityEvent(org.bukkit.entity.Hanging p0, org.bukkit.entity.Entity p1) {
        this(p0, p1, org.bukkit.event.hanging.HangingBreakEvent.RemoveCause.ENTITY);
    }

    public HangingBreakByEntityEvent(org.bukkit.entity.Hanging p0, org.bukkit.entity.Entity p1, org.bukkit.event.hanging.HangingBreakEvent.RemoveCause p2) {
        super(p0, p2);
        this.remover = p1;
    }

    public org.bukkit.entity.Entity getRemover() {
        return remover;
    }
}
