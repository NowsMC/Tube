package space.nows.tube.server.mob.kind.monster.vex;

import net.minestom.server.entity.damage.DamageType;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.trait.Trait;

final class Fading implements Trait {

    private static final int AFTER_LIFE = 20;

    private final Creature vex;

    Fading(Creature vex) {
        this.vex = vex;
    }

    @Override
    public void tick() {
        vex.brain().recall(Vex.LIFE).ifPresent(left -> {
            if (left - 1 <= 0) {
                vex.brain().remember(Vex.LIFE, AFTER_LIFE);
                vex.damage(DamageType.STARVE, 1);
            } else {
                vex.brain().remember(Vex.LIFE, left - 1);
            }
        });
    }
}
