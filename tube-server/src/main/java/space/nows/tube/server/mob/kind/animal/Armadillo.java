package space.nows.tube.server.mob.kind.animal;

import net.minestom.server.entity.EntityType;
import space.nows.tube.server.mob.CreatureStats;

public final class Armadillo extends Grazer {

    private static final CreatureStats STATS = CreatureStats.animal(12, 0.14);

    @Override
    public EntityType type() {
        return EntityType.ARMADILLO;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    protected String foodTag() {
        return "minecraft:armadillo_food";
    }
}
