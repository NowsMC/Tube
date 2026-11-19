package space.nows.tube.server.mob.kind.monster.spider;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.world.Difficulty;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;

public final class CaveSpider extends Spider {

    private static final CreatureStats STATS = CreatureStats.monster(12, 0.3);
    private static final int NORMAL_POISON_SECONDS = 7;
    private static final int HARD_POISON_SECONDS = 15;

    @Override
    public EntityType type() {
        return EntityType.CAVE_SPIDER;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public String voice() {
        return "spider";
    }

    @Override
    public void landedHit(Creature creature, LivingEntity target) {
        Difficulty difficulty = MinecraftServer.getDifficulty();
        int seconds = difficulty == Difficulty.HARD ? HARD_POISON_SECONDS : difficulty == Difficulty.NORMAL ? NORMAL_POISON_SECONDS : 0;
        if (seconds > 0) {
            target.addEffect(new Potion(PotionEffect.POISON, 0, seconds * 20));
        }
    }
}
