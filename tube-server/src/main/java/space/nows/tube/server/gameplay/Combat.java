package space.nows.tube.server.gameplay;

import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.EquipmentSlotGroup;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import net.minestom.server.world.Difficulty;
import space.nows.tube.server.gameplay.loot.LootTables;
import space.nows.tube.server.mob.KillCredit;

import java.util.Set;

final class Combat {

    private static final Tag<Long> LAST_HURT = Tag.Transient("tube:last_hurt");
    private static final Tag<Long> LAST_SWING = Tag.Transient("tube:last_swing");
    private static final long INVULNERABLE_TICKS = 10;
    private static final Set<String> BYPASSES_ARMOR = Set.of("minecraft:on_fire", "minecraft:in_wall", "minecraft:cramming", "minecraft:drown",
            "minecraft:fly_into_wall", "minecraft:generic", "minecraft:wither", "minecraft:dragon_breath", "minecraft:starve", "minecraft:fall",
            "minecraft:freeze", "minecraft:stalagmite", "minecraft:magic", "minecraft:indirect_magic", "minecraft:out_of_world",
            "minecraft:generic_kill", "minecraft:sonic_boom", "minecraft:outside_border");

    private Combat() {
    }

    static void install(GlobalEventHandler handler) {
        handler.addListener(PlayerHandAnimationEvent.class, event -> event.getPlayer().setTag(LAST_SWING, event.getPlayer().getAliveTicks()));
        handler.addListener(PlayerChangeHeldSlotEvent.class, event -> event.getPlayer().setTag(LAST_SWING, event.getPlayer().getAliveTicks()));
        handler.addListener(EntityAttackEvent.class, Combat::attack);
        handler.addListener(EntityDamageEvent.class, event -> {
            if (event.isCancelled()) {
                return;
            }
            Damage damage = event.getDamage();
            LivingEntity entity = event.getEntity();
            String type = damage.getType().key().asString();
            if (entity instanceof Player player && damage.getAttacker() != null && !(damage.getAttacker() instanceof Player)) {
                Difficulty difficulty = MinecraftServer.getDifficulty();
                float amount = damage.getAmount();
                damage.setAmount(switch (difficulty) {
                    case PEACEFUL -> 0;
                    case EASY -> Math.min(amount / 2 + 1, amount);
                    case HARD -> amount * 1.5f;
                    default -> amount;
                });
                if (damage.getAmount() <= 0) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (!BYPASSES_ARMOR.contains(type)) {
                damage.setAmount(afterArmor(entity, damage.getAmount()));
                if (entity instanceof Player) {
                    int wear = Math.max(1, (int) (damage.getAmount() / 4));
                    for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HELMET, EquipmentSlot.CHESTPLATE, EquipmentSlot.LEGGINGS, EquipmentSlot.BOOTS}) {
                        if (!entity.getEquipment(slot).isAir()) {
                            Durability.damage(entity, slot, wear);
                        }
                    }
                }
            }
            damage.setAmount(afterProtection(entity, type, damage.getAmount()));
        });
    }

    private static void attack(EntityAttackEvent event) {
        if (!(event.getTarget() instanceof LivingEntity target) || target.isDead() || !(event.getEntity() instanceof LivingEntity attacker)) {
            return;
        }
        if (target instanceof Player player && (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)) {
            return;
        }
        long now = target.getAliveTicks();
        Long last = target.getTag(LAST_HURT);
        if (last != null && now - last < INVULNERABLE_TICKS) {
            return;
        }
        float damage;
        double knockback = 0.4;
        Damage source;
        if (attacker instanceof Player player) {
            ItemStack weapon = player.getItemInMainHand();
            double charge = charge(player, weapon);
            player.setTag(LAST_SWING, player.getAliveTicks());
            damage = (float) (attackDamage(weapon) * (0.2 + charge * charge * 0.8));
            int sharpness = LootTables.level(weapon, "minecraft:sharpness");
            if (sharpness > 0) {
                damage += (float) ((0.5 * sharpness + 0.5) * charge);
            }
            boolean critical = charge > 0.9 && !player.isOnGround() && player.getVelocity().y() < 0 && !player.isSprinting() && !player.isFlying();
            if (critical) {
                damage *= 1.5f;
            }
            if (player.isSprinting() && charge > 0.9) {
                knockback += 0.5;
            }
            knockback += LootTables.level(weapon, "minecraft:knockback") * 0.5;
            int fireAspect = LootTables.level(weapon, "minecraft:fire_aspect");
            if (fireAspect > 0) {
                target.setFireTicks(fireAspect * 80);
            }
            source = Damage.fromPlayer(player, damage);
            KillCredit.hitBy(target, player);
        } else {
            damage = (float) attacker.getAttributeValue(Attribute.ATTACK_DAMAGE);
            source = Damage.fromEntity(attacker, damage);
        }
        if (target.damage(source)) {
            target.setTag(LAST_HURT, now);
            double yaw = Math.toRadians(attacker.getPosition().yaw());
            target.takeKnockback((float) knockback, Math.sin(yaw), -Math.cos(yaw));
            if (attacker instanceof Player player) {
                Hunger.exhaust(player, 0.1f);
                ItemStack weapon = player.getItemInMainHand();
                if (weapon.has(DataComponents.MAX_DAMAGE)) {
                    Durability.damage(player, EquipmentSlot.MAIN_HAND, weapon.has(DataComponents.WEAPON) ? 1 : 2);
                }
            }
        }
    }

    static double charge(Player player, ItemStack weapon) {
        Long last = player.getTag(LAST_SWING);
        if (last == null) {
            return 1;
        }
        double period = 20 / attackSpeed(weapon);
        return Math.clamp((player.getAliveTicks() - last + 0.5) / period, 0, 1);
    }

    static double attackSpeed(ItemStack weapon) {
        return 4 + modifierSum(weapon, Attribute.ATTACK_SPEED);
    }

    static double attackDamage(ItemStack weapon) {
        return 1 + modifierSum(weapon, Attribute.ATTACK_DAMAGE);
    }

    private static double modifierSum(ItemStack weapon, Attribute attribute) {
        double sum = 0;
        var modifiers = weapon.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (modifiers != null) {
            for (var modifier : modifiers.modifiers()) {
                boolean mainHand = modifier.slot() == EquipmentSlotGroup.MAIN_HAND || modifier.slot() == EquipmentSlotGroup.HAND || modifier.slot() == EquipmentSlotGroup.ANY;
                if (modifier.attribute() == attribute && mainHand && modifier.modifier().operation() == AttributeOperation.ADD_VALUE) {
                    sum += modifier.modifier().amount();
                }
            }
        }
        return sum;
    }

    static float afterArmor(LivingEntity entity, float amount) {
        double armor = entity.getAttributeValue(Attribute.ARMOR);
        double toughness = entity.getAttributeValue(Attribute.ARMOR_TOUGHNESS);
        double reduction = Math.clamp(armor - 4 * amount / (toughness + 8), armor / 5, 20);
        return (float) (amount * (1 - reduction / 25));
    }

    private static float afterProtection(LivingEntity entity, String type, float amount) {
        int points = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HELMET, EquipmentSlot.CHESTPLATE, EquipmentSlot.LEGGINGS, EquipmentSlot.BOOTS}) {
            ItemStack armor = entity.getEquipment(slot);
            if (armor.isAir()) {
                continue;
            }
            if (!type.equals("minecraft:out_of_world") && !type.equals("minecraft:starve")) {
                points += LootTables.level(armor, "minecraft:protection");
            }
            if (type.contains("fire") || type.equals("minecraft:lava") || type.equals("minecraft:hot_floor")) {
                points += 2 * LootTables.level(armor, "minecraft:fire_protection");
            }
            if (type.contains("explosion")) {
                points += 2 * LootTables.level(armor, "minecraft:blast_protection");
            }
            if (type.equals("minecraft:arrow") || type.equals("minecraft:trident") || type.equals("minecraft:mob_projectile")) {
                points += 2 * LootTables.level(armor, "minecraft:projectile_protection");
            }
            if (type.equals("minecraft:fall")) {
                points += 3 * LootTables.level(armor, "minecraft:feather_falling");
            }
        }
        return amount * (1 - Math.min(20, points) / 25f);
    }
}
