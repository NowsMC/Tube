package space.nows.tube.server.mob.kind.monster.cube;

import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.server.mob.Creature;

final class KeepHopping extends Behavior {

    private static final double FLOAT_SPEED = 1.2;
    private static final float FLOAT_JUMP_CHANCE = 0.8f;

    private final Creature cube;
    private final Hop hop;
    private final boolean floating;

    KeepHopping(Creature cube, Hop hop, boolean floating) {
        super(Control.JUMP, Control.MOVE);
        this.cube = cube;
        this.hop = hop;
        this.floating = floating;
    }

    @Override
    public boolean canStart() {
        return !floating || cube.inWater() || cube.inLava();
    }

    @Override
    public boolean everyTick() {
        return floating;
    }

    @Override
    public void tick() {
        if (!floating) {
            hop.want(1.0);
            return;
        }
        if (cube.random().nextFloat() < FLOAT_JUMP_CHANCE) {
            cube.jump();
        }
        hop.want(FLOAT_SPEED);
    }
}
