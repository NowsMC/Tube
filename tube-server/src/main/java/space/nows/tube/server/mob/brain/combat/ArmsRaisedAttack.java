package space.nows.tube.server.mob.brain.combat;

import space.nows.tube.server.mob.Creature;

public final class ArmsRaisedAttack extends MeleeAttack {

    private static final int RAISE_AFTER = 5;

    private int raising;

    public ArmsRaisedAttack(Creature creature, double speed, boolean persistent) {
        super(creature, speed, persistent);
    }

    @Override
    public void start() {
        super.start();
        raising = 0;
    }

    @Override
    public void tick() {
        super.tick();
        raising++;
        creature.setAggressive(raising >= RAISE_AFTER && attackIn() < attackInterval() / 2);
    }
}
