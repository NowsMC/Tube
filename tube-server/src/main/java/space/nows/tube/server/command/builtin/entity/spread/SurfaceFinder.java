package space.nows.tube.server.command.builtin.entity.spread;

import space.nows.tube.api.world.Block;
import space.nows.tube.api.world.Environment;
import space.nows.tube.api.world.World;

import java.util.OptionalInt;
import java.util.Set;

public final class SurfaceFinder {

    private static final Set<String> UNSAFE = Set.of("minecraft:water", "minecraft:lava", "minecraft:fire", "minecraft:magma_block");

    private final World world;

    public SurfaceFinder(World world) {
        this.world = world;
    }

    public OptionalInt standingHeight(double x, double z) {
        int blockX = (int) Math.floor(x);
        int blockZ = (int) Math.floor(z);
        Environment environment = world.options().environment();
        int top = switch (environment) {
            case OVERWORLD -> 319;
            case END -> 255;
            case NETHER -> 120;
        };
        int bottom = environment == Environment.OVERWORLD ? -64 : 1;
        for (int y = top; y >= bottom; y--) {
            Block ground = world.block(blockX, y, blockZ);
            if (ground.isAir()) {
                continue;
            }
            if (UNSAFE.contains(ground.key()) || !world.block(blockX, y + 1, blockZ).isAir() || !world.block(blockX, y + 2, blockZ).isAir()) {
                return OptionalInt.empty();
            }
            return OptionalInt.of(y + 1);
        }
        return OptionalInt.empty();
    }
}
