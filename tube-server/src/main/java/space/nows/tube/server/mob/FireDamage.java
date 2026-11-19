package space.nows.tube.server.mob;

import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.registry.RegistryKey;

import java.util.Set;

public final class FireDamage {

    private static final Set<RegistryKey<DamageType>> TYPES = Set.of(DamageType.IN_FIRE, DamageType.ON_FIRE, DamageType.LAVA,
            DamageType.HOT_FLOOR, DamageType.CAMPFIRE);

    private FireDamage() {
    }

    public static boolean is(Damage damage) {
        return TYPES.contains(damage.getType());
    }
}
