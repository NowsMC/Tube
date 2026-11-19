package space.nows.tube.server.mob.brain.idle;

import net.minestom.server.coordinate.Point;
import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.move.FloatControl;

public final class FloatAround extends Behavior {

    private static final double REACHED = 1;
    private static final double TOO_FAR = 3600;
    private static final int RANGE = 16;

    private final Creature creature;
    private final FloatControl control;
    private final int range;

    public FloatAround(Creature creature, FloatControl control) {
        this(creature, control, RANGE);
    }

    public FloatAround(Creature creature, FloatControl control, int range) {
        super(Control.MOVE);
        this.creature = creature;
        this.control = control;
        this.range = range;
    }

    @Override
    public boolean canStart() {
        Point wanted = control.wanted();
        if (wanted == null) {
            return true;
        }
        double distance = wanted.distanceSquared(creature.getPosition());
        return distance < REACHED || distance > TOO_FAR;
    }

    @Override
    public boolean canContinue() {
        return false;
    }

    @Override
    public void start() {
        control.moveTo(creature.getPosition().add(offset(), offset(), offset()));
    }

    private double offset() {
        return (creature.random().nextFloat() * 2 - 1) * range;
    }
}
