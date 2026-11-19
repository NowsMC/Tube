package space.nows.tube.server.mob.kind.animal;

import net.minestom.server.entity.EntityType;
import space.nows.tube.server.mob.CreatureStats;

public final class Camel extends Grazer {

    private static final CreatureStats STATS = CreatureStats.animal(32, 0.09);

    @Override
    public EntityType type() {
        return EntityType.CAMEL;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    protected String foodTag() {
        return "minecraft:camel_food";
    }

    @Override
    protected double pace() {
        return 2.0;
    }
}
