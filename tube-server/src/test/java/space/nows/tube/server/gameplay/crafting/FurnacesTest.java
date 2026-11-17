package space.nows.tube.server.gameplay.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

@EnvTest
class FurnacesTest {

    private static final RecipeBook BOOK = RecipeBook.builder()
            .add("iron_ingot", JsonParser.parseString("""
                    {"type":"minecraft:smelting","experience":0.7,"ingredient":"minecraft:raw_iron","result":{"id":"minecraft:iron_ingot"}}""").getAsJsonObject())
            .build();

    @Test
    void furnacesSmeltWithFuelWhileNobodyWatches(Env env) {
        new Furnaces(() -> BOOK).install(MinecraftServer.getGlobalEventHandler());
        Instance instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();
        ListBinaryTag items = ListBinaryTag.builder(BinaryTagTypes.COMPOUND)
                .add(ItemStack.of(Material.RAW_IRON, 2).toItemNBT().putByte("Slot", (byte) 0))
                .add(ItemStack.of(Material.COAL).toItemNBT().putByte("Slot", (byte) 1))
                .build();
        Vec position = new Vec(1, 41, 1);
        instance.setBlock(position, Block.FURNACE.withHandler(MinecraftServer.getBlockManager().getHandler("minecraft:furnace")).withTag(Tag.NBT("Items"), items));

        for (int tick = 0; tick < 430; tick++) {
            env.tick();
        }

        Block furnace = instance.getBlock(position);
        int ingots = 0;
        boolean coalLeft = false;
        for (BinaryTag element : (ListBinaryTag) furnace.getTag(Tag.NBT("Items"))) {
            CompoundBinaryTag slot = (CompoundBinaryTag) element;
            if (slot.getString("id").equals("minecraft:iron_ingot")) {
                ingots = slot.getInt("count", 1);
            }
            coalLeft |= slot.getString("id").equals("minecraft:coal");
        }
        assertEquals(2, ingots);
        assertTrue(!coalLeft);
        assertEquals("true", furnace.getProperty("lit"));
        assertEquals(1600, Furnaces.fuelTime(Material.COAL));
        assertEquals(300, Furnaces.fuelTime(Material.OAK_LOG));
        assertEquals(300, Furnaces.fuelTime(Material.SPRUCE_PLANKS));
        assertEquals(100, Furnaces.fuelTime(Material.STICK));
    }
}
