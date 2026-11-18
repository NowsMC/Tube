package space.nows.tube.bukkit.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.attribute.Attribute;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.UUID;

class MetadataAndAttributesTest {

    private static Plugin plugin() {
        return (Plugin) Proxy.newProxyInstance(MetadataAndAttributesTest.class.getClassLoader(), new Class<?>[] {Plugin.class}, (proxy, method, args) -> null);
    }

    @Test
    void metadataIsKeptPerOwnerAndPlugin() {
        MetadataStore store = new MetadataStore();
        UUID owner = UUID.randomUUID();
        Plugin first = plugin();
        Plugin second = plugin();

        store.set(owner, "vanished", new FixedMetadataValue(first, true));
        store.set(owner, "vanished", new FixedMetadataValue(second, false));
        store.set(owner, "vanished", new FixedMetadataValue(first, false));

        assertEquals(2, store.get(owner, "vanished").size());
        assertFalse(store.get(owner, "vanished").get(0).asBoolean() && store.get(owner, "vanished").get(1).asBoolean());
        store.remove(owner, "vanished", first);
        assertEquals(1, store.get(owner, "vanished").size());
        assertSame(second, store.get(owner, "vanished").get(0).getOwningPlugin());
        store.forget(owner);
        assertTrue(store.get(owner, "vanished").isEmpty());
        assertTrue(store.get(UUID.randomUUID(), "vanished").isEmpty());
    }

    @Test
    void attributesAreFoundByEitherName() {
        assertSame(Attribute.MAX_HEALTH, Attribute.GENERIC_MAX_HEALTH);
        assertSame(Attribute.MOVEMENT_SPEED, Attribute.valueOf("GENERIC_MOVEMENT_SPEED"));
        assertEquals("minecraft:max_health", Attribute.MAX_HEALTH.getKey().toString());
        assertTrue(Attribute.MAX_HEALTH.compareTo(Attribute.ARMOR) < 0);
        assertThrows(IllegalArgumentException.class, () -> Attribute.valueOf("NOTHING"));
    }
}
