package space.nows.tube.server.mob.kind.monster.cube;

import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.server.mob.Creature;

final class HopAbout extends Behavior {

    private final Creature cube;
    private final Hop hop;
    private float chosen;
    private int nextTurn;

    HopAbout(Creature cube, Hop hop) {
        super(Control.LOOK);
        this.cube = cube;
        this.hop = hop;
    }

    @Override
    public boolean canStart() {
        return cube.target() == null && (cube.isOnGround() || cube.inWater() || cube.inLava());
    }

    @Override
    public void tick() {
        if (--nextTurn <= 0) {
            nextTurn = ticks(40 + cube.random().nextInt(60));
            chosen = cube.random().nextInt(360);
        }
        hop.face(chosen, false);
    }
}
