package org.bukkit.event.hanging;

public abstract class HangingEvent extends org.bukkit.event.Event {
    protected org.bukkit.entity.Hanging hanging;

    protected HangingEvent(org.bukkit.entity.Hanging p0) {
        super();
        this.hanging = p0;
    }

    public org.bukkit.entity.Hanging getEntity() {
        return hanging;
    }
}
