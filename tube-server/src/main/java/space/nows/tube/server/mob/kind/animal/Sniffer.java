package space.nows.tube.server.mob.kind.animal;

import net.minestom.server.entity.EntityType;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;

public final class Sniffer extends Grazer {

    private static final CreatureStats STATS = CreatureStats.animal(14, 0.1);

    @Override
    public EntityType type() {
        return EntityType.SNIFFER;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    protected String foodTag() {
        return "minecraft:sniffer_food";
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        super.configure(creature, brain);
        brain.add(4, new Sniff(creature));
    }
}
