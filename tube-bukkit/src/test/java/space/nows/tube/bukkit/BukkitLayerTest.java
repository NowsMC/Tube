package space.nows.tube.bukkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.nows.tube.api.event.PlayerChatEvent;
import space.nows.tube.api.event.PlayerJoinEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

class BukkitLayerTest {

    private static final String PLUGIN = """
            package demo;

            import org.bukkit.command.Command;
            import org.bukkit.command.CommandSender;
            import org.bukkit.event.EventHandler;
            import org.bukkit.event.EventPriority;
            import org.bukkit.event.Listener;
            import org.bukkit.event.block.BlockBreakEvent;
            import org.bukkit.event.inventory.InventoryClickEvent;
            import org.bukkit.event.player.AsyncPlayerChatEvent;
            import org.bukkit.event.player.PlayerMoveEvent;
            import org.bukkit.event.player.PlayerJoinEvent;
            import org.bukkit.plugin.java.JavaPlugin;

            import java.io.IOException;
            import java.nio.file.Files;
            import java.nio.file.StandardOpenOption;

            public final class Demo extends JavaPlugin implements Listener {

                private void log(String line) {
                    try {
                        getDataFolder().mkdirs();
                        Files.writeString(getDataFolder().toPath().resolve("log.txt"), line + "\\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                }

                @Override
                public void onLoad() {
                    log("load");
                }

                @Override
                public void onEnable() {
                    saveDefaultConfig();
                    log("enable:" + getConfig().getString("greeting") + ":" + getServer().getName());
                    getServer().getPluginManager().registerEvents(this, this);
                    getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
                    getServer().getMessenger().registerIncomingPluginChannel(this, "demo:chan", (channel, player, message) -> log("message:" + channel + ":" + player.getName() + ":" + new String(message)));
                    getCommand("demo").setExecutor((CommandSender sender, Command command, String label, String[] args) -> {
                        sender.sendMessage("demo " + label + " " + String.join(",", args));
                        return true;
                    });
                    getServer().getScheduler().runTaskLater(this, () -> log("scheduled"), 1L);
                }

                @Override
                public void onDisable() {
                    log("disable");
                }

                @EventHandler
                public void onJoin(PlayerJoinEvent event) {
                    log("join:" + event.getPlayer().getName() + ":" + event.getPlayer().getUniqueId().equals(event.getPlayer().getUniqueId()));
                }

                @EventHandler
                public void onBreak(BlockBreakEvent event) {
                    log("break:" + event.getBlock().getX() + "," + event.getBlock().getY() + "," + event.getBlock().getZ() + ":" + event.getPlayer().getName());
                    if (event.getBlock().getX() == 13) {
                        event.setCancelled(true);
                    }
                }

                @EventHandler
                public void onMove(PlayerMoveEvent event) {
                    event.setCancelled(event.getTo().getX() > 100);
                }

                @EventHandler
                public void onClick(InventoryClickEvent event) {
                    event.setCancelled(event.getSlot() == 5 && event.getCurrentItem() != null && event.getCurrentItem().getAmount() == 3);
                }

                @EventHandler(priority = EventPriority.HIGH)
                public void onChat(AsyncPlayerChatEvent event) {
                    if (event.getMessage().contains("blocked")) {
                        event.setCancelled(true);
                    } else {
                        event.setMessage(event.getMessage().toUpperCase());
                    }
                }
            }
            """;

    private static final String DESCRIPTOR = """
            name: Demo
            version: 1.2.3
            main: demo.Demo
            api-version: '1.13'
            commands:
              demo:
                description: demo command
                aliases: [dm]
            """;

    @TempDir
    Path directory;

    private StubServer stub;
    private BukkitRuntime runtime;

    @BeforeEach
    void startLayer() throws IOException {
        BukkitRuntime.forgetInstalledServer();
        Path plugins = Files.createDirectories(directory.resolve("plugins"));
        PluginJars.build(plugins, "demo.jar", DESCRIPTOR, "demo.Demo", PLUGIN, "config.yml", "greeting: hello\n");
        stub = new StubServer();
        runtime = BukkitRuntime.install(stub.server, plugins);
        runtime.loadPlugins();
        runtime.useCurrentThreadAsTickThread();
    }

    @AfterEach
    void stopLayer() {
        if (runtime != null) {
            runtime.shutdown();
        }
    }

    private List<String> log() throws IOException {
        return Files.readAllLines(directory.resolve("plugins/Demo/log.txt"));
    }

    @Test
    void loadsAndEnablesPluginsWithTheirConfig() throws IOException {
        assertEquals(List.of("load", "enable:hello:Tube"), log());
        assertEquals(1, runtime.pluginManager().getPlugins().length);
        assertTrue(runtime.pluginManager().getPlugin("Demo").isEnabled());
        assertEquals("1.2.3", runtime.pluginManager().getPlugin("Demo").getDescription().getVersion());
    }

