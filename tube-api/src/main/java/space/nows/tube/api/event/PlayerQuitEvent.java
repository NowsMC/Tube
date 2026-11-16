package space.nows.tube.api.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import space.nows.tube.api.Player;

public final class PlayerQuitEvent implements Event {

    private final Player player;
    private Component quitMessage;

    public PlayerQuitEvent(Player player) {
        this.player = player;
        this.quitMessage = Component.translatable("multiplayer.player.left", NamedTextColor.YELLOW, Component.text(player.name()));
    }

    public Component quitMessage() {
        return quitMessage;
    }

    public void setQuitMessage(Component quitMessage) {
        this.quitMessage = quitMessage;
    }

    public Player player() {
        return player;
    }
}
