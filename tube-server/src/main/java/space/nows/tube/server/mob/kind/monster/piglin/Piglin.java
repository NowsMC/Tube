package space.nows.tube.server.mob.kind.monster.piglin;

import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.combat.CrossbowAttack;
import space.nows.tube.server.mob.brain.combat.CrossbowShot;
import space.nows.tube.server.mob.brain.combat.MeleeAttack;
import space.nows.tube.server.mob.brain.flee.AvoidNearby;
import space.nows.tube.server.mob.brain.idle.LookAround;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.brain.target.Retaliate;
import space.nows.tube.server.mob.kind.monster.Monster;
import space.nows.tube.server.mob.sense.Grudge;
import space.nows.tube.server.mob.trait.Conversion;

import java.util.Set;
import java.util.random.RandomGenerator;

public class Piglin extends Monster {

    private static final CreatureStats STATS = CreatureStats.monster(16, 0.35).withDamage(5);
    private static final Set<Material> GOLD_ARMOR = Set.of(Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS,
            Material.GOLDEN_BOOTS);
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HELMET, EquipmentSlot.CHESTPLATE, EquipmentSlot.LEGGINGS, EquipmentSlot.BOOTS};
    private static final Material[] GOLD = {Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS};
    private static final float ARMOR_CHANCE = 0.1f;
    private static final int MIN_ANGER = 400;
    private static final int MAX_ANGER = 780;

    @Override
    public EntityType type() {
        return EntityType.PIGLIN;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    static boolean wearsGold(LivingEntity entity) {
        for (EquipmentSlot slot : ARMOR) {
            if (GOLD_ARMOR.contains(entity.getEquipment(slot).material())) {
                return true;
            }
        }
        return false;
    }

    protected boolean spares(LivingEntity player) {
        return wearsGold(player);
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        Grudge grudge = new Grudge(creature, MIN_ANGER, MAX_ANGER);
        creature.addTrait(grudge);
        creature.addTrait(Conversion.zombify(creature, EntityType.ZOMBIFIED_PIGLIN));
        brain.add(1, new AvoidNearby(creature, entity -> entity.getEntityType() == EntityType.ZOMBIFIED_PIGLIN
                || entity.getEntityType() == EntityType.ZOGLIN, target -> true, 8f, 1.0, 1.2));
        brain.add(2, new CrossbowAttack(creature, 1.0, 8f, new CrossbowShot()));
        brain.add(3, new MeleeAttack(creature, 1.0, true));
        brain.add(5, Wander.onLand(creature, 0.6));
        brain.add(6, WatchNearby.players(creature, 8f));
        brain.add(7, new LookAround(creature));
        brain.add(1, new Retaliate(creature).alertingOthers());
        brain.add(2, HuntNearest.players(creature, true, target -> grudge.holds(target) || !spares(target)));
        brain.add(3, HuntNearest.of(creature, EntityType.HOGLIN, true).when(piglin -> !piglin.isBaby()));
    }

    @Override
    public void damaged(Creature creature, Damage damage) {
        if (damage.getAttacker() instanceof Player attacker) {
            creature.trait(Grudge.class).ifPresent(grudge -> grudge.provoke(attacker));
        }
    }

    @Override
    public void prepare(Creature creature, RandomGenerator random) {
        creature.setItemInMainHand(ItemStack.of(random.nextBoolean() ? Material.CROSSBOW : Material.GOLDEN_SWORD));
        for (int i = 0; i < ARMOR.length; i++) {
            if (random.nextFloat() < ARMOR_CHANCE) {
                creature.setEquipment(ARMOR[i], ItemStack.of(GOLD[i]));
            }
        }
    }
}
