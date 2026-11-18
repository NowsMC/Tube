package space.nows.tube.api.entity;

import net.kyori.adventure.text.Component;
import space.nows.tube.api.world.Location;
import space.nows.tube.api.world.World;

import java.util.Optional;
import java.util.Set;
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

    double eyeHeight();

    default Location eyeLocation() {
        Location at = location();
        return new Location(at.world(), at.x(), at.y() + eyeHeight(), at.z(), at.yaw(), at.pitch());
    }

    boolean isSneaking();

    boolean isSprinting();

    boolean isSwimming();

    boolean isGlowing();

    void setGlowing(boolean glowing);

    Set<String> tags();

    boolean addTag(String tag);

    boolean removeTag(String tag);

    void remove();
}
