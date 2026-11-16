package org.bukkit.event.player;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.net.InetAddress;
import java.util.UUID;

public class AsyncPlayerPreLoginEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final String name;
    private final InetAddress ipAddress;
    private final UUID uniqueId;
    private final boolean transferred;
    private Result result = Result.ALLOWED;
    private String message = "";

    public AsyncPlayerPreLoginEvent(String name, InetAddress ipAddress, UUID uniqueId) {
        this(name, ipAddress, uniqueId, false);
    }

    public AsyncPlayerPreLoginEvent(String name, InetAddress ipAddress, UUID uniqueId, boolean transferred) {
        super(true);
        this.name = name;
        this.ipAddress = ipAddress;
        this.uniqueId = uniqueId;
        this.transferred = transferred;
    }

    public Result getLoginResult() {
        return result;
    }

    public void setLoginResult(Result result) {
        this.result = result;
    }

    public String getKickMessage() {
        return message;
    }

    public void setKickMessage(String message) {
        this.message = message;
    }

    public void allow() {
        result = Result.ALLOWED;
        message = "";
    }

    public void disallow(Result result, String message) {
        this.result = result;
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public InetAddress getAddress() {
        return ipAddress;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public boolean isTransferred() {
        return transferred;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public enum Result {
        ALLOWED,
        KICK_FULL,
        KICK_BANNED,
        KICK_WHITELIST,
        KICK_OTHER
    }
}
