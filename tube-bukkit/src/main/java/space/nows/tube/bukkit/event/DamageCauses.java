package space.nows.tube.bukkit.event;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.util.HashMap;
import java.util.Map;

final class DamageCauses {

    private static final Map<String, DamageCause> BY_KEY = new HashMap<>();

    static {
        map(DamageCause.ENTITY_ATTACK, "player_attack", "mob_attack", "mob_attack_no_aggro");
        map(DamageCause.PROJECTILE, "arrow", "trident", "thrown", "fireball", "unattributed_fireball", "wither_skull", "mob_projectile");
        map(DamageCause.SUFFOCATION, "in_wall");
        map(DamageCause.FALL, "fall", "ender_pearl");
        map(DamageCause.FIRE, "in_fire", "campfire");
        map(DamageCause.FIRE_TICK, "on_fire");
        map(DamageCause.LAVA, "lava");
        map(DamageCause.DROWNING, "drown");
        map(DamageCause.ENTITY_EXPLOSION, "explosion", "player_explosion");
        map(DamageCause.VOID, "out_of_world");
        map(DamageCause.LIGHTNING, "lightning_bolt");
        map(DamageCause.SUICIDE, "generic_kill");
        map(DamageCause.STARVATION, "starve");
        map(DamageCause.MAGIC, "magic", "indirect_magic");
        map(DamageCause.WITHER, "wither");
        map(DamageCause.FALLING_BLOCK, "falling_block", "falling_anvil", "falling_stalactite");
        map(DamageCause.THORNS, "thorns");
        map(DamageCause.DRAGON_BREATH, "dragon_breath");
        map(DamageCause.FLY_INTO_WALL, "fly_into_wall");
        map(DamageCause.HOT_FLOOR, "hot_floor");
        map(DamageCause.CONTACT, "cactus", "sweet_berry_bush", "stalagmite");
        map(DamageCause.CRAMMING, "cramming");
        map(DamageCause.DRYOUT, "dry_out");
        map(DamageCause.FREEZE, "freeze");
        map(DamageCause.SONIC_BOOM, "sonic_boom");
    }

    private DamageCauses() {
    }

    static DamageCause fromKey(String key) {
        String name = key == null ? "" : key.replaceFirst("^minecraft:", "");
        return BY_KEY.getOrDefault(name, DamageCause.CUSTOM);
    }

    private static void map(DamageCause cause, String... keys) {
        for (String key : keys) {
            BY_KEY.put(key, cause);
        }
    }
}
