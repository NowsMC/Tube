package space.nows.tube.server.mob.kind.monster.illager;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.flee.AvoidNearby;
import space.nows.tube.server.mob.brain.idle.WatchNearby;

public final class Evoker extends Illager {

    private static final CreatureStats STATS = CreatureStats.monster(24, 0.5).withFollowRange(12);

    @Override
    public EntityType type() {
        return EntityType.EVOKER;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    protected int caution() {
        return 3;
    }

    @Override
    protected boolean seesVillagers() {
        return false;
    }

    @Override
    protected void behaviors(Creature creature, Brain brain) {
        Spellbook book = new Spellbook(creature);
        creature.addTrait(book);
        brain.add(1, new Casting(creature, book));
        brain.add(2, new AvoidNearby(creature, entity -> entity instanceof Player, target -> true, 8f, 0.6, 1.0));
        brain.add(4, new SummonSpell(creature, book));
        brain.add(5, new FangSpell(creature, book));
        brain.add(6, new WololoSpell(creature, book));
        brain.add(9, WatchNearby.of(creature, entity -> entity instanceof Player, 3f));
    }
}
