package space.nows.tube.server.mob.kind.ambient;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.ambient.BatMeta;
import net.minestom.server.instance.Instance;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.trait.Trait;

final class Roost implements Trait {

    private static final double TICKS_PER_SECOND = 20;
    private static final double WAKE_RANGE = 4;
    private static final double FALL_DAMPING = 0.6;

    private final Creature bat;
    private boolean resting;
    private Point wanted;

    Roost(Creature bat) {
        this.bat = bat;
        bat.setNoGravity(true);
    }

    @Override
    public void tick() {
        Instance instance = bat.getInstance();
        if (bat.isDead() || instance == null) {
            return;
        }
        Pos at = bat.getPosition();
        Point above = at.add(0, 1, 0);
        if (resting) {
            bat.setVelocity(Vec.ZERO);
            if (!bat.blockAt(above).solid() || playerNear(instance)) {
                hang(false);
            }
            return;
        }
        if (wanted != null && (!bat.blockAt(wanted).air() || wanted.y() <= instance.getCachedDimensionType().minY())) {
            wanted = null;
        }
        if (wanted == null || bat.random().nextInt(30) == 0 || wanted.add(0.5, 0.5, 0.5).distanceSquared(at) < 4) {
            wanted = new Vec(Math.floor(at.x() + bat.random().nextInt(7) - bat.random().nextInt(7)),
                    Math.floor(at.y() + bat.random().nextInt(6) - 2.0),
                    Math.floor(at.z() + bat.random().nextInt(7) - bat.random().nextInt(7)));
        }
        Vec motion = bat.getVelocity().div(TICKS_PER_SECOND);
        double dx = wanted.x() + 0.5 - at.x();
        double dy = wanted.y() + 0.1 - at.y();
        double dz = wanted.z() + 0.5 - at.z();
        Vec next = motion.add((Math.signum(dx) * 0.5 - motion.x()) * 0.1, (Math.signum(dy) * 0.7 - motion.y()) * 0.1,
                (Math.signum(dz) * 0.5 - motion.z()) * 0.1);
        next = next.withY(next.y() * FALL_DAMPING);
        bat.setVelocity(next.mul(TICKS_PER_SECOND));
        bat.setView((float) Math.toDegrees(-Math.atan2(next.x(), next.z())), at.pitch());
        if (bat.random().nextInt(100) == 0 && bat.blockAt(above).solid()) {
            hang(true);
        }
    }

    private boolean playerNear(Instance instance) {
        for (Player player : instance.getPlayers()) {
            if (player.getGameMode() != GameMode.SPECTATOR && player.getPosition().distanceSquared(bat.getPosition()) < WAKE_RANGE * WAKE_RANGE) {
                return true;
            }
        }
        return false;
    }

    private void hang(boolean hanging) {
        resting = hanging;
        if (bat.getEntityMeta() instanceof BatMeta meta) {
            meta.setHanging(hanging);
        }
        if (hanging) {
            Pos at = bat.getPosition();
            bat.teleport(at.withY(Math.floor(at.y()) + 1.0 - bat.getBoundingBox().height()));
        }
    }
}
