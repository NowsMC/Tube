package space.nows.tube.server.mob.kind.fish;

import space.nows.tube.api.entity.mob.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureKind;
import space.nows.tube.server.mob.Disposition;
import space.nows.tube.server.mob.brain.flee.SwimAway;
import space.nows.tube.server.mob.brain.idle.SwimAbout;
import space.nows.tube.server.mob.brain.idle.SwimPanic;
import space.nows.tube.server.mob.brain.social.School;
import space.nows.tube.server.mob.trait.Gills;

public abstract class Fish implements CreatureKind {

    private static final double SWIM_SPEED = 0.08;

    @Override
    public Disposition disposition() {
        return Disposition.PASSIVE;
    }

    protected int schoolSize() {
        return 8;
    }

    @Override
    public int despawnRange() {
        return 64;
    }

    @Override
    public boolean aquatic() {
        return true;
    }

    @Override
    public boolean ambientSounds() {
        return false;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        brain.add(0, new SwimPanic(creature, SWIM_SPEED * 1.25));
        brain.add(2, SwimAway.players(creature, 8f, SWIM_SPEED * 1.6, SWIM_SPEED * 1.4));
        brain.add(4, new SwimAbout(creature, SWIM_SPEED));
        if (schoolSize() > 0) {
            brain.add(5, new School(creature, schoolSize(), SWIM_SPEED));
        }
        creature.addTrait(new Gills(creature));
    }
}
