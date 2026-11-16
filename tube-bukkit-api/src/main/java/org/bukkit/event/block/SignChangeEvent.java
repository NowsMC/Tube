package org.bukkit.event.block;

public class SignChangeEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    private boolean cancel;
    private org.bukkit.entity.Player player;
    private java.lang.String[] lines;
    private org.bukkit.block.sign.Side side;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public SignChangeEvent(org.bukkit.block.Block p0, org.bukkit.entity.Player p1, java.lang.String[] p2) {
        this(p0, p1, p2, org.bukkit.block.sign.Side.FRONT);
    }

    public SignChangeEvent(org.bukkit.block.Block p0, org.bukkit.entity.Player p1, java.lang.String[] p2, org.bukkit.block.sign.Side p3) {
        super(p0);
        this.cancel = false;
        this.player = p1;
        this.lines = p2;
        this.side = p3;
    }

    public org.bukkit.entity.Player getPlayer() {
        return player;
    }

    public java.lang.String[] getLines() {
        return lines;
    }

    public java.lang.String getLine(int p0) {
        throw new UnsupportedOperationException("SignChangeEvent#getLine is not supported by Tube yet");
    }

    public void setLine(int p0, java.lang.String p1) {
        throw new UnsupportedOperationException("SignChangeEvent#setLine is not supported by Tube yet");
    }

    public org.bukkit.block.sign.Side getSide() {
        return side;
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
