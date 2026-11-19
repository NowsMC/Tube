package space.nows.tube.server.mob.kind.monster.spider;

import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.brain.combat.MeleeAttack;

final class SpiderBite extends MeleeAttack {

    private static final int GIVE_UP_ONE_IN = 100;

    SpiderBite(Creature spider) {
        super(spider, 1.0, true);
    }

    @Override
    public boolean canStart() {
        return super.canStart() && creature.getPassengers().isEmpty();
    }

    @Override
    public boolean canContinue() {
        if (!Spiders.dark(creature) && creature.random().nextInt(GIVE_UP_ONE_IN) == 0) {
            creature.setTarget(null);
            return false;
        }
        return super.canContinue();
    }
}
