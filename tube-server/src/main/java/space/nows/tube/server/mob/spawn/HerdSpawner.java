package space.nows.tube.server.mob.spawn;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureKind;
import space.nows.tube.server.mob.CreatureKinds;
import space.nows.tube.server.mob.life.Lifecycle;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

final class HerdSpawner {

    private static final List<EntityType> ANIMALS = List.of(EntityType.COW, EntityType.PIG, EntityType.SHEEP, EntityType.CHICKEN);
    private static final double MIN_DISTANCE = 24;
    private static final double EXTRA_DISTANCE = 40;
    private static final int SEARCH_HEIGHT = 32;
    private static final float BABY_CHANCE = 0.05f;

    private HerdSpawner() {
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
        int startY = Math.min(instance.getCachedDimensionType().maxY() - 3, center.blockY() + SEARCH_HEIGHT);
        for (int y = startY; y > center.blockY() - SEARCH_HEIGHT; y--) {
            Block ground = instance.getBlock(x, y - 1, z);
            if (ground.air()) {
                continue;
            }
            if (!ground.compare(Block.GRASS_BLOCK) || !SpawnSpots.standable(instance, x, y, z)) {
                return;
            }
            CreatureKind kind = CreatureKinds.of(ANIMALS.get(random.nextInt(ANIMALS.size()))).orElseThrow();
            int group = 2 + random.nextInt(3);
            for (int i = 0; i < group; i++) {
                int dx = x + random.nextInt(-2, 3);
                int dz = z + random.nextInt(-2, 3);
                if (SpawnSpots.standable(instance, dx, y, dz)) {
                    Creature creature = new Creature(kind);
                    boolean baby = i > 0 && random.nextFloat() <= BABY_CHANCE;
                    CreatureKinds.spawn(creature, instance, new Pos(dx + 0.5, y, dz + 0.5, random.nextFloat() * 360, 0))
                            .thenRun(() -> {
                                if (baby) {
                                    creature.trait(Lifecycle.class).ifPresent(Lifecycle::makeBaby);
                                }
                            });
                }
            }
            return;
        }
    }
}
