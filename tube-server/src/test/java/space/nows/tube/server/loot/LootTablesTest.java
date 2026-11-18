package space.nows.tube.server.loot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.RegistryKey;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@EnvTest
class LootTablesTest {

    private static ItemStack enchanted(Material material, String enchantment, int level) {
        return ItemStack.of(material).with(DataComponents.ENCHANTMENTS, new EnchantmentList(RegistryKey.<Enchantment>of(enchantment), level));
    }

    private static List<ItemStack> blockDrops(Block block, ItemStack tool, Random random, int times) {
        List<ItemStack> drops = new ArrayList<>();
        for (int i = 0; i < times; i++) {
            drops.addAll(LootTables.block(block, LootContext.block(block, tool, random)));
        }
        return drops;
    }

    private static int count(List<ItemStack> drops, Material material) {
        return drops.stream().filter(item -> item.material() == material).mapToInt(ItemStack::amount).sum();
    }

    @Test
    void silkTouchKeepsTheBlockAndAPlainPickaxeBreaksIt(Env env) {
        Random random = new Random(1);

        List<ItemStack> plain = blockDrops(Block.STONE, ItemStack.of(Material.IRON_PICKAXE), random, 20);
        List<ItemStack> silk = blockDrops(Block.STONE, enchanted(Material.IRON_PICKAXE, "minecraft:silk_touch", 1), random, 20);

        assertEquals(20, count(plain, Material.COBBLESTONE));
        assertEquals(0, count(plain, Material.STONE));
        assertEquals(20, count(silk, Material.STONE));
    }

    @Test
    void fortuneRaisesOreDrops(Env env) {
        Random random = new Random(2);

        int plain = count(blockDrops(Block.DIAMOND_ORE, ItemStack.of(Material.IRON_PICKAXE), random, 2000), Material.DIAMOND);
        int fortunate = count(blockDrops(Block.DIAMOND_ORE, enchanted(Material.IRON_PICKAXE, "minecraft:fortune", 3), random, 2000), Material.DIAMOND);

        assertEquals(2000, plain);
        assertTrue(fortunate > 3000, "fortune gave " + fortunate);
    }

    @Test
    void leavesDropSaplingsAndSticksSometimes(Env env) {
        List<ItemStack> drops = blockDrops(Block.OAK_LEAVES, ItemStack.AIR, new Random(3), 3000);

        assertTrue(count(drops, Material.OAK_SAPLING) > 50, "saplings " + count(drops, Material.OAK_SAPLING));
        assertTrue(count(drops, Material.STICK) > 50, "sticks " + count(drops, Material.STICK));
    }

    @Test
    void explosionsDestroyAShareOfTheDrops(Env env) {
        Random random = new Random(4);
        LootContext blast = new LootContext(random, ItemStack.AIR, Block.DIRT, true, false, false, null, false, 0, false);
        int kept = 0;
        for (int i = 0; i < 2000; i++) {
            kept += LootTables.block(Block.DIRT, blast).size();
        }

        assertTrue(kept > 450 && kept < 750, "kept " + kept);
    }

    @Test
    void aBurningCowDropsCookedBeef(Env env) {
        Random random = new Random(5);
        List<ItemStack> raw = new ArrayList<>();
        List<ItemStack> cooked = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            raw.addAll(LootTables.entity(EntityType.COW, LootContext.mob(ItemStack.AIR, true, false, random)));
            cooked.addAll(LootTables.entity(EntityType.COW, LootContext.mob(ItemStack.AIR, true, true, random)));
        }

        assertTrue(count(raw, Material.BEEF) > 0 && count(raw, Material.COOKED_BEEF) == 0);
        assertTrue(count(cooked, Material.COOKED_BEEF) > 0 && count(cooked, Material.BEEF) == 0);
        assertTrue(count(raw, Material.LEATHER) > 0);
    }

    @Test
    void sheepDropWoolOfTheirColourUnlessSheared(Env env) {
        Random random = new Random(6);
        List<ItemStack> woolly = LootTables.entity(EntityType.SHEEP, LootContext.mob(ItemStack.AIR, true, false, random).withSheep("red", false));
        List<ItemStack> bald = LootTables.entity(EntityType.SHEEP, LootContext.mob(ItemStack.AIR, true, false, random).withSheep("red", true));

        assertEquals(1, count(woolly, Material.RED_WOOL));
        assertEquals(0, count(bald, Material.RED_WOOL));
    }

    @Test
    void nestedTablesAreFollowed(Env env) {
        Random random = new Random(7);
        int inkSacs = 0;
        boolean everyStackWasTen = true;
        for (int i = 0; i < 5000; i++) {
            for (ItemStack item : LootTables.gameplay("minecraft:fishing", LootContext.fishing(ItemStack.AIR, 0, false, random))) {
                if (item.material() == Material.INK_SAC) {
                    inkSacs++;
                    everyStackWasTen &= item.amount() == 10;
                }
            }
        }

        assertTrue(inkSacs > 0, "no ink sacs were fished");
        assertTrue(everyStackWasTen);
    }

    @Test
    void chestTablesRollAndUnknownTablesRollNothing(Env env) {
        Random random = new Random(8);
        LootContext context = LootContext.block(null, ItemStack.AIR, random);

        assertFalse(LootTables.chest("minecraft:simple_dungeon", context).isEmpty());
        assertTrue(LootTables.chest("minecraft:no_such_table", context).isEmpty());
        assertTrue(LootTables.gameplay("minecraft:no_such_table", context).isEmpty());
    }
}
