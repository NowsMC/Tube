package space.nows.tube.server.mob.trait;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import space.nows.tube.server.mob.Creature;

public final class Crowding implements Trait {

    private static final double SEARCH_RADIUS = 2;
    private static final double MIN_DISTANCE = 0.01;
    private static final double TICKS_PER_SECOND = 20;
    private static final double STRENGTH = 0.05 * TICKS_PER_SECOND;

    private final Creature creature;

    public Crowding(Creature creature) {
        this.creature = creature;
    }

    @Override
    public void tick() {
        if (creature.isDead() || creature.getInstance() == null) {
            return;
        }
        for (Entity other : creature.getInstance().getNearbyEntities(creature.getPosition(), SEARCH_RADIUS)) {
            if (other != creature && pushable(other) && creature.getBoundingBox().intersectEntity(creature.getPosition(), other)) {
                push(other);
            }
        }
    }

    private static boolean pushable(Entity entity) {
        return entity instanceof LivingEntity living && !living.isDead() && !(entity instanceof Player player && player.getGameMode() == GameMode.SPECTATOR);
    }

    private void push(Entity other) {
        double dx = other.getPosition().x() - creature.getPosition().x();
        double dz = other.getPosition().z() - creature.getPosition().z();
        double distance = Math.max(Math.abs(dx), Math.abs(dz));
        if (distance < MIN_DISTANCE) {
            double angle = creature.random().nextDouble() * Math.PI * 2;
            dx = Math.cos(angle);
            dz = Math.sin(angle);
            distance = 1;
        }
        distance = Math.sqrt(distance);
        double scale = Math.min(1, 1 / distance) / distance * STRENGTH;
        Vec push = new Vec(dx * scale, 0, dz * scale);
        creature.setVelocity(creature.getVelocity().sub(push));
        if (!(other instanceof Player)) {
            other.setVelocity(other.getVelocity().add(push));
        }
    }
}
