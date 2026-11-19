package space.nows.tube.server.mob.kind.monster.creeper;

import net.minestom.server.entity.EntityType;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.combat.Fuse;
import space.nows.tube.server.mob.brain.combat.MeleeAttack;
import space.nows.tube.server.mob.brain.flee.AvoidNearby;
import space.nows.tube.server.mob.brain.idle.LookAround;
import space.nows.tube.server.mob.brain.idle.StayAfloat;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.brain.target.Retaliate;
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
    public void configure(Creature creature, Brain brain) {
        CreeperFuse fuse = new CreeperFuse(creature);
        brain.add(1, new StayAfloat(creature));
        brain.add(2, new Fuse(creature, fuse));
        brain.add(3, AvoidNearby.type(creature, EntityType.OCELOT, 6f, 1.0, 1.2));
        brain.add(3, AvoidNearby.type(creature, EntityType.CAT, 6f, 1.0, 1.2));
        brain.add(4, new MeleeAttack(creature, 1.0, false));
        brain.add(5, Wander.onLand(creature, 0.8));
        brain.add(6, WatchNearby.players(creature, 8f));
        brain.add(6, new LookAround(creature));
        brain.add(1, HuntNearest.players(creature, true));
        brain.add(2, new Retaliate(creature));
        creature.addTrait(fuse);
    }
}
