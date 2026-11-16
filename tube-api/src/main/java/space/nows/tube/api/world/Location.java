package space.nows.tube.api.world;

import java.util.Objects;

public record Location(World world, double x, double y, double z, float yaw, float pitch) {

    public Location {
        Objects.requireNonNull(world, "world");
    }

    public Location(World world, double x, double y, double z) {
        this(world, x, y, z, 0f, 0f);
    }
}
