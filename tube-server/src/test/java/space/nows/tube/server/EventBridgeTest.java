package space.nows.tube.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.nows.tube.api.Block;
import space.nows.tube.api.event.BlockBreakEvent;
import space.nows.tube.api.event.BlockPlaceEvent;
import space.nows.tube.api.event.PlayerJoinEvent;
import space.nows.tube.api.event.PlayerQuitEvent;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

@EnvTest
class EventBridgeTest {

    @TempDir
    Path directory;

    private final DefaultEventBus bus = new DefaultEventBus();
    private final List<String> log = new ArrayList<>();
    private WorldService worlds;
    private PlayerService players;

    private void install() {
        worlds = new WorldService(directory, "main");
        players = new PlayerService(worlds, new PermissionService(directory));
        new EventBridge(bus, players, worlds).install(MinecraftServer.getGlobalEventHandler());
    }

    private net.minestom.server.instance.Instance instance() {
        return ((TubeWorld) worlds.defaultWorld()).instance();
    }

    @Test
    void postsJoinAndQuit(Env env) {
        install();
        bus.subscribe(PlayerJoinEvent.class, event -> log.add("join:" + event.player().name()));
        bus.subscribe(PlayerQuitEvent.class, event -> log.add("quit:" + event.player().name()));

        var player = env.createPlayer(instance(), new Pos(0, 42, 0));
        assertEquals(List.of("join:" + player.getUsername()), log);
        assertEquals(1, players.online().size());

        player.remove();
        assertEquals(List.of("join:" + player.getUsername(), "quit:" + player.getUsername()), log);
        assertTrue(players.online().isEmpty());
    }

    @Test
    void chatCanBeRewrittenOrCancelled(Env env) {
        install();
        var player = env.createPlayer(instance(), new Pos(0, 42, 0));
        var recipients = (Collection<net.minestom.server.entity.Player>) List.of(player);

        bus.subscribe(space.nows.tube.api.event.PlayerChatEvent.class, event -> {
            if (event.message().contains("secret")) {
                event.setCancelled(true);
            } else {
                event.setMessage(event.message().toUpperCase());
            }
        });

        var rewritten = new net.minestom.server.event.player.PlayerChatEvent(player, recipients, "hello");
        EventDispatcher.call(rewritten);
        assertFalse(rewritten.isCancelled());
        assertTrue(PlainTextComponentSerializer.plainText().serialize(rewritten.getFormattedMessage()).endsWith("HELLO"));

        var blocked = new net.minestom.server.event.player.PlayerChatEvent(player, recipients, "a secret");
        EventDispatcher.call(blocked);
        assertTrue(blocked.isCancelled());
    }

    @Test
    void blockBreakCanBeCancelled(Env env) {
        install();
        var player = env.createPlayer(instance(), new Pos(0, 42, 0));
        bus.subscribe(BlockBreakEvent.class, event -> {
            log.add(event.block().key() + "@" + event.x() + "," + event.y() + "," + event.z());
            event.setCancelled(true);
        });

        var stone = net.minestom.server.instance.block.Block.STONE;
        var breaking = new net.minestom.server.event.player.PlayerBlockBreakEvent(
                player, instance(), stone, net.minestom.server.instance.block.Block.AIR, new BlockVec(3, 30, 4), BlockFace.TOP);
        EventDispatcher.call(breaking);

        assertEquals(List.of("minecraft:stone@3,30,4"), log);
        assertTrue(breaking.isCancelled());
    }

    @Test
    void blockPlaceCanSwapTheBlock(Env env) {
        install();
        var player = env.createPlayer(instance(), new Pos(0, 42, 0));
        bus.subscribe(BlockPlaceEvent.class, event -> event.setBlock(Block.of("minecraft:diamond_block")));

        var placing = new net.minestom.server.event.player.PlayerBlockPlaceEvent(
                player, instance(), net.minestom.server.instance.block.Block.DIRT, BlockFace.TOP,
                new BlockVec(1, 41, 1), Vec.ZERO, PlayerHand.MAIN);
        EventDispatcher.call(placing);

        assertFalse(placing.isCancelled());
        assertEquals(net.minestom.server.instance.block.Block.DIAMOND_BLOCK, placing.getBlock());
    }
}
