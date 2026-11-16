package space.nows.tube.api;

import java.util.UUID;

public interface Player extends CommandSender {

    UUID uuid();

    World world();

    Location location();

    void teleport(Location location);

    void kick(String reason);
}
