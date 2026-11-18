package space.nows.tube.server.mob.kind.monster.creeper;

import net.minestom.server.entity.EntityType;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.ai.GoalSelector;
import space.nows.tube.server.mob.ai.combat.Fuse;
import space.nows.tube.server.mob.ai.combat.MeleeAttack;
import space.nows.tube.server.mob.ai.flee.AvoidNearby;
import space.nows.tube.server.mob.ai.idle.LookAround;
import space.nows.tube.server.mob.ai.idle.StayAfloat;
import space.nows.tube.server.mob.ai.idle.Wander;
import space.nows.tube.server.mob.ai.idle.WatchNearby;
import space.nows.tube.server.mob.ai.target.HuntNearest;
import space.nows.tube.server.mob.ai.target.Retaliate;
import space.nows.tube.server.mob.kind.monster.Monster;

public final class Creeper extends Monster {

    private static final CreatureStats STATS = CreatureStats.monster(20, 0.25);

    @Override
    public EntityType type() {
        return EntityType.CREEPER;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public boolean ambientSounds() {
        return false;
    }

    @Override
    public void configure(Creature creature, GoalSelector goals, GoalSelector targets) {
        CreeperFuse fuse = new CreeperFuse(creature);
        goals.add(1, new StayAfloat(creature));
        goals.add(2, new Fuse(creature, fuse));
        goals.add(3, AvoidNearby.type(creature, EntityType.OCELOT, 6f, 1.0, 1.2));
        goals.add(3, AvoidNearby.type(creature, EntityType.CAT, 6f, 1.0, 1.2));
        goals.add(4, new MeleeAttack(creature, 1.0, false));
        goals.add(5, Wander.onLand(creature, 0.8));
        goals.add(6, WatchNearby.players(creature, 8f));
        goals.add(6, new LookAround(creature));
        targets.add(1, HuntNearest.players(creature, true));
        targets.add(2, new Retaliate(creature));
        creature.addTrait(fuse);
    }
}
