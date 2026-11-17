package space.nows.tube.server.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.component.DataComponents;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import net.minestom.server.tag.Tag;

import java.util.List;

class SurvivalRulesTest {

    @BeforeAll
    static void initMinestom() {
        MinecraftServer.init(new Auth.Offline());
    }

    private static Material single(Block block, ItemStack tool) {
        List<ItemStack> drops = BlockDrops.dropsFor(block, tool);
        return drops.isEmpty() ? null : drops.getFirst().material();
    }

    @Test
    void blocksDropFromTheVanillaLootTables() {
        ItemStack hand = ItemStack.AIR;
        ItemStack pickaxe = ItemStack.of(Material.IRON_PICKAXE);
        assertEquals(Material.DIRT, single(Block.GRASS_BLOCK, hand));
        assertEquals(Material.OAK_LOG, single(Block.OAK_LOG, hand));
        assertNull(single(Block.STONE, hand));
        assertEquals(Material.COBBLESTONE, single(Block.STONE, pickaxe));
        assertEquals(Material.STONE, single(Block.STONE, pickaxe.with(DataComponents.ENCHANTMENTS, new EnchantmentList(Enchantment.SILK_TOUCH, 1))));
        assertEquals(Material.DIAMOND, single(Block.DIAMOND_ORE, pickaxe));
        assertNull(single(Block.DIAMOND_ORE, ItemStack.of(Material.WOODEN_PICKAXE)));
        assertNull(single(Block.GLASS, hand));
        assertEquals(Material.OAK_DOOR, single(Block.OAK_DOOR.withProperty("half", "lower"), hand));
        assertNull(single(Block.OAK_DOOR.withProperty("half", "upper"), hand));
        assertEquals(Material.WHEAT_SEEDS, single(Block.WHEAT, hand));
        assertTrue(BlockDrops.dropsFor(Block.WHEAT.withProperty("age", "7"), hand).stream().anyMatch(item -> item.material() == Material.WHEAT));
        int lapis = 0;
        for (int i = 0; i < 50; i++) {
            lapis += BlockDrops.dropsFor(Block.LAPIS_ORE, ItemStack.of(Material.STONE_PICKAXE)).getFirst().amount();
        }
        assertTrue(lapis > 150, "lapis drops 4 to 9");
        assertTrue(BlockDrops.experienceFor(Block.DIAMOND_ORE, pickaxe) >= 3);
        assertEquals(0, BlockDrops.experienceFor(Block.STONE, pickaxe));
    }

    @Test
    void toolsWearOutAndBreak() {
        ItemStack pickaxe = ItemStack.of(Material.WOODEN_PICKAXE);
        ItemStack worn = Durability.damaged(pickaxe, 1);
        assertEquals(1, worn.get(DataComponents.DAMAGE));
        assertTrue(Durability.damaged(pickaxe.with(DataComponents.DAMAGE, 58), 1).isAir());
        assertEquals(ItemStack.of(Material.DIRT), Durability.damaged(ItemStack.of(Material.DIRT), 1));
    }

    @Test
    void containerItemsSurviveTheBlockNbt() {
        assertEquals(InventoryType.CHEST_3_ROW, Containers.kind(Block.CHEST).type());
        assertEquals(InventoryType.SHULKER_BOX, Containers.kind(Block.RED_SHULKER_BOX).type());
        assertNull(Containers.kind(Block.STONE));

        Inventory open = new Inventory(InventoryType.CHEST_3_ROW, "");
        open.setItemStack(3, ItemStack.of(Material.DIAMOND, 5));
        open.setItemStack(26, ItemStack.of(Material.TORCH, 64));
        Block chest = Block.CHEST.withTag(Tag.NBT("Items"), Containers.items(open));

        Inventory reopened = new Inventory(InventoryType.CHEST_3_ROW, "");
        Containers.read(chest, reopened);
        assertEquals(ItemStack.of(Material.DIAMOND, 5), reopened.getItemStack(3));
        assertEquals(ItemStack.of(Material.TORCH, 64), reopened.getItemStack(26));
        assertTrue(reopened.getItemStack(0).isAir());
    }
}
