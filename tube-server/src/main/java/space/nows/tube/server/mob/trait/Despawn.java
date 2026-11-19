package space.nows.tube.server.mob.trait;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.world.Difficulty;
import space.nows.tube.server.mob.Creature;

public final class Despawn implements Trait {

    private static final double CALM_RANGE = 32;
    private static final int IDLE_BEFORE_WANDERING_OFF = 600;
    private static final int WANDER_OFF_ONE_IN = 800;

    private final Creature creature;

    public Despawn(Creature creature) {
        this.creature = creature;
    }

    @Override
    public void tick() {
        if (creature.isDead() || creature.isRemoved()) {
            return;
        }
        if (creature.kind().disposition().hostile() && MinecraftServer.getDifficulty() == Difficulty.PEACEFUL) {
            creature.remove();
            return;
        }
        if (creature.isPersistent()) {
            creature.resetIdle();
            return;
        }
        double nearest = nearestPlayer();
        if (Double.isNaN(nearest)) {
            return;
        }
        boolean leaves = creature.kind().leavesWhenFar(creature);
        double range = creature.kind().despawnRange();
        if (leaves && nearest > range * range) {
            creature.remove();
            return;
        }
        if (nearest < CALM_RANGE * CALM_RANGE) {
            creature.resetIdle();
        } else if (leaves && creature.idleTicks() > IDLE_BEFORE_WANDERING_OFF && creature.random().nextInt(WANDER_OFF_ONE_IN) == 0) {
            creature.remove();
        }
    }

    private double nearestPlayer() {
        Instance instance = creature.getInstance();
        double nearest = Double.NaN;
        for (Player player : instance.getPlayers()) {
            if (player.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }
            double distance = player.getPosition().distanceSquared(creature.getPosition());
            if (Double.isNaN(nearest) || distance < nearest) {
                nearest = distance;
            }
        }
        return nearest;
    }
}
