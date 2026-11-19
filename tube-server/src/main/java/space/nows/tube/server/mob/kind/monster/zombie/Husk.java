package space.nows.tube.server.mob.kind.monster.zombie;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.spawn.LocalDifficulty;

public final class Husk extends Zombie {

    private static final int HUNGER_TICKS_PER_DIFFICULTY = 140;

    @Override
    public EntityType type() {
        return EntityType.HUSK;
    }

    @Override
    protected boolean burnsInSun() {
        return false;
    }

    @Override
    public void landedHit(Creature creature, LivingEntity target) {
        if (!creature.getItemInMainHand().isAir()) {
            return;
        }
        int difficulty = (int) LocalDifficulty.at(creature.getInstance(), 0).effective();
        if (difficulty > 0) {
            target.addEffect(new Potion(PotionEffect.HUNGER, 0, HUNGER_TICKS_PER_DIFFICULTY * difficulty));
        }
    }
}
