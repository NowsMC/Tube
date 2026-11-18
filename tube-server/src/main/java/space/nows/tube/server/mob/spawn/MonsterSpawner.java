package space.nows.tube.server.mob.spawn;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureKinds;
import space.nows.tube.server.mob.sense.Daylight;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

final class MonsterSpawner {

    private static final List<EntityType> MONSTERS = List.of(EntityType.ZOMBIE, EntityType.ZOMBIE, EntityType.SKELETON,
            EntityType.SKELETON, EntityType.SPIDER, EntityType.CREEPER);
    private static final double MIN_DISTANCE = 24;
    private static final int SKY_LIGHT_LIMIT = 7;

    private MonsterSpawner() {
    }

    static void attempt(Instance instance, Player player, ThreadLocalRandom random) {
        Pos center = player.getPosition();
        double angle = random.nextDouble(Math.PI * 2);
        double distance = MIN_DISTANCE + random.nextDouble(MIN_DISTANCE);
        int x = (int) Math.floor(center.x() + Math.cos(angle) * distance);
        int z = (int) Math.floor(center.z() + Math.sin(angle) * distance);
        if (!instance.isChunkLoaded(x >> 4, z >> 4)) {
            return;
        }
        int startY = Math.min(instance.getCachedDimensionType().maxY() - 3, center.blockY() + random.nextInt(-24, 25));
        for (int y = startY; y > startY - 16 && y > instance.getCachedDimensionType().minY(); y--) {
            if (!SpawnSpots.standable(instance, x, y, z)) {
                continue;
            }
            Pos at = new Pos(x + 0.5, y, z + 0.5);
            if (instance.getBlockLight(x, y, z) > 0) {
                return;
            }
            boolean sky = Daylight.seesSky(instance, at);
            if (sky && Daylight.brightOutside(instance) || !sky && instance.getSkyLight(x, y, z) > SKY_LIGHT_LIMIT
                    || center.distanceSquared(at) < MIN_DISTANCE * MIN_DISTANCE) {
                return;
            }
            EntityType type = MONSTERS.get(random.nextInt(MONSTERS.size()));
            CreatureKinds.of(type).ifPresent(kind -> CreatureKinds.spawn(new Creature(kind), instance, at));
            return;
        }
    }
}
