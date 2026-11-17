package space.nows.tube.server.gameplay.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.listener.WindowListener;
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

@EnvTest
class CraftingGridsTest {

    private static final RecipeBook BOOK = RecipeBook.builder()
            .add("crafting_table", JsonParser.parseString("""
                    {"type":"minecraft:crafting_shaped","key":{"#":"#minecraft:planks"},"pattern":["##","##"],"result":{"id":"minecraft:crafting_table"}}""").getAsJsonObject())
            .add("oak_planks", JsonParser.parseString("""
                    {"type":"minecraft:crafting_shapeless","ingredients":["#minecraft:oak_logs"],"result":{"count":4,"id":"minecraft:oak_planks"}}""").getAsJsonObject())
            .build();

    private Player join(Env env) {
        new CraftingGrids(() -> BOOK).install(MinecraftServer.getGlobalEventHandler());
        Instance instance = env.createFlatInstance();
        return env.createPlayer(instance, new Pos(0, 42, 0));
    }

    private static void click(Player player, int windowSlot, ClientClickWindowPacket.ClickType type, int button) {
        WindowListener.clickWindowListener(new ClientClickWindowPacket(0, 0, (short) windowSlot, (byte) button, type, Map.of(),
                ItemStack.Hash.of(player.getInventory().getCursorItem(), MinecraftServer.process())), player);
    }

    @Test
    void takingTheResultConsumesTheGrid(Env env) {
        Player player = join(env);
        var inventory = player.getInventory();
        for (int slot = PlayerInventoryUtils.CRAFT_SLOT_1; slot <= PlayerInventoryUtils.CRAFT_SLOT_4; slot++) {
            inventory.setItemStack(slot, ItemStack.of(Material.OAK_PLANKS, 2));
        }
        assertEquals(ItemStack.of(Material.CRAFTING_TABLE), inventory.getItemStack(PlayerInventoryUtils.CRAFT_RESULT));

        click(player, 0, ClientClickWindowPacket.ClickType.PICKUP, 0);

        assertEquals(ItemStack.of(Material.CRAFTING_TABLE), inventory.getCursorItem());
        assertEquals(ItemStack.of(Material.OAK_PLANKS, 1), inventory.getItemStack(PlayerInventoryUtils.CRAFT_SLOT_1));
        assertEquals(ItemStack.of(Material.CRAFTING_TABLE), inventory.getItemStack(PlayerInventoryUtils.CRAFT_RESULT));

        click(player, 0, ClientClickWindowPacket.ClickType.PICKUP, 0);
        assertEquals(ItemStack.of(Material.CRAFTING_TABLE, 2), inventory.getCursorItem());
        assertTrue(inventory.getItemStack(PlayerInventoryUtils.CRAFT_SLOT_1).isAir());
        assertTrue(inventory.getItemStack(PlayerInventoryUtils.CRAFT_RESULT).isAir());
    }

    @Test
    void shiftClickCraftsAsManyAsPossible(Env env) {
        Player player = join(env);
        var inventory = player.getInventory();
        inventory.setItemStack(PlayerInventoryUtils.CRAFT_SLOT_2, ItemStack.of(Material.OAK_LOG, 5));

        click(player, 0, ClientClickWindowPacket.ClickType.QUICK_MOVE, 0);

        int planks = 0;
        for (int slot = 0; slot < 36; slot++) {
            if (inventory.getItemStack(slot).material() == Material.OAK_PLANKS) {
                planks += inventory.getItemStack(slot).amount();
            }
        }
        assertEquals(20, planks);
        assertTrue(inventory.getItemStack(PlayerInventoryUtils.CRAFT_SLOT_2).isAir());
        assertTrue(inventory.getItemStack(PlayerInventoryUtils.CRAFT_RESULT).isAir());
    }

    @Test
    void gridItemsGoBackWhenSaving(Env env) {
        Player player = join(env);
        player.getInventory().setItemStack(PlayerInventoryUtils.CRAFT_SLOT_3, ItemStack.of(Material.OAK_LOG, 3));
        CraftingGrids.returnPlayerGrid(player);
        assertEquals(ItemStack.of(Material.OAK_LOG, 3), player.getInventory().getItemStack(0));
        assertTrue(player.getInventory().getItemStack(PlayerInventoryUtils.CRAFT_RESULT).isAir());
    }
}
