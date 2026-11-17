package space.nows.tube.server.gameplay.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.google.gson.JsonParser;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

class RecipeBookTest {

    @TempDir
    Path directory;

    @BeforeAll
    static void initMinestom() {
        MinecraftServer.init(new Auth.Offline());
    }

    private static RecipeBook sample() {
        return RecipeBook.builder()
                .add("oak_planks", JsonParser.parseString("""
                        {"type":"minecraft:crafting_shapeless","ingredients":["#minecraft:oak_logs"],"result":{"count":4,"id":"minecraft:oak_planks"}}""").getAsJsonObject())
                .add("stick", JsonParser.parseString("""
                        {"type":"minecraft:crafting_shaped","key":{"#":"#minecraft:planks"},"pattern":["#","#"],"result":{"count":4,"id":"minecraft:stick"}}""").getAsJsonObject())
                .add("wooden_pickaxe", JsonParser.parseString("""
                        {"type":"minecraft:crafting_shaped","key":{"#":"minecraft:stick","X":"#minecraft:planks"},"pattern":["XXX"," # "," # "],"result":{"id":"minecraft:wooden_pickaxe"}}""").getAsJsonObject())
                .add("wooden_hoe", JsonParser.parseString("""
                        {"type":"minecraft:crafting_shaped","key":{"#":"minecraft:stick","X":"#minecraft:planks"},"pattern":["XX"," #"," #"],"result":{"id":"minecraft:wooden_hoe"}}""").getAsJsonObject())
                .add("iron_ingot", JsonParser.parseString("""
                        {"type":"minecraft:smelting","experience":0.7,"ingredient":"minecraft:raw_iron","result":{"id":"minecraft:iron_ingot"}}""").getAsJsonObject())
                .build();
    }

    private static ItemStack[] grid(int size, Object... cells) {
        ItemStack[] grid = new ItemStack[size * size];
        Arrays.fill(grid, ItemStack.AIR);
        for (int i = 0; i < cells.length; i += 2) {
            grid[(Integer) cells[i]] = ItemStack.of((Material) cells[i + 1]);
        }
        return grid;
    }

    @Test
    void shapedAndShapelessRecipesMatchAnywhereInTheGrid() {
        RecipeBook book = sample();
        assertEquals(ItemStack.of(Material.OAK_PLANKS, 4), book.craft(grid(2, 3, Material.OAK_LOG), 2));
        assertEquals(ItemStack.of(Material.STICK, 4), book.craft(grid(2, 1, Material.BIRCH_PLANKS, 3, Material.OAK_PLANKS), 2));
        assertEquals(ItemStack.of(Material.STICK, 4), book.craft(grid(3, 5, Material.BIRCH_PLANKS, 8, Material.OAK_PLANKS), 3));
        assertEquals(ItemStack.of(Material.WOODEN_PICKAXE), book.craft(grid(3,
                0, Material.OAK_PLANKS, 1, Material.OAK_PLANKS, 2, Material.OAK_PLANKS, 4, Material.STICK, 7, Material.STICK), 3));
        assertTrue(book.craft(grid(3, 0, Material.OAK_PLANKS, 1, Material.OAK_PLANKS, 4, Material.STICK), 3).isAir());
        assertEquals(ItemStack.of(Material.WOODEN_HOE), book.craft(grid(3,
                1, Material.OAK_PLANKS, 2, Material.OAK_PLANKS, 4, Material.STICK, 7, Material.STICK), 3));
        assertEquals(ItemStack.of(Material.WOODEN_HOE), book.craft(grid(3,
                0, Material.OAK_PLANKS, 1, Material.OAK_PLANKS, 4, Material.STICK, 7, Material.STICK), 3));
        assertEquals("minecraft:iron_ingot", book.cooking("minecraft:smelting", ItemStack.of(Material.RAW_IRON)).orElseThrow().result().material().key().asString());
    }

    @Test
    void readsTheRecipesOfARealServerJar() throws Exception {
        String jar = System.getProperty("tube.vanillaJar");
        assumeTrue(jar != null && Files.isRegularFile(Path.of(jar)));
        Path zip = directory.resolve("recipes.zip");
        VanillaRecipes.extractRecipes(Path.of(jar), "26.2", zip);
        RecipeBook book = VanillaRecipes.read(zip);
        System.out.println("vanilla recipes " + book.size());
        assertTrue(book.size() > 1000);
        assertEquals(ItemStack.of(Material.CRAFTING_TABLE), book.craft(grid(2,
                0, Material.OAK_PLANKS, 1, Material.OAK_PLANKS, 2, Material.SPRUCE_PLANKS, 3, Material.OAK_PLANKS), 2));
        assertEquals(ItemStack.of(Material.TORCH, 4), book.craft(grid(3, 1, Material.CHARCOAL, 4, Material.STICK), 3));
        assertEquals(ItemStack.of(Material.FURNACE), book.craft(grid(3,
                0, Material.COBBLESTONE, 1, Material.COBBLESTONE, 2, Material.COBBLESTONE, 3, Material.COBBLESTONE, 5, Material.COBBLED_DEEPSLATE,
                6, Material.COBBLESTONE, 7, Material.COBBLESTONE, 8, Material.COBBLESTONE), 3));
        assertTrue(book.cooking("minecraft:smelting", ItemStack.of(Material.RAW_IRON)).isPresent());
        assertTrue(book.cooking("minecraft:smelting", ItemStack.of(Material.OAK_LOG)).isPresent());
    }
}
