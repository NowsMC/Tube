package space.nows.tube.bukkit.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.junit.jupiter.api.Test;

class DamageCausesTest {

    @Test
    void mapsMinecraftDamageTypes() {
        assertEquals(DamageCause.FALL, DamageCauses.fromKey("minecraft:fall"));
        assertEquals(DamageCause.ENTITY_ATTACK, DamageCauses.fromKey("minecraft:player_attack"));
        assertEquals(DamageCause.DROWNING, DamageCauses.fromKey("minecraft:drown"));
        assertEquals(DamageCause.CUSTOM, DamageCauses.fromKey("minecraft:generic"));
        assertEquals(DamageCause.CUSTOM, DamageCauses.fromKey(null));
    }
}
