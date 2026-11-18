package org.bukkit.attribute;

public interface Attribute extends org.bukkit.util.OldEnum<org.bukkit.attribute.Attribute>, org.bukkit.Keyed, org.bukkit.Translatable, org.bukkit.registry.RegistryAware {

    Attribute MAX_HEALTH = SimpleAttribute.of("max_health");
    Attribute FOLLOW_RANGE = SimpleAttribute.of("follow_range");
    Attribute KNOCKBACK_RESISTANCE = SimpleAttribute.of("knockback_resistance");
    Attribute MOVEMENT_SPEED = SimpleAttribute.of("movement_speed");
    Attribute ATTACK_DAMAGE = SimpleAttribute.of("attack_damage");
    Attribute ATTACK_SPEED = SimpleAttribute.of("attack_speed");
    Attribute ARMOR = SimpleAttribute.of("armor");
    Attribute ARMOR_TOUGHNESS = SimpleAttribute.of("armor_toughness");
    Attribute LUCK = SimpleAttribute.of("luck");
    Attribute MAX_ABSORPTION = SimpleAttribute.of("max_absorption");
    Attribute SCALE = SimpleAttribute.of("scale");
    Attribute STEP_HEIGHT = SimpleAttribute.of("step_height");
    Attribute GRAVITY = SimpleAttribute.of("gravity");
    Attribute JUMP_STRENGTH = SimpleAttribute.of("jump_strength");
    Attribute FLYING_SPEED = SimpleAttribute.of("flying_speed");
    Attribute SAFE_FALL_DISTANCE = SimpleAttribute.of("safe_fall_distance");
    Attribute BLOCK_INTERACTION_RANGE = SimpleAttribute.of("block_interaction_range");
    Attribute ENTITY_INTERACTION_RANGE = SimpleAttribute.of("entity_interaction_range");
    Attribute GENERIC_MAX_HEALTH = MAX_HEALTH;
    Attribute GENERIC_FOLLOW_RANGE = FOLLOW_RANGE;
    Attribute GENERIC_KNOCKBACK_RESISTANCE = KNOCKBACK_RESISTANCE;
    Attribute GENERIC_MOVEMENT_SPEED = MOVEMENT_SPEED;
    Attribute GENERIC_ATTACK_DAMAGE = ATTACK_DAMAGE;
    Attribute GENERIC_ATTACK_SPEED = ATTACK_SPEED;
    Attribute GENERIC_ARMOR = ARMOR;
    Attribute GENERIC_ARMOR_TOUGHNESS = ARMOR_TOUGHNESS;
    Attribute GENERIC_LUCK = LUCK;
    Attribute GENERIC_MAX_ABSORPTION = MAX_ABSORPTION;
    Attribute GENERIC_FLYING_SPEED = FLYING_SPEED;
    Attribute GENERIC_JUMP_STRENGTH = JUMP_STRENGTH;

    static Attribute valueOf(String name) {
        return SimpleAttribute.byName(name);
    }
}
