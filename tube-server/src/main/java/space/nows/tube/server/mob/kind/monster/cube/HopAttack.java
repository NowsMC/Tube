package space.nows.tube.server.mob.kind.monster.cube;

import net.minestom.server.entity.LivingEntity;
import space.nows.tube.api.entity.mob.brain.Behavior;
import space.nows.tube.api.entity.mob.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.brain.target.Targeting;

final class HopAttack extends Behavior {

    private static final int TIRED_AFTER = 300;

    private final Creature cube;
    private final Cube kind;
    private final Hop hop;
    private int tiredIn;

    HopAttack(Creature cube, Cube kind, Hop hop) {
        super(Control.LOOK);
        this.cube = cube;
        this.kind = kind;
        this.hop = hop;
    }

    @Override
    public boolean canStart() {
        LivingEntity target = cube.target();
        return target != null && Targeting.combat(0).blind().test(cube, target);
    }

    @Override
    public boolean canContinue() {
        return canStart() && --tiredIn > 0;
    }

    @Override
    public void start() {
        tiredIn = halved(TIRED_AFTER);
    }

    @Override
    public boolean everyTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = cube.target();
        if (target != null) {
            cube.gaze().lookAt(target, 10f, 10f);
            double dx = target.getPosition().x() - cube.getPosition().x();
            double dz = target.getPosition().z() - cube.getPosition().z();
            hop.face((float) Math.toDegrees(Math.atan2(-dx, dz)), kind.harmful(cube));
        }
    }
}
