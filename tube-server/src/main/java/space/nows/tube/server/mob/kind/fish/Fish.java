package space.nows.tube.server.mob.kind.fish;

import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureKind;
import space.nows.tube.server.mob.Disposition;
import space.nows.tube.server.mob.ai.GoalSelector;
import space.nows.tube.server.mob.ai.idle.LookAround;
import space.nows.tube.server.mob.ai.idle.SwimAbout;
import space.nows.tube.server.mob.trait.Gills;

abstract class Fish implements CreatureKind {

    private static final double SWIM_SPEED = 0.08;

    @Override
    public Disposition disposition() {
        return Disposition.PASSIVE;
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
    public void configure(Creature creature, GoalSelector goals, GoalSelector targets) {
        goals.add(0, new SwimAbout(creature, SWIM_SPEED));
        goals.add(1, new LookAround(creature));
        creature.addTrait(new Gills(creature));
    }
}
