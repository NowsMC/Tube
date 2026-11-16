package space.nows.tube.bukkit.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

class BukkitPlayerInventoryTest {

    private final space.nows.tube.api.ItemStack[] storage = new space.nows.tube.api.ItemStack[46];
    private final Map<space.nows.tube.api.EquipmentSlot, space.nows.tube.api.ItemStack> equipment = new EnumMap<>(space.nows.tube.api.EquipmentSlot.class);
    private int held;
    private PlayerInventory inventory;

    @BeforeEach
    void setUp() {
        java.util.Arrays.fill(storage, space.nows.tube.api.ItemStack.AIR);
        space.nows.tube.api.Inventory backing = (space.nows.tube.api.Inventory) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {space.nows.tube.api.Inventory.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "size" -> storage.length;
                    case "item" -> storage[(Integer) args[0]];
                    case "setItem" -> {
                        storage[(Integer) args[0]] = (space.nows.tube.api.ItemStack) args[1];
                        yield null;
                    }
                    case "hashCode" -> 1;
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        UUID id = UUID.randomUUID();
        space.nows.tube.api.Player player = (space.nows.tube.api.Player) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {space.nows.tube.api.Player.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "inventory" -> backing;
                    case "uuid" -> id;
                    case "heldSlot" -> held;
                    case "setHeldSlot" -> {
                        held = (Integer) args[0];
                        yield null;
                    }
                    case "equipment" -> equipment.getOrDefault((space.nows.tube.api.EquipmentSlot) args[0], space.nows.tube.api.ItemStack.AIR);
                    case "setEquipment" -> {
                        equipment.put((space.nows.tube.api.EquipmentSlot) args[0], (space.nows.tube.api.ItemStack) args[1]);
                        yield null;
                    }
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        inventory = new BukkitPlayerInventory(player, null);
    }

    @Test
    void emptySlotsAreNullAndHandsAreNever() {
        assertNull(inventory.getItem(0));
        assertTrue(inventory.getItemInMainHand().getType().isAir());
        assertEquals(41, inventory.getSize());
    }

    @Test
    void armorAndOffHandMapToEquipment() {
        inventory.setHelmet(new ItemStack(Material.DIAMOND_HELMET));
        inventory.setItemInOffHand(new ItemStack(Material.SHIELD));
        assertEquals("minecraft:diamond_helmet", equipment.get(space.nows.tube.api.EquipmentSlot.HEAD).type());
        assertEquals(Material.DIAMOND_HELMET, inventory.getItem(39).getType());
        assertEquals(Material.SHIELD, inventory.getItemInOffHand().getType());
        assertEquals(Material.DIAMOND_HELMET, inventory.getArmorContents()[3].getType());
    }

    @Test
    void mainHandFollowsHeldSlot() {
        inventory.setHeldItemSlot(4);
        inventory.setItemInMainHand(new ItemStack(Material.STONE, 5));
        assertEquals(5, storage[4].amount());
        assertEquals(Material.STONE, inventory.getItemInHand().getType());
    }

    @Test
    void addItemStacksThenFillsEmptySlots() {
        inventory.setItem(0, new ItemStack(Material.STONE, 60));
        var left = inventory.addItem(new ItemStack(Material.STONE, 10));
        assertTrue(left.isEmpty());
        assertEquals(64, inventory.getItem(0).getAmount());
        assertEquals(6, inventory.getItem(1).getAmount());
    }

    @Test
    void addItemReportsWhatDidNotFit() {
        for (int i = 0; i < 36; i++) {
            inventory.setItem(i, new ItemStack(Material.DIRT, 64));
        }
        var left = inventory.addItem(new ItemStack(Material.STONE, 3));
        assertEquals(3, left.get(0).getAmount());
    }

    @Test
    void removeItemTakesAcrossSlots() {
        inventory.setItem(0, new ItemStack(Material.STONE, 10));
        inventory.setItem(5, new ItemStack(Material.STONE, 10));
        var left = inventory.removeItem(new ItemStack(Material.STONE, 15));
        assertTrue(left.isEmpty());
        assertNull(inventory.getItem(0));
        assertEquals(5, inventory.getItem(5).getAmount());
        assertTrue(inventory.contains(Material.STONE, 5));
        assertTrue(!inventory.contains(Material.STONE, 6));
    }

    @Test
    void nameAndLoreSurviveTheRoundTrip() {
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Wand");
        meta.setLore(java.util.List.of("§7first", "second"));
        item.setItemMeta(meta);
        inventory.setItem(2, item);
        ItemMeta read = inventory.getItem(2).getItemMeta();
        assertEquals("§6Wand", read.getDisplayName());
        assertEquals(java.util.List.of("§7first", "second"), read.getLore());
    }
}
