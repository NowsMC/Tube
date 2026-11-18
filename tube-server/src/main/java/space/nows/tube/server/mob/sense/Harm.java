package space.nows.tube.server.mob.sense;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;

import java.util.Set;

public final class Harm {

    private static final long DAMAGE_MEMORY_TICKS = 40;
    private static final long ATTACKER_MEMORY_TICKS = 100;
    private static final Set<String> PANIC_CAUSES = Set.of(
            "minecraft:cactus", "minecraft:freeze", "minecraft:hot_floor", "minecraft:sulfur_cube_hot", "minecraft:in_fire",
            "minecraft:lava", "minecraft:lightning_bolt", "minecraft:on_fire", "minecraft:arrow", "minecraft:dragon_breath",
            "minecraft:explosion", "minecraft:fireball", "minecraft:fireworks", "minecraft:indirect_magic", "minecraft:magic",
            "minecraft:mob_attack", "minecraft:mob_projectile", "minecraft:player_explosion", "minecraft:sonic_boom",
            "minecraft:sting", "minecraft:thrown", "minecraft:trident", "minecraft:unattributed_fireball",
            "minecraft:wind_charge", "minecraft:wither", "minecraft:wither_skull", "minecraft:player_attack",
            "minecraft:spear");

    private String lastType;
    private long lastDamagedAt;
    private LivingEntity attacker;
    private long attackedAt;
    private int attackCount;
    private int damageCount;

    public void record(Damage damage, long now) {
        lastType = damage.getType().key().asString();
        lastDamagedAt = now;
        damageCount++;
        Entity source = damage.getAttacker();
        if (source instanceof LivingEntity living) {
            attacker = living;
            attackedAt = now;
            attackCount++;
        }
    }

    public boolean panicking(long now) {
        return lastType != null && now - lastDamagedAt <= DAMAGE_MEMORY_TICKS && PANIC_CAUSES.contains(lastType);
    }

    public LivingEntity attacker(long now) {
        if (attacker != null && (attacker.isRemoved() || now - attackedAt > ATTACKER_MEMORY_TICKS)) {
            attacker = null;
        }
        return attacker;
    }

    public int attackCount() {
        return attackCount;
    }

    public int damageCount() {
        return damageCount;
    }
}
