package space.nows.tube.server.mob.kind.animal;

import net.minestom.server.entity.EntityType;
import space.nows.tube.server.mob.CreatureStats;

public final class Panda extends Grazer {

    private static final CreatureStats STATS = CreatureStats.animal(20, 0.15).withDamage(6);

    @Override
    public EntityType type() {
        return EntityType.PANDA;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    protected String foodTag() {
        return "minecraft:panda_food";
    }
}
