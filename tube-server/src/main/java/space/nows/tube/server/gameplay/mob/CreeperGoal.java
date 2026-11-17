package space.nows.tube.server.gameplay.mob;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.metadata.monster.CreeperMeta;
import net.minestom.server.sound.SoundEvent;
import space.nows.tube.server.gameplay.Explosions;

final class CreeperGoal extends GoalSelector {

    private static final int FUSE_TICKS = 30;

    private Entity target;
    private int fuse = -1;

    CreeperGoal(EntityCreature creeper) {
        super(creeper);
    }

    @Override
    public boolean shouldStart() {
        target = findTarget();
        return target != null;
    }

    @Override
    public void start() {
        entityCreature.getNavigator().setPathTo(target.getPosition());
    }

    @Override
    public void tick(long time) {
        if (target == null) {
            return;
        }
        double distance = entityCreature.getPosition().distance(target.getPosition());
        CreeperMeta meta = (CreeperMeta) entityCreature.getEntityMeta();
        if (fuse < 0) {
            if (distance < 3) {
                fuse = 0;
                meta.setState(CreeperMeta.State.FUSE);
                entityCreature.getNavigator().setPathTo(null);
                entityCreature.getViewersAsAudience().playSound(Sound.sound(SoundEvent.ENTITY_CREEPER_PRIMED, Sound.Source.HOSTILE, 1f, 0.5f), entityCreature);
            } else if (entityCreature.getAliveTicks() % 10 == 0) {
                entityCreature.getNavigator().setPathTo(target.getPosition());
            }
            return;
        }
        if (distance > 7) {
            fuse = -1;
            meta.setState(CreeperMeta.State.IDLE);
            return;
        }
        if (++fuse >= FUSE_TICKS && entityCreature.getInstance() != null) {
            var instance = entityCreature.getInstance();
            var position = entityCreature.getPosition();
            entityCreature.remove();
            Explosions.explode(instance, position.add(0, 0.5, 0), meta.isCharged() ? 6 : 3, true, entityCreature);
        }
    }

    @Override
    public boolean shouldEnd() {
        return target == null || target.isRemoved() || target.getInstance() != entityCreature.getInstance()
                || target.getPosition().distance(entityCreature.getPosition()) > 24
                || target instanceof net.minestom.server.entity.LivingEntity living && living.isDead();
    }

    @Override
    public void end() {
        fuse = -1;
        target = null;
        ((CreeperMeta) entityCreature.getEntityMeta()).setState(CreeperMeta.State.IDLE);
    }
}
