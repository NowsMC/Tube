package space.nows.tube.server.gameplay.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.listener.BlockPlacementListener;
import net.minestom.server.network.packet.client.play.ClientPlayerBlockPlacementPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

@EnvTest
class PlacementTest {

    private Instance instance;
    private Player player;

    private void join(Env env, float yaw) {
        BlockInteractions.install(MinecraftServer.getGlobalEventHandler());
        instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();
        player = env.createPlayer(instance, new Pos(8, 40, 0, yaw, 0));
        player.setGameMode(GameMode.SURVIVAL);
    }

    private void place(Material material, Vec against, BlockFace face, float cursorY) {
        player.setItemInMainHand(ItemStack.of(material, 8));
        BlockPlacementListener.listener(new ClientPlayerBlockPlacementPacket(PlayerHand.MAIN, against, face, 0.5f, cursorY, 0.5f, false, false, 0), player);
    }

    @Test
    void blocksFaceTheWayVanillaPlacesThem(Env env) {
        join(env, 180);
        place(Material.FURNACE, new Vec(5, 39, 5), BlockFace.TOP, 1);
        assertEquals("south", instance.getBlock(5, 40, 5).getProperty("facing"));
        place(Material.OAK_STAIRS, new Vec(6, 39, 5), BlockFace.TOP, 1);
        assertEquals("north", instance.getBlock(6, 40, 5).getProperty("facing"));
        assertEquals("bottom", instance.getBlock(6, 40, 5).getProperty("half"));
        place(Material.OAK_LOG, new Vec(5, 40, 5), BlockFace.WEST, 0.5f);
        assertEquals("x", instance.getBlock(4, 40, 5).getProperty("axis"));
    }

    @Test
    void torchesHangOnWallsAndFallWithoutSupport(Env env) {
        join(env, 0);
        instance.setBlock(5, 40, 5, Block.STONE);
        place(Material.TORCH, new Vec(5, 40, 5), BlockFace.SOUTH, 0.5f);
        Block torch = instance.getBlock(5, 40, 6);
        assertTrue(torch.compare(Block.WALL_TORCH), torch::toString);
        assertEquals("south", torch.getProperty("facing"));
        instance.setBlock(5, 40, 5, Block.AIR);
        assertTrue(instance.getBlock(5, 40, 6).air());
    }

    @Test
    void slabsStackIntoDoubleSlabs(Env env) {
        join(env, 0);
        place(Material.OAK_SLAB, new Vec(5, 39, 5), BlockFace.TOP, 1);
        assertEquals("bottom", instance.getBlock(5, 40, 5).getProperty("type"));
        place(Material.OAK_SLAB, new Vec(5, 40, 5), BlockFace.TOP, 0.5f);
        assertEquals("double", instance.getBlock(5, 40, 5).getProperty("type"));
    }

    @Test
    void doorsAndBedsUseTwoBlocks(Env env) {
        join(env, 0);
        place(Material.OAK_DOOR, new Vec(5, 39, 5), BlockFace.TOP, 1);
        assertEquals("lower", instance.getBlock(5, 40, 5).getProperty("half"));
        assertEquals("upper", instance.getBlock(5, 41, 5).getProperty("half"));
        assertEquals("south", instance.getBlock(5, 41, 5).getProperty("facing"));
        place(Material.RED_BED, new Vec(8, 39, 5), BlockFace.TOP, 1);
        assertEquals("foot", instance.getBlock(8, 40, 5).getProperty("part"));
        assertEquals("head", instance.getBlock(8, 40, 6).getProperty("part"));
        instance.setBlock(8, 40, 5, Block.AIR);
        assertTrue(instance.getBlock(8, 40, 6).air());
    }

    @Test
    void fencesAndChestsConnect(Env env) {
        join(env, 0);
        place(Material.OAK_FENCE, new Vec(5, 39, 5), BlockFace.TOP, 1);
        place(Material.OAK_FENCE, new Vec(6, 39, 5), BlockFace.TOP, 1);
        assertEquals("true", instance.getBlock(5, 40, 5).getProperty("east"));
        assertEquals("true", instance.getBlock(6, 40, 5).getProperty("west"));
        place(Material.CHEST, new Vec(5, 39, 8), BlockFace.TOP, 1);
        place(Material.CHEST, new Vec(6, 39, 8), BlockFace.TOP, 1);
        assertTrue(!instance.getBlock(5, 40, 8).getProperty("type").equals("single"));
        assertTrue(!instance.getBlock(6, 40, 8).getProperty("type").equals("single"));
    }
}
