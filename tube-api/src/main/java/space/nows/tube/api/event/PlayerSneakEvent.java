package space.nows.tube.api.event;

import space.nows.tube.api.entity.Player;

public record PlayerSneakEvent(Player player, boolean sneaking) implements Event {
}
