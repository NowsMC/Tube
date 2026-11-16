package org.bukkit.event.entity;

public class EntityPortalEvent extends org.bukkit.event.entity.EntityTeleportEvent {
    private int searchRadius;
    private boolean canCreatePortal;
    private int creationRadius;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public EntityPortalEvent(org.bukkit.entity.Entity p0, org.bukkit.Location p1, org.bukkit.Location p2) {
        super(p0, p1, p2);
        this.searchRadius = 128;
        this.canCreatePortal = true;
        this.creationRadius = 16;
    }

    public EntityPortalEvent(org.bukkit.entity.Entity p0, org.bukkit.Location p1, org.bukkit.Location p2, int p3) {
        super(p0, p1, p2);
        this.searchRadius = 128;
        this.canCreatePortal = true;
        this.creationRadius = 16;
        this.searchRadius = p3;
    }

    public EntityPortalEvent(org.bukkit.entity.Entity p0, org.bukkit.Location p1, org.bukkit.Location p2, int p3, boolean p4, int p5) {
        super(p0, p1, p2);
        this.searchRadius = 128;
        this.canCreatePortal = true;
        this.creationRadius = 16;
        this.searchRadius = p3;
        this.canCreatePortal = p4;
        this.creationRadius = p5;
    }

    public void setSearchRadius(int p0) {
        this.searchRadius = p0;
    }

    public int getSearchRadius() {
        return searchRadius;
    }

    public boolean getCanCreatePortal() {
        return canCreatePortal;
    }

    public void setCanCreatePortal(boolean p0) {
        this.canCreatePortal = p0;
    }

    public void setCreationRadius(int p0) {
        this.creationRadius = p0;
    }

    public int getCreationRadius() {
        return creationRadius;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}
