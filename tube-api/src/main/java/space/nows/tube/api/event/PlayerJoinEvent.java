package space.nows.tube.api.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import space.nows.tube.api.Player;

public final class PlayerJoinEvent implements Event {

    private final Player player;
    private Component joinMessage;

    public PlayerJoinEvent(Player player) {
        this.player = player;
        this.joinMessage = Component.translatable("multiplayer.player.joined", NamedTextColor.YELLOW, Component.text(player.name()));
    }

    public Component joinMessage() {
        return joinMessage;
    }

    public void setJoinMessage(Component joinMessage) {
        this.joinMessage = joinMessage;
    }

    public Player player() {
        return player;
    }
}
