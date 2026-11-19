package space.nows.tube.server.mob.kind.animal;

import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.Disposition;

import java.util.Set;

public final class CamelHusk extends Grazer {

    private static final CreatureStats STATS = CreatureStats.animal(32, 0.09);

    @Override
    public EntityType type() {
        return EntityType.CAMEL_HUSK;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public Disposition disposition() {
        return Disposition.HOSTILE;
    }

    @Override
    public boolean leavesWhenFar(Creature creature) {
        return true;
    }

    @Override
    protected String foodTag() {
        return "minecraft:camel_husk_food";
    }

    @Override
    protected double pace() {
        return 2.0;
    }

    @Override
    public Set<Material> breedingFood() {
        return Set.of();
    }
}