    @Test
    void pluginCommandsAreExposedThroughTubeCommands() {
        assertTrue(stub.commands.names().contains("demo"));
        assertTrue(stub.commands.names().contains("dm"));

        List<String> replies = new ArrayList<>();
        stub.commands.commands.get("dm").execute(StubServer.consoleLike(replies), List.of("a", "b"));

        assertEquals(List.of("demo dm a,b"), replies);
    }

    @Test
    void schedulerTasksRunOnTheTubeScheduler() throws IOException {
        assertEquals(2, log().size());
        stub.scheduler.tick();
        assertEquals("scheduled", log().getLast());
    }

    @Test
    void tubeEventsReachBukkitListeners() throws IOException {
        var player = stub.player("Steve");

        stub.events.post(new PlayerJoinEvent(player));

        assertEquals("join:Steve:true", log().getLast());
    }

    @Test
    void chatCanBeRewrittenAndCancelledByPlugins() {
        var player = stub.player("Alex");

        PlayerChatEvent rewritten = stub.events.post(new PlayerChatEvent(player, "hello"));
        assertEquals("HELLO", rewritten.message());

        PlayerChatEvent blocked = stub.events.post(new PlayerChatEvent(player, "this is blocked"));
        assertTrue(blocked.isCancelled());
    }

    @Test
    void blockBreaksReachBukkitListenersAndCanBeCancelled() throws IOException {
        var player = stub.player("Steve");
        var world = (space.nows.tube.api.World) java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {space.nows.tube.api.World.class}, (proxy, method, args) -> method.getName().equals("name") ? "world" : null);

        var allowed = stub.events.post(new space.nows.tube.api.event.BlockBreakEvent(player, world, 1, 64, 2, space.nows.tube.api.Block.of("stone")));
        var denied = stub.events.post(new space.nows.tube.api.event.BlockBreakEvent(player, world, 13, 64, 2, space.nows.tube.api.Block.of("stone")));

        assertTrue(!allowed.isCancelled());
        assertTrue(denied.isCancelled());
        assertEquals("break:13,64,2:Steve", log().getLast());
    }

    @Test
    void playerMovesCanBeCancelled() {
        var player = stub.player("Steve");
        var world = (space.nows.tube.api.World) java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {space.nows.tube.api.World.class}, (proxy, method, args) -> method.getName().equals("name") ? "world" : null);
        var from = new space.nows.tube.api.Location(world, 0, 64, 0);

        var allowed = stub.events.post(new space.nows.tube.api.event.PlayerMoveEvent(player, from, new space.nows.tube.api.Location(world, 5, 64, 0)));
        var denied = stub.events.post(new space.nows.tube.api.event.PlayerMoveEvent(player, from, new space.nows.tube.api.Location(world, 200, 64, 0)));

        assertTrue(!allowed.isCancelled());
        assertTrue(denied.isCancelled());
    }

    @Test
    void inventoryClicksCanBeCancelled() {
        var player = stub.player("Steve");
        var inventory = (space.nows.tube.api.Inventory) java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {space.nows.tube.api.Inventory.class}, (proxy, method, args) -> method.getName().equals("equals") ? proxy == args[0] : method.getName().equals("hashCode") ? 1 : null);

        var cancelled = stub.events.post(new space.nows.tube.api.event.InventoryClickEvent(player, inventory, 5, space.nows.tube.api.ItemStack.of("stone", 3)));
        var kept = stub.events.post(new space.nows.tube.api.event.InventoryClickEvent(player, inventory, 5, space.nows.tube.api.ItemStack.of("stone", 2)));

        assertTrue(cancelled.isCancelled());
        assertTrue(!kept.isCancelled());
    }

    @Test
    void pluginMessagesFlowBothWays() throws IOException {
        var player = stub.player("Steve");
        stub.channelHandlers.get("demo:chan").received(player, "demo:chan", "hi".getBytes());
        assertEquals("message:demo:chan:Steve:hi", log().getLast());

        runtime.messenger().send(runtime.pluginManager().getPlugin("Demo"), player, "BungeeCord", "go".getBytes());
        assertEquals("bungeecord:main:go", stub.sentMessages.getLast());
    }

    @Test
    void disablingStopsPluginsAndRemovesBridgedCommands() throws IOException {
        runtime.shutdown();
        runtime = null;

        assertEquals("disable", log().getLast());
        assertTrue(stub.commands.names().isEmpty());
    }

    @SuppressWarnings("unused")
    private static Component unused() {
        return null;
    }
}
