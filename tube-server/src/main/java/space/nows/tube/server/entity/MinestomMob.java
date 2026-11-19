package space.nows.tube.server.entity;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.metadata.MobMeta;
import space.nows.tube.api.entity.Entity;
import space.nows.tube.api.entity.LivingEntity;
import space.nows.tube.api.entity.Mob;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.api.world.Location;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.world.WorldService;

import java.util.Optional;

public final class MinestomMob extends MinestomLivingEntity implements Mob {

    private final Creature creature;

    public MinestomMob(Creature creature, WorldService worlds) {
        super(creature, worlds);
        this.creature = creature;
    }

    @Override
    public Creature minestom() {
        return creature;
    }

    @Override
    public Brain brain() {
        return creature.brain();
    }

    @Override
    public Optional<LivingEntity> target() {
        net.minestom.server.entity.LivingEntity target = creature.target();
        return target == null ? Optional.empty() : Optional.of((LivingEntity) worlds.wrap(target));
    }

    @Override
    public void setTarget(LivingEntity target) {
        creature.setTarget(target == null ? null : handleOf(target));
    }

    @Override
    public boolean moveTo(Location location, double speed) {
        return creature.navigation().moveTo(new Vec(location.x(), location.y(), location.z()), speed);
    }

    @Override
    public boolean moveTo(Entity entity, double speed) {
        return creature.navigation().moveTo(handleOf(entity), speed);
    }

    @Override
    public void stopMoving() {
        creature.navigation().stop();
    }

    @Override
    public boolean isMoving() {
        return !creature.navigation().isDone();
    }

    @Override
    public void lookAt(Entity entity) {
        creature.gaze().lookAt(handleOf(entity));
    }

    @Override
    public void lookAt(Location location) {
        creature.gaze().lookAt(location.x(), location.y(), location.z());
    }

    @Override
    public void attack(Entity target) {
        creature.strike(handleOf(target));
    }

    @Override
    public boolean isBaby() {
        return creature.isBaby();
    }

    @Override
    public boolean isAggressive() {
        return creature.getEntityMeta() instanceof MobMeta meta && meta.isAggressive();
    }

    @Override
    public void setAggressive(boolean aggressive) {
        creature.setAggressive(aggressive);
    }

    private static net.minestom.server.entity.Entity handleOf(Entity entity) {
        if (entity instanceof MinestomEntity wrapped) {
            return wrapped.minestom();
        }
        throw new IllegalArgumentException("Not a Tube entity: " + entity);
    }

    private static net.minestom.server.entity.LivingEntity handleOf(LivingEntity entity) {
        if (handleOf((Entity) entity) instanceof net.minestom.server.entity.LivingEntity living) {
            return living;
        }
        throw new IllegalArgumentException("Not a living entity: " + entity);
    }
}
