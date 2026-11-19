package space.nows.tube.server.mob.kind.monster.vex;

import net.minestom.server.entity.LivingEntity;
import space.nows.tube.api.entity.mob.brain.Behavior;
import space.nows.tube.api.entity.mob.brain.Control;
import space.nows.tube.server.mob.Creature;

final class Obey extends Behavior {

    private final Creature vex;

    Obey(Creature vex) {
        super(Control.TARGET);
        this.vex = vex;
    }

    private LivingEntity ordered() {
        return vex.brain().recall(Vex.OWNER).map(Creature::target).orElse(null);
    }

    @Override
    public boolean canStart() {
        LivingEntity target = ordered();
        return target != null && target != vex && !target.isDead();
    }

    @Override
    public void start() {
        vex.setTarget(ordered());
    }
}
