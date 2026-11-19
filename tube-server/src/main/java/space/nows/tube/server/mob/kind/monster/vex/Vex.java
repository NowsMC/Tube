package space.nows.tube.server.mob.kind.monster.vex;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.brain.target.Retaliate;
import space.nows.tube.server.mob.kind.monster.Monster;

public final class Vex extends Monster {

    private static final CreatureStats STATS = CreatureStats.monster(14, 0.7).withDamage(4);

    @Override
    public EntityType type() {
        return EntityType.VEX;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        Glide glide = new Glide(creature);
        creature.addTrait(glide);
        brain.add(4, new Charge(creature, glide));
        brain.add(8, new Haunt(creature, glide, null));
        brain.add(9, WatchNearby.of(creature, entity -> entity instanceof Player, 3f));
        brain.add(10, WatchNearby.of(creature, entity -> entity instanceof Creature, 8f));
        brain.add(1, new Retaliate(creature).alertingOthers());
        brain.add(3, HuntNearest.players(creature, true));
    }
}
