package space.nows.tube.server.gameplay.block;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.listener.PlayerPickListener;
import net.minestom.server.network.packet.client.play.ClientPickItemFromBlockPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

@EnvTest
class PickBlockTest {

    private Instance instance;
    private Player player;

    private void join(Env env, GameMode mode) {
        BlockInteractions.install(MinecraftServer.getGlobalEventHandler());
        instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();
        player = env.createPlayer(instance, new Pos(8, 40, 8));
        player.setGameMode(mode);
        instance.setBlock(5, 40, 5, Block.DIAMOND_BLOCK);
        instance.setBlock(6, 40, 5, Block.OAK_WALL_SIGN);
    }

    private void pick(int x, int y, int z) {
        PlayerPickListener.playerPickBlockListener(new ClientPickItemFromBlockPacket(new Vec(x, y, z), false), player);
    }

    @Test
    void creativePutsTheBlockInTheSelectedSlotWhenItIsEmpty(Env env) {
        join(env, GameMode.CREATIVE);
        player.setHeldItemSlot((byte) 3);

        pick(5, 40, 5);

        assertEquals(Material.DIAMOND_BLOCK, player.getInventory().getItemStack(3).material());
        assertEquals(3, player.getHeldSlot());
    }

    @Test
    void creativeUsesTheNextFreeSlotWhenTheSelectedOneIsTaken(Env env) {
        join(env, GameMode.CREATIVE);
        player.getInventory().setItemStack(0, ItemStack.of(Material.STONE));
        player.setHeldItemSlot((byte) 0);

        pick(5, 40, 5);

        assertEquals(Material.STONE, player.getInventory().getItemStack(0).material());
        assertEquals(Material.DIAMOND_BLOCK, player.getInventory().getItemStack(1).material());
        assertEquals(1, player.getHeldSlot());
    }

    @Test
    void anItemAlreadyInTheHotbarIsSelected(Env env) {
        join(env, GameMode.CREATIVE);
        player.getInventory().setItemStack(6, ItemStack.of(Material.DIAMOND_BLOCK, 5));

        pick(5, 40, 5);

        assertEquals(6, player.getHeldSlot());
        assertEquals(5, player.getInventory().getItemStack(6).amount());
    }

    @Test
    void survivalMovesAnItemFromTheInventoryToTheHotbar(Env env) {
        join(env, GameMode.SURVIVAL);
        player.getInventory().setItemStack(20, ItemStack.of(Material.DIAMOND_BLOCK, 7));

        pick(5, 40, 5);

        assertEquals(7, player.getInventory().getItemStack(player.getHeldSlot()).amount());
        assertEquals(true, player.getInventory().getItemStack(20).isAir());
    }

    @Test
    void survivalGivesNothingForAnItemItDoesNotHave(Env env) {
        join(env, GameMode.SURVIVAL);

        pick(5, 40, 5);

        assertEquals(true, player.getInventory().getItemStack(player.getHeldSlot()).isAir());
    }

    @Test
    void blocksWithoutAnItemAreIgnored(Env env) {
        join(env, GameMode.CREATIVE);

        pick(7, 45, 7);

        assertEquals(true, player.getInventory().getItemStack(player.getHeldSlot()).isAir());
    }

    @Test
    void wallSignsPickTheSign(Env env) {
        join(env, GameMode.CREATIVE);

        pick(6, 40, 5);

        assertEquals(Material.OAK_SIGN, player.getInventory().getItemStack(player.getHeldSlot()).material());
    }
}
