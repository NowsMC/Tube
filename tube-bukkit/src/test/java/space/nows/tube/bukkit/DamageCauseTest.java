package space.nows.tube.bukkit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.junit.jupiter.api.Test;

class DamageCauseTest {

    @Test
    void mapsMinecraftDamageTypes() {
        assertEquals(DamageCause.FALL, BukkitEventBridge.cause("minecraft:fall"));
        assertEquals(DamageCause.ENTITY_ATTACK, BukkitEventBridge.cause("minecraft:player_attack"));
        assertEquals(DamageCause.DROWNING, BukkitEventBridge.cause("minecraft:drown"));
        assertEquals(DamageCause.CUSTOM, BukkitEventBridge.cause("minecraft:generic"));
        assertEquals(DamageCause.CUSTOM, BukkitEventBridge.cause(null));
    }
}
