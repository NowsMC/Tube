package space.nows.tube.server.mob;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import space.nows.tube.server.mob.kind.animal.Chicken;
import space.nows.tube.server.mob.kind.animal.Cow;
import space.nows.tube.server.mob.kind.animal.Pig;
import space.nows.tube.server.mob.kind.animal.Sheep;
import space.nows.tube.server.mob.kind.monster.Zombie;
import space.nows.tube.server.mob.kind.monster.creeper.Creeper;
import space.nows.tube.server.mob.kind.monster.enderman.Enderman;
import space.nows.tube.server.mob.kind.monster.skeleton.Skeleton;
import space.nows.tube.server.mob.kind.monster.spider.Spider;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public final class CreatureKinds {

    private static final Map<EntityType, CreatureKind> KINDS = new HashMap<>();

    static {
        for (CreatureKind kind : List.of(new Cow(), new Pig(), new Sheep(), new Chicken(),
                new Zombie(), new Skeleton(), new Spider(), new Creeper(), new Enderman())) {
            KINDS.put(kind.type(), kind);
        }
    }

    private CreatureKinds() {
    }

    public static Optional<CreatureKind> of(EntityType type) {
        return Optional.ofNullable(KINDS.get(type));
    }

    public static Collection<CreatureKind> all() {
        return KINDS.values();
    }

    public static CompletableFuture<Void> spawn(Creature creature, Instance instance, Pos at) {
        return creature.setInstance(instance, at).thenRun(() -> creature.kind().prepare(creature, ThreadLocalRandom.current()));
    }
}
