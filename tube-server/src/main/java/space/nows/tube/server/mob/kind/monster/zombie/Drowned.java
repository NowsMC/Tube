package space.nows.tube.server.mob.kind.monster.zombie;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.brain.combat.ArmsRaisedAttack;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.brain.target.Retaliate;
import space.nows.tube.server.mob.kind.monster.Turtles;
import space.nows.tube.server.mob.move.Liquids;
import space.nows.tube.server.mob.sense.Daylight;
import space.nows.tube.server.mob.spawn.LocalDifficulty;

import java.util.random.RandomGenerator;

public final class Drowned extends Zombie {

    private static final float ARMED_CHANCE = 0.1f;

    @Override
    public EntityType type() {
        return EntityType.DROWNED;
    }

    @Override
    public boolean aquatic() {
        return true;
    }

    @Override
    protected void behaviors(Creature creature, Brain brain) {
        brain.add(1, new SeekWater(creature));
        brain.add(2, new Dive(creature));
        brain.add(2, new ArmsRaisedAttack(creature, 1.0, false));
        brain.add(6, new Surface(creature));
        brain.add(7, Wander.anywhere(creature, 1.0));
        brain.add(1, new Retaliate(creature, EntityType.DROWNED).alertingOthers(EntityType.ZOMBIFIED_PIGLIN));
        brain.add(2, HuntNearest.players(creature, true, target -> fair(creature, target)));
        brain.add(3, HuntNearest.of(creature, entity -> entity.getEntityType() == EntityType.VILLAGER
                || entity.getEntityType() == EntityType.WANDERING_TRADER, 10, false, entity -> true));
        brain.add(3, HuntNearest.of(creature, EntityType.IRON_GOLEM, true));
        brain.add(3, HuntNearest.of(creature, EntityType.AXOLOTL, true));
        brain.add(5, Turtles.hatchlingHunt(creature));
    }

    private static boolean fair(Creature drowned, LivingEntity target) {
        return !Daylight.brightOutside(drowned.getInstance()) || Liquids.isWater(drowned.blockAt(target.getPosition()));
    }

    @Override
    protected void equip(Creature creature, LocalDifficulty difficulty, RandomGenerator random) {
        if (random.nextFloat() > 1 - ARMED_CHANCE) {
            creature.setItemInMainHand(ItemStack.of(random.nextInt(16) < 10 ? Material.TRIDENT : Material.FISHING_ROD));
        }
    }
}
