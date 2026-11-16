package org.bukkit.event.server;

import org.bukkit.event.Event;

public abstract class ServerEvent extends Event {

    public ServerEvent() {
        super();
    }

    public ServerEvent(boolean isAsync) {
        super(isAsync);
    }
}
