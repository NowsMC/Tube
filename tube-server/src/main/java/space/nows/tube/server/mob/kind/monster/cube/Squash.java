package space.nows.tube.server.mob.kind.monster.cube;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.sound.MobVoice;
import space.nows.tube.server.mob.trait.Trait;

final class Squash implements Trait {

    private static final double TOUCH = 0.1;

    private final Creature cube;
    private final Cube kind;

    Squash(Creature cube, Cube kind) {
        this.cube = cube;
        this.kind = kind;
    }

    @Override
    public void tick() {
        if (cube.isDead() || !kind.harmful(cube)) {
            return;
        }
        double reach = cube.getBoundingBox().width() + 1;
        for (Entity entity : cube.getInstance().getNearbyEntities(cube.getPosition(), reach)) {
            if (!touchable(entity) || !cube.getBoundingBox().expand(TOUCH, TOUCH, TOUCH).intersectEntity(cube.getPosition(), entity)
                    || !cube.sight().canSee((LivingEntity) entity)) {
                continue;
            }
            if (((LivingEntity) entity).damage(Damage.fromEntity(cube, kind.squashDamage(cube)))) {
                MobVoice.play(cube, "attack");
            }
        }
    }

    private static boolean touchable(Entity entity) {
        if (entity instanceof Player player) {
            return !player.isDead() && player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR;
        }
        return entity.getEntityType() == EntityType.IRON_GOLEM && entity instanceof LivingEntity living && !living.isDead();
    }
}
