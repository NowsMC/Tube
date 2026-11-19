package space.nows.tube.api.event.player;

import net.kyori.adventure.resource.ResourcePackStatus;
import space.nows.tube.api.entity.player.Player;
import space.nows.tube.api.event.Event;

import java.util.UUID;

public final class PlayerResourcePackEvent implements Event {

    private final Player player;
    private final UUID packId;
    private final ResourcePackStatus status;

    public PlayerResourcePackEvent(Player player, UUID packId, ResourcePackStatus status) {
        this.player = player;
        this.packId = packId;
        this.status = status;
    }

    public Player player() {
        return player;
    }

    public UUID packId() {
        return packId;
    }

    public ResourcePackStatus status() {
        return status;
    }
}
