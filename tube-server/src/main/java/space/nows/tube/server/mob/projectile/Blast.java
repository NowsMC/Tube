package space.nows.tube.server.mob.projectile;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import space.nows.tube.api.world.GameRule;
import space.nows.tube.server.gameplay.Explosions;
import space.nows.tube.server.world.rule.RuleBook;

public final class Blast implements Impact {

    private static final float DAMAGE = 6;

    private final float power;

    public Blast(float power) {
        this.power = power;
    }

    @Override
    public void hitEntity(Fireball fireball, LivingEntity target) {
        target.damage(new Damage(DamageType.FIREBALL, fireball, fireball.owner(), fireball.getPosition(), DAMAGE));
        explode(fireball, fireball.getPosition());
    }

    @Override
    public void hitBlock(Fireball fireball, Point block, Point before) {
        explode(fireball, before);
    }

    private void explode(Fireball fireball, Point at) {
        boolean griefing = RuleBook.enabled(fireball.getInstance(), GameRule.MOB_GRIEFING);
        Explosions.explode(fireball.getInstance(), at, power, griefing, fireball);
    }
}
