package space.nows.tube.api.event;

import space.nows.tube.api.entity.Player;

public record PlayerSprintEvent(Player player, boolean sprinting) implements Event {
}
