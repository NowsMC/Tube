package org.bukkit.event.server;

public class ServerListPingEvent extends org.bukkit.event.server.ServerEvent implements java.lang.Iterable<org.bukkit.entity.Player> {
    private java.lang.String hostname;
    private java.net.InetAddress address;
    private java.lang.String motd;
    private int numPlayers;
    private int maxPlayers;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public ServerListPingEvent(java.lang.String p0, java.net.InetAddress p1, java.lang.String p2, int p3, int p4) {
        super(true);
        this.hostname = p0;
        this.address = p1;
        this.motd = p2;
        this.numPlayers = p3;
        this.maxPlayers = p4;
    }

    protected ServerListPingEvent(java.lang.String p0, java.net.InetAddress p1, java.lang.String p2, int p3) {
        super(true);
        this.numPlayers = -2147483648;
        this.hostname = p0;
        this.address = p1;
        this.motd = p2;
        this.maxPlayers = p3;
    }

    public java.lang.String getHostname() {
        return hostname;
    }

    public java.net.InetAddress getAddress() {
        return address;
    }

    public java.lang.String getMotd() {
        return motd;
    }

    public void setMotd(java.lang.String p0) {
        this.motd = p0;
    }

    public int getNumPlayers() {
        throw new UnsupportedOperationException("ServerListPingEvent#getNumPlayers is not supported by Tube yet");
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public boolean shouldSendChatPreviews() {
        return false;
    }

    public void setMaxPlayers(int p0) {
        this.maxPlayers = p0;
    }

    public void setServerIcon(org.bukkit.util.CachedServerIcon p0) {
        throw new UnsupportedOperationException("ServerListPingEvent#setServerIcon is not supported by Tube yet");
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }

    public java.util.Iterator<org.bukkit.entity.Player> iterator() {
        throw new UnsupportedOperationException("ServerListPingEvent#iterator is not supported by Tube yet");
    }
}
