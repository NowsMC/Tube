package space.nows.tube.server.mob.kind.flier;

import net.minestom.server.entity.EntityType;
import space.nows.tube.api.entity.mob.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.kind.monster.Monster;
import space.nows.tube.server.mob.trait.SunBurn;

public final class Phantom extends Monster {

    private static final CreatureStats STATS = CreatureStats.monster(20, 0.7).withDamage(6).withFollowRange(64);

    @Override
    public EntityType type() {
        return EntityType.PHANTOM;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        creature.addTrait(new Swoop(creature));
        creature.addTrait(new SunBurn(creature));
        brain.add(1, HuntNearest.players(creature, false));
    }
}
