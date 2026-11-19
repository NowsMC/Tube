package space.nows.tube.api.event.player;

import space.nows.tube.api.entity.player.Player;
import space.nows.tube.api.event.Event;

public record PlayerSneakEvent(Player player, boolean sneaking) implements Event {
}
