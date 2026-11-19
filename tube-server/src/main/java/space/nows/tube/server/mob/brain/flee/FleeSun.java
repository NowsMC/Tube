package space.nows.tube.server.mob.brain.flee;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.instance.Instance;
import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.sense.Daylight;

public final class FleeSun extends Behavior {

    private static final int ATTEMPTS = 10;

    private final Creature creature;
    private final double speed;
    private Point shade;

    public FleeSun(Creature creature, double speed) {
        super(Control.MOVE);
        this.creature = creature;
        this.speed = speed;
    }

    @Override
    public boolean canStart() {
        Instance instance = creature.getInstance();
        if (creature.target() != null || !Daylight.brightOutside(instance) || creature.getFireTicks() <= 0
                || !Daylight.seesSky(instance, creature.getPosition()) || !creature.getEquipment(EquipmentSlot.HELMET).isAir()) {
            return false;
        }
        shade = findShade(instance);
        return shade != null;
    }

    private Point findShade(Instance instance) {
        Point origin = creature.getPosition();
        for (int i = 0; i < ATTEMPTS; i++) {
            Point at = new Vec(origin.blockX() + creature.random().nextInt(20) - 10, origin.blockY() + creature.random().nextInt(6) - 3,
                    origin.blockZ() + creature.random().nextInt(20) - 10);
            if (!Daylight.seesSky(instance, at) && creature.walkPreference(at) < 0) {
                return at.add(0.5, 0, 0.5);
            }
        }
        return null;
    }

    @Override
    public boolean canContinue() {
        return !creature.navigation().isDone();
    }

    @Override
    public void start() {
        creature.navigation().moveTo(shade, speed);
    }
}
