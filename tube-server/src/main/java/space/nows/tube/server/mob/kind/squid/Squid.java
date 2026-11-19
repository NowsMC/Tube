package space.nows.tube.server.mob.kind.squid;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.Damage;
import space.nows.tube.api.entity.mob.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureKind;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.Disposition;
import space.nows.tube.server.mob.sound.MobVoice;

public class Squid implements CreatureKind {

    private static final CreatureStats STATS = CreatureStats.mob(10, 0.7);

    @Override
    public EntityType type() {
        return EntityType.SQUID;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public Disposition disposition() {
        return Disposition.PASSIVE;
    }

    @Override
    public boolean aquatic() {
        return true;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        Pulse pulse = new Pulse(creature);
        creature.addTrait(pulse);
        brain.add(0, new Drift(creature, pulse));
        brain.add(1, new Ink(creature, pulse));
    }

    @Override
    public void damaged(Creature creature, Damage damage) {
        MobVoice.play(creature, "squirt");
    }
}
