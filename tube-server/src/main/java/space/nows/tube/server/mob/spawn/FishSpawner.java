package space.nows.tube.server.mob.spawn;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureKinds;
import space.nows.tube.server.mob.move.Liquids;

import java.util.concurrent.ThreadLocalRandom;

final class FishSpawner {

    private static final double MIN_DISTANCE = 16;
    private static final double EXTRA_DISTANCE = 32;
    private static final int SEARCH_HEIGHT = 24;
    private static final int MIN_DEPTH = 3;
    private static final int COD_SCHOOL = 3;
    private static final int COD_EXTRA = 4;
    private static final int SALMON_SCHOOL = 1;
    private static final int SALMON_EXTRA = 5;
    private static final int SCATTER = 3;

    private FishSpawner() {
    }

    static void attempt(Instance instance, Player player, ThreadLocalRandom random) {
        Pos center = player.getPosition();
        double angle = random.nextDouble(Math.PI * 2);
        double distance = MIN_DISTANCE + random.nextDouble(EXTRA_DISTANCE);
        int x = (int) Math.floor(center.x() + Math.cos(angle) * distance);
        int z = (int) Math.floor(center.z() + Math.sin(angle) * distance);
        if (!instance.isChunkLoaded(x >> 4, z >> 4)) {
            return;
        }
        int surface = surface(instance, x, z, center.blockY());
        if (surface == Integer.MIN_VALUE) {
            return;
        }
        int depth = depth(instance, x, surface, z);
        if (depth < MIN_DEPTH) {
            return;
        }
        int y = surface - 1 - random.nextInt(depth - 1);
        EntityType species = species(instance, x, y, z, random);
        if (species == null) {
            return;
        }
        int size = species == EntityType.COD ? COD_SCHOOL + random.nextInt(COD_EXTRA) : SALMON_SCHOOL + random.nextInt(SALMON_EXTRA);
        for (int i = 0; i < size; i++) {
            int fishX = x + random.nextInt(-SCATTER, SCATTER + 1);
            int fishY = y + random.nextInt(-1, 2);
            int fishZ = z + random.nextInt(-SCATTER, SCATTER + 1);
            if (Liquids.isWater(instance.getBlock(fishX, fishY, fishZ)) && Liquids.isWater(instance.getBlock(fishX, fishY + 1, fishZ))) {
                Creature fish = new Creature(CreatureKinds.of(species).orElseThrow());
                CreatureKinds.spawn(fish, instance, new Pos(fishX + 0.5, fishY, fishZ + 0.5, random.nextFloat() * 360, 0));
            }
        }
    }

    static EntityType species(Instance instance, int x, int y, int z, ThreadLocalRandom random) {
        String biome = instance.getBiome(x, y, z).key().value();
        if (biome.equals("river") || biome.equals("frozen_river")) {
            return EntityType.SALMON;
        }
        if (biome.contains("cold") || biome.contains("frozen")) {
            return random.nextBoolean() ? EntityType.COD : EntityType.SALMON;
        }
        return biome.contains("ocean") ? EntityType.COD : null;
    }

    private static int surface(Instance instance, int x, int z, int near) {
        int top = Math.min(instance.getCachedDimensionType().maxY() - 1, near + SEARCH_HEIGHT);
        for (int y = top; y > near - SEARCH_HEIGHT * 2; y--) {
            if (Liquids.isWater(instance.getBlock(x, y, z))) {
                return y;
            }
            if (!instance.getBlock(x, y, z).air()) {
                return Integer.MIN_VALUE;
            }
        }
        return Integer.MIN_VALUE;
    }

    private static int depth(Instance instance, int x, int surface, int z) {
        int depth = 0;
        for (int y = surface; Liquids.isWater(instance.getBlock(x, y, z)); y--) {
            depth++;
        }
        return depth;
    }
}
