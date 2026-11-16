package org.bukkit.event.player;

public class PlayerPickupArrowEvent extends org.bukkit.event.player.PlayerPickupItemEvent {
    private org.bukkit.entity.AbstractArrow arrow;

    public PlayerPickupArrowEvent(org.bukkit.entity.Player p0, org.bukkit.entity.Item p1, org.bukkit.entity.AbstractArrow p2) {
        super(p0, p1, (int) 0);
        this.arrow = p2;
    }

    public org.bukkit.entity.AbstractArrow getArrow() {
        return arrow;
    }
}
