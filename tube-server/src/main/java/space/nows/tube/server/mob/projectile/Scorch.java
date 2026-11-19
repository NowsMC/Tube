package space.nows.tube.server.mob.projectile;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import space.nows.tube.api.world.GameRule;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.world.rule.RuleBook;

public final class Scorch implements Impact {

    private static final float DAMAGE = 5;
    private static final int BURN_TICKS = 100;

    @Override
    public void hitEntity(Fireball fireball, LivingEntity target) {
        int before = target.getFireTicks();
        target.setFireTicks(Math.max(before, BURN_TICKS));
        if (!target.damage(new Damage(DamageType.FIREBALL, fireball, fireball.owner(), fireball.getPosition(), DAMAGE))) {
            target.setFireTicks(before);
        }
    }

    @Override
    public void hitBlock(Fireball fireball, Point block, Point before) {
        Instance instance = fireball.getInstance();
        if (fireball.owner() instanceof Creature && !RuleBook.enabled(instance, GameRule.MOB_GRIEFING)) {
            return;
        }
        if (instance.getBlock(before).air()) {
            instance.setBlock(before, Block.FIRE);
        }
    }
}
