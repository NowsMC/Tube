package space.nows.tube.api;

import net.kyori.adventure.text.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Entity {

    UUID uuid();

    String type();

    World world();

    Location location();

    CompletableFuture<Void> teleport(Location location);

    Optional<Component> customName();

    void setCustomName(Component name);

    boolean isRemoved();

    void remove();
}
