package space.nows.tube.server.mob.kind.monster.hoglin;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import space.nows.tube.api.entity.brain.Activity;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.Disposition;
import space.nows.tube.server.mob.brain.combat.MeleeAttack;
import space.nows.tube.server.mob.brain.flee.AvoidNearby;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.social.Breed;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.brain.target.Retaliate;
import space.nows.tube.server.mob.interact.ItemTags;
import space.nows.tube.server.mob.kind.animal.Animal;
import space.nows.tube.server.mob.life.Lifecycle;
import space.nows.tube.server.mob.trait.Conversion;

import java.util.Set;

public final class Hoglin extends Animal {

    private static final CreatureStats STATS = CreatureStats.monster(40, 0.3).withKnockbackResistance(0.6).withDamage(6);
    private static final Set<Material> FOOD = ItemTags.of("minecraft:hoglin_food");
    private static final Set<Block> REPELLENTS = Set.of(Block.WARPED_FUNGUS, Block.NETHER_PORTAL, Block.RESPAWN_ANCHOR,
            Block.POTTED_WARPED_FUNGUS);
    private static final Activity FIGHT = Activity.of("minecraft:fight");
    private static final int REPELLENT_RANGE = 8;
    private static final int ADULT_INTERVAL = 40;
    private static final int BABY_INTERVAL = 15;

    @Override
    public EntityType type() {
        return EntityType.HOGLIN;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public Disposition disposition() {
        return Disposition.HOSTILE;
    }

    @Override
    public boolean leavesWhenFar(Creature creature) {
        return true;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        Lifecycle life = lifecycle(creature);
        creature.getAttribute(Attribute.ATTACK_KNOCKBACK).setBaseValue(1);
        creature.addTrait(new Conversion(creature, EntityType.ZOGLIN));
        brain.addActivity(FIGHT, mind -> creature.target() != null);
        brain.add(FIGHT, 1, new MeleeAttack(creature, 1.0, true, creature.isBaby() ? BABY_INTERVAL : ADULT_INTERVAL));
        brain.add(Activity.IDLE, 1, new Breed(creature, life, 0.6));
        brain.add(Activity.IDLE, 2, new AvoidNearby(creature, entity -> entity.getEntityType() == EntityType.PIGLIN,
                target -> !creature.isBaby(), 8f, 0.4, 0.4));
        brain.add(Activity.IDLE, 3, Wander.onLand(creature, 0.4));
        brain.add(Activity.IDLE, 4, WatchNearby.players(creature, 8f));
        brain.add(1, new Retaliate(creature).alertingOthers());
        brain.add(2, HuntNearest.players(creature, true).when(hoglin -> !nearRepellent(hoglin)));
    }

    private static boolean nearRepellent(Creature hoglin) {
        Point at = hoglin.getPosition();
        for (int dx = -REPELLENT_RANGE; dx <= REPELLENT_RANGE; dx += 2) {
            for (int dy = -4; dy <= 4; dy += 2) {
                for (int dz = -REPELLENT_RANGE; dz <= REPELLENT_RANGE; dz += 2) {
                    Block block = hoglin.blockAt(new Vec(at.blockX() + dx, at.blockY() + dy, at.blockZ() + dz));
                    if (REPELLENTS.contains(block.defaultState())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public float attackDamage(Creature creature) {
        return Gore.damage(creature);
    }

    @Override
    public void landedHit(Creature creature, LivingEntity target) {
        Gore.toss(creature, target);
    }

    @Override
    public Set<Material> breedingFood() {
        return FOOD;
    }
}
