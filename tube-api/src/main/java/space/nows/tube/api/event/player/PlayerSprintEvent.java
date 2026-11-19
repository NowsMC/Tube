package space.nows.tube.api.event.player;

import space.nows.tube.api.entity.player.Player;
import space.nows.tube.api.event.Event;

public record PlayerSprintEvent(Player player, boolean sprinting) implements Event {
}
