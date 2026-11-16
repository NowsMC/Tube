package space.nows.tube.bukkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class BlockDataTest {

    @Test
    void parsesAndPrintsProperties() {
        BukkitBlockData data = BukkitBlockData.parse("minecraft:oak_stairs[half=top,facing=north]");

        assertEquals(Material.OAK_STAIRS, data.getMaterial());
        assertEquals("minecraft:oak_stairs[facing=north,half=top]", data.getAsString());
        assertTrue(data.matches(BukkitBlockData.parse("oak_stairs[facing=north,half=top]")));
    }

    @Test
    void rejectsUnknownBlocks() {
        assertThrows(IllegalArgumentException.class, () -> BukkitBlockData.parse("minecraft:not_a_block"));
        assertThrows(IllegalArgumentException.class, () -> BukkitBlockData.parse("stone[broken"));
    }
}
