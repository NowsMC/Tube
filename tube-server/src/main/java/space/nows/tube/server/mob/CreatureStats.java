package space.nows.tube.server.mob;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;

public record CreatureStats(double health, double speed, double damage, double followRange, double armor,
                            double knockbackResistance, double temptRange) {

    private static final double DEFAULT_FOLLOW_RANGE = 16;
    private static final double MONSTER_DAMAGE = 2;
    private static final double ANIMAL_TEMPT_RANGE = 10;

    public static CreatureStats mob(double health, double speed) {
        return new CreatureStats(health, speed, 0, DEFAULT_FOLLOW_RANGE, 0, 0, 0);
    }

    public static CreatureStats monster(double health, double speed) {
        return mob(health, speed).withDamage(MONSTER_DAMAGE);
    }

    public static CreatureStats animal(double health, double speed) {
        return mob(health, speed).withTemptRange(ANIMAL_TEMPT_RANGE);
    }

    public CreatureStats withDamage(double value) {
        return new CreatureStats(health, speed, value, followRange, armor, knockbackResistance, temptRange);
    }

    public CreatureStats withFollowRange(double value) {
        return new CreatureStats(health, speed, damage, value, armor, knockbackResistance, temptRange);
    }

    public CreatureStats withArmor(double value) {
        return new CreatureStats(health, speed, damage, followRange, value, knockbackResistance, temptRange);
    }

    public CreatureStats withKnockbackResistance(double value) {
        return new CreatureStats(health, speed, damage, followRange, armor, value, temptRange);
    }

    public CreatureStats withTemptRange(double value) {
        return new CreatureStats(health, speed, damage, followRange, armor, knockbackResistance, value);
    }

    void applyTo(LivingEntity entity) {
        entity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(health);
        entity.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(speed);
        entity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
        entity.getAttribute(Attribute.FOLLOW_RANGE).setBaseValue(followRange);
        entity.getAttribute(Attribute.ARMOR).setBaseValue(armor);
        entity.getAttribute(Attribute.KNOCKBACK_RESISTANCE).setBaseValue(knockbackResistance);
        entity.getAttribute(Attribute.TEMPT_RANGE).setBaseValue(temptRange);
        entity.setHealth((float) health);
    }
}
