package space.nows.tube.server.mob.trait;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.damage.DamageType;
import space.nows.tube.server.mob.Creature;

public final class Gills implements Trait {

    private static final int DRY_DAMAGE_INTERVAL = 20;
    private static final int FLOP_INTERVAL = 8;
    private static final double FLOP_UP = 4.0;
    private static final double FLOP_SIDE = 2.0;
    private static final float DRY_DAMAGE = 1f;

    private final Creature fish;
    private int dry;

    public Gills(Creature fish) {
        this.fish = fish;
    }

    @Override
    public void tick() {
        if (fish.isDead()) {
            return;
        }
        if (fish.inWater()) {
            dry = 0;
            fish.setNoGravity(true);
            return;
        }
        fish.setNoGravity(false);
        dry++;
        if (fish.isOnGround() && dry % FLOP_INTERVAL == 0) {
            double sideways = (fish.random().nextDouble() - 0.5) * 2 * FLOP_SIDE;
            double forward = (fish.random().nextDouble() - 0.5) * 2 * FLOP_SIDE;
            fish.setVelocity(new Vec(sideways, FLOP_UP, forward));
        }
        if (dry % DRY_DAMAGE_INTERVAL == 0) {
            fish.damage(DamageType.DROWN, DRY_DAMAGE);
        }
    }
}
