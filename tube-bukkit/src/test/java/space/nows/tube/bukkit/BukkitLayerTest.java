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
            import org.bukkit.event.player.AsyncPlayerChatEvent;
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
        assertEquals(1, runtime.pluginManager.getPlugins().length);
        assertTrue(runtime.pluginManager.getPlugin("Demo").isEnabled());
        assertEquals("1.2.3", runtime.pluginManager.getPlugin("Demo").getPluginMeta().getVersion());
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
