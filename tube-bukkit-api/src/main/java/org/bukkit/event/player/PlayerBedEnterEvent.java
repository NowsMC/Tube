package org.bukkit.event.player;

public class PlayerBedEnterEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    private org.bukkit.block.Block bed;
    private org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult bedEnterResult;
    private org.bukkit.event.Event.Result useBed;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public PlayerBedEnterEvent(org.bukkit.entity.Player p0, org.bukkit.block.Block p1, org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult p2) {
        super(p0);
        this.useBed = org.bukkit.event.Event.Result.DEFAULT;
        this.bed = p1;
        this.bedEnterResult = p2;
    }

    public PlayerBedEnterEvent(org.bukkit.entity.Player p0, org.bukkit.block.Block p1) {
        this(p0, p1, org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult.OK);
    }

    public org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult getBedEnterResult() {
        return bedEnterResult;
    }

    public org.bukkit.event.Event.Result useBed() {
        return useBed;
    }

    public void setUseBed(org.bukkit.event.Event.Result p0) {
        this.useBed = p0;
    }

    public boolean isCancelled() {
        return useBed == org.bukkit.event.Event.Result.DENY
                || useBed == org.bukkit.event.Event.Result.DEFAULT && bedEnterResult != BedEnterResult.OK;
    }

    public void setCancelled(boolean p0) {
        setUseBed(p0 ? org.bukkit.event.Event.Result.DENY : useBed == org.bukkit.event.Event.Result.DENY ? org.bukkit.event.Event.Result.DEFAULT : useBed);
    }

    public org.bukkit.block.Block getBed() {
        return bed;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }

    public enum BedEnterResult {
        OK,
        NOT_POSSIBLE_HERE,
        NOT_POSSIBLE_NOW,
        TOO_FAR_AWAY,
        NOT_SAFE,
        OTHER_PROBLEM
    }
}
