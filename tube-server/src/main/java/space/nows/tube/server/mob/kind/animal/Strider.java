package space.nows.tube.server.mob.kind.animal;

import net.minestom.server.entity.EntityType;
import space.nows.tube.server.mob.CreatureStats;

public final class Strider extends Grazer {

    private static final CreatureStats STATS = CreatureStats.animal(20, 0.175);

    @Override
    public EntityType type() {
        return EntityType.STRIDER;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    protected String foodTag() {
        return "minecraft:strider_food";
    }

    @Override
    public boolean fireproof() {
        return true;
    }
}
