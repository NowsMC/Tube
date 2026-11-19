package space.nows.tube.server.mob.kind.monster.vex;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.server.mob.Creature;

final class Haunt extends Behavior {

    private static final int CHANCE = 7;
    private static final int ATTEMPTS = 3;
    private static final double SPEED = 0.25;

    private final Creature vex;
    private final Glide glide;
    private final Point home;

    Haunt(Creature vex, Glide glide, Point home) {
        super(Control.MOVE);
        this.vex = vex;
        this.glide = glide;
        this.home = home;
    }

    @Override
    public boolean canStart() {
        return !glide.busy() && vex.random().nextInt(halved(CHANCE)) == 0;
    }

    @Override
    public boolean canContinue() {
        return false;
    }

    @Override
    public void start() {
        Point origin = home != null ? home : new Vec(vex.getPosition().blockX(), vex.getPosition().blockY(), vex.getPosition().blockZ());
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            Point spot = origin.add(vex.random().nextInt(15) - 7, vex.random().nextInt(11) - 5, vex.random().nextInt(15) - 7);
            if (vex.blockAt(spot).air()) {
                Point center = spot.add(0.5, 0.5, 0.5);
                glide.moveTo(center, SPEED);
                if (vex.target() == null) {
                    vex.gaze().lookAt(center.x(), center.y(), center.z(), 180f, 20f);
                }
                return;
            }
        }
    }
}
