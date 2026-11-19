package space.nows.tube.server.mob.brain.idle;

import net.minestom.server.coordinate.Pos;
import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.server.mob.Creature;

public final class LookAround extends Behavior {

    private static final float CHANCE = 0.02f;

    private final Creature creature;
    private double dirX;
    private double dirZ;
    private int remaining;

    public LookAround(Creature creature) {
        super(Control.MOVE, Control.LOOK);
        this.creature = creature;
    }

    @Override
    public boolean canStart() {
        return creature.random().nextFloat() < CHANCE;
    }

    @Override
    public boolean canContinue() {
        return remaining >= 0;
    }

    @Override
    public void start() {
        double angle = Math.PI * 2 * creature.random().nextDouble();
        dirX = Math.cos(angle);
        dirZ = Math.sin(angle);
        remaining = 20 + creature.random().nextInt(20);
    }

    @Override
    public boolean everyTick() {
        return true;
    }

    @Override
    public void tick() {
        remaining--;
        Pos position = creature.getPosition();
        creature.gaze().lookAt(position.x() + dirX, position.y() + creature.getEyeHeight(), position.z() + dirZ);
    }
}
