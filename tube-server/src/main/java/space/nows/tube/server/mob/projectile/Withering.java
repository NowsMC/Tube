package space.nows.tube.server.mob.projectile;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.world.Difficulty;
import space.nows.tube.api.world.GameRule;
import space.nows.tube.server.gameplay.Explosions;
import space.nows.tube.server.world.rule.RuleBook;

public final class Withering implements Impact {

    private static final float DAMAGE = 8;
    private static final int NORMAL_WITHER = 200;
    private static final int HARD_WITHER = 800;
    private static final float POWER = 1;

    @Override
    public void hitEntity(Fireball skull, LivingEntity target) {
        if (target.damage(new Damage(DamageType.WITHER_SKULL, skull, skull.owner(), skull.getPosition(), DAMAGE))) {
            Difficulty difficulty = MinecraftServer.getDifficulty();
            int duration = difficulty == Difficulty.HARD ? HARD_WITHER : difficulty == Difficulty.NORMAL ? NORMAL_WITHER : 0;
            if (duration > 0) {
                target.addEffect(new Potion(PotionEffect.WITHER, 1, duration));
            }
        }
        explode(skull, skull.getPosition());
    }

    @Override
    public void hitBlock(Fireball skull, Point block, Point before) {
        explode(skull, before);
    }

    private void explode(Fireball skull, Point at) {
        Explosions.explode(skull.getInstance(), at, POWER, RuleBook.enabled(skull.getInstance(), GameRule.MOB_GRIEFING), skull);
    }
}
