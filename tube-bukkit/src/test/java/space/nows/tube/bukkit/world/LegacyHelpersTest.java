package space.nows.tube.bukkit.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

class LegacyHelpersTest {

    private static Block block(int x, int y, int z) {
        return (Block) Proxy.newProxyInstance(LegacyHelpersTest.class.getClassLoader(), new Class<?>[] {Block.class}, (proxy, method, args) -> switch (method.getName()) {
            case "getX" -> x;
            case "getY" -> y;
            case "getZ" -> z;
            case "getWorld" -> null;
            default -> method.isDefault() ? java.lang.invoke.MethodHandles.privateLookupIn(Block.class, java.lang.invoke.MethodHandles.lookup())
                    .unreflectSpecial(method, Block.class).bindTo(proxy).invokeWithArguments(args == null ? new Object[0] : args) : null;
        });
    }

    @Test
    void equipmentSlotsFollowTheItemName() {
        assertEquals(EquipmentSlot.HEAD, Material.DIAMOND_HELMET.getEquipmentSlot());
        assertEquals(EquipmentSlot.CHEST, Material.IRON_CHESTPLATE.getEquipmentSlot());
        assertEquals(EquipmentSlot.CHEST, Material.ELYTRA.getEquipmentSlot());
        assertEquals(EquipmentSlot.LEGS, Material.LEATHER_LEGGINGS.getEquipmentSlot());
        assertEquals(EquipmentSlot.FEET, Material.NETHERITE_BOOTS.getEquipmentSlot());
        assertEquals(EquipmentSlot.OFF_HAND, Material.SHIELD.getEquipmentSlot());
        assertEquals(EquipmentSlot.HAND, Material.STONE.getEquipmentSlot());
    }

    @Test
    void legacyIdsAreNotAvailableOnModernMaterials() {
        assertThrows(IllegalArgumentException.class, Material.STONE::getId);
        assertThrows(UnsupportedOperationException.class, () -> new ItemStack(Material.STONE).getTypeId());
        assertEquals(Material.STONE, new ItemStack(Material.STONE, 2, (short) 0, (byte) 1).getType());
        assertEquals(2, new ItemStack(Material.STONE, 2, (short) 0, (byte) 1).getAmount());
    }

    @Test
    void locationsSubtractEachOther() {
        Location result = new Location(null, 10, 20, 30).subtract(new Location(null, 1, 2, 3));

        assertEquals(9, result.getX());
        assertEquals(18, result.getY());
        assertEquals(27, result.getZ());
    }

    @Test
    void aBlockKnowsWhichSideAnotherBlockIsOn() {
        Block origin = block(5, 64, 5);

        assertEquals(BlockFace.UP, origin.getFace(block(5, 65, 5)));
        assertEquals(BlockFace.WEST, origin.getFace(block(4, 64, 5)));
        assertNull(origin.getFace(block(9, 64, 5)));
    }
}
