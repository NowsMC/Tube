package space.nows.tube.server.mob.kind.monster;

import net.minestom.server.entity.EntityType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.world.Difficulty;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.combat.ArmsRaisedAttack;
import space.nows.tube.server.mob.brain.idle.LookAround;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.brain.target.Retaliate;
import space.nows.tube.server.mob.spawn.LocalDifficulty;
import space.nows.tube.server.mob.trait.SunBurn;

import java.util.random.RandomGenerator;

public class Zombie extends Monster {

    private static final float BABY_CHANCE = 0.05f;
    private static final float WEAPON_CHANCE = 0.01f;
    private static final float HARD_WEAPON_CHANCE = 0.05f;
    private static final CreatureStats STATS = CreatureStats.monster(20, 0.23).withFollowRange(35).withDamage(3).withArmor(2);

    @Override
    public EntityType type() {
        return EntityType.ZOMBIE;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    protected boolean burnsInSun() {
        return true;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        brain.add(3, new ArmsRaisedAttack(creature, 1.0, false));
        brain.add(7, Wander.onLand(creature, 1.0));
        brain.add(8, WatchNearby.players(creature, 8f));
        brain.add(8, new LookAround(creature));
        brain.add(1, new Retaliate(creature).alertingOthers(EntityType.ZOMBIFIED_PIGLIN));
        brain.add(2, HuntNearest.players(creature, true));
        brain.add(3, HuntNearest.of(creature, entity -> entity.getEntityType() == EntityType.VILLAGER
                || entity.getEntityType() == EntityType.WANDERING_TRADER, 10, false, entity -> true));
        brain.add(3, HuntNearest.of(creature, EntityType.IRON_GOLEM, true));
        brain.add(5, Turtles.hatchlingHunt(creature));
        if (burnsInSun()) {
            creature.addTrait(new SunBurn(creature));
        }
    }

    @Override
    public void prepare(Creature creature, RandomGenerator random) {
        LocalDifficulty difficulty = LocalDifficulty.at(creature.getInstance(), 0);
        if (random.nextFloat() < BABY_CHANCE) {
            BabyZombie.make(creature);
        }
        ArmorRoll.dress(creature, difficulty, random);
        if (random.nextFloat() < (difficulty.base() == Difficulty.HARD ? HARD_WEAPON_CHANCE : WEAPON_CHANCE)) {
            int roll = random.nextInt(6);
            creature.setItemInMainHand(ItemStack.of(roll == 0 ? Material.IRON_SWORD : roll == 1 ? Material.IRON_SPEAR : Material.IRON_SHOVEL));
        }
    }
}
