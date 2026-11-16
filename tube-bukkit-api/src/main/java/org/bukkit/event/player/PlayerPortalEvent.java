package org.bukkit.event.player;

public class PlayerPortalEvent extends org.bukkit.event.player.PlayerTeleportEvent {
    private int getSearchRadius;
    private boolean canCreatePortal;
    private int creationRadius;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public PlayerPortalEvent(org.bukkit.entity.Player p0, org.bukkit.Location p1, org.bukkit.Location p2) {
        super(p0, p1, p2);
        this.getSearchRadius = 128;
        this.canCreatePortal = true;
        this.creationRadius = 16;
    }

    public PlayerPortalEvent(org.bukkit.entity.Player p0, org.bukkit.Location p1, org.bukkit.Location p2, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause p3) {
        super(p0, p1, p2, p3);
        this.getSearchRadius = 128;
        this.canCreatePortal = true;
        this.creationRadius = 16;
    }

    public PlayerPortalEvent(org.bukkit.entity.Player p0, org.bukkit.Location p1, org.bukkit.Location p2, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause p3, int p4, boolean p5, int p6) {
        super(p0, p1, p2, p3);
        this.getSearchRadius = 128;
        this.canCreatePortal = true;
        this.creationRadius = 16;
        this.getSearchRadius = p4;
        this.canCreatePortal = p5;
        this.creationRadius = p6;
    }

    public void setSearchRadius(int p0) {
        this.getSearchRadius = p0;
    }

    public int getSearchRadius() {
        return getSearchRadius;
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
