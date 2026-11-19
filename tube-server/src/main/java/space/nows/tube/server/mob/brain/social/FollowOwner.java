package space.nows.tube.server.mob.brain.social;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import space.nows.tube.api.entity.mob.brain.Behavior;
import space.nows.tube.api.entity.mob.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.life.Tameness;

public final class FollowOwner extends Behavior {

    private static final double TELEPORT_FROM = 144;
    private static final int REPATH = 10;
    private static final int TELEPORT_TRIES = 10;

    private final Creature pet;
    private final Tameness tameness;
    private final double speed;
    private final double startDistance;
    private final double stopDistance;
    private Player owner;
    private int repathIn;

    public FollowOwner(Creature pet, Tameness tameness, double speed, double startDistance, double stopDistance) {
        super(Control.MOVE, Control.LOOK);
        this.pet = pet;
        this.tameness = tameness;
        this.speed = speed;
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
    }

    @Override
    public boolean canStart() {
        Player candidate = tameness.owner();
        if (candidate == null || candidate.getGameMode() == GameMode.SPECTATOR || tameness.sitting()
                || pet.getPosition().distanceSquared(candidate.getPosition()) < startDistance * startDistance) {
            return false;
        }
        owner = candidate;
        return true;
    }

    @Override
    public boolean canContinue() {
        return owner != null && !tameness.sitting() && !pet.navigation().isDone()
                && pet.getPosition().distanceSquared(owner.getPosition()) > stopDistance * stopDistance;
    }

    @Override
    public void start() {
        repathIn = 0;
    }

    @Override
    public void stop() {
        owner = null;
        pet.navigation().stop();
    }

    @Override
    public void tick() {
        pet.gaze().lookAt(owner, 10f, 30f);
        if (--repathIn > 0) {
            return;
        }
        repathIn = ticks(REPATH);
        if (pet.getPosition().distanceSquared(owner.getPosition()) >= TELEPORT_FROM) {
            teleportNear();
        } else {
            pet.navigation().moveTo(owner, speed);
        }
    }

    private void teleportNear() {
        Pos at = owner.getPosition();
        for (int i = 0; i < TELEPORT_TRIES; i++) {
            int dx = pet.random().nextInt(-3, 4);
            int dz = pet.random().nextInt(-3, 4);
            if (Math.abs(dx) < 2 && Math.abs(dz) < 2) {
                continue;
            }
            Pos spot = new Pos(at.blockX() + dx + 0.5, at.blockY(), at.blockZ() + dz + 0.5);
            if (pet.blockAt(spot.add(0, -1, 0)).solid() && pet.blockAt(spot).air() && pet.blockAt(spot.add(0, 1, 0)).air()) {
                pet.teleport(spot.withView(pet.getPosition().yaw(), pet.getPosition().pitch()));
                pet.navigation().stop();
                return;
            }
        }
    }
}
