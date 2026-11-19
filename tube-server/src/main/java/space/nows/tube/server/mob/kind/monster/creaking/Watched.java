package space.nows.tube.server.mob.kind.monster.creaking;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.monster.CreakingMeta;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.trait.Trait;

final class Watched implements Trait {

    private static final double RANGE = 24;
    private static final double TOLERANCE = 0.5;

    private final Creature creaking;
    private boolean frozen;

    Watched(Creature creaking) {
        this.creaking = creaking;
    }

    @Override
    public void tick() {
        boolean watched = watched();
        if (watched == frozen) {
            return;
        }
        frozen = watched;
        creaking.brain().setEnabled(Control.MOVE, !watched);
        creaking.brain().setEnabled(Control.LOOK, !watched);
        if (watched) {
            creaking.navigation().stop();
        }
        if (creaking.getEntityMeta() instanceof CreakingMeta meta) {
            meta.setCanMove(!watched);
        }
    }

    private boolean watched() {
        for (Player player : creaking.getInstance().getPlayers()) {
            if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR
                    || player.getPosition().distanceSquared(creaking.getPosition()) > RANGE * RANGE) {
                continue;
            }
            Pos eye = player.getPosition().add(0, player.getEyeHeight(), 0);
            Vec toward = creaking.getPosition().add(0, creaking.getBoundingBox().height() / 2, 0).sub(eye).asVec();
            if (toward.length() < 1.0E-6) {
                continue;
            }
            if (player.getPosition().direction().normalize().dot(toward.normalize()) > TOLERANCE && player.hasLineOfSight(creaking, false)) {
                return true;
            }
        }
        return false;
    }
}
