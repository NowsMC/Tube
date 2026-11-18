package space.nows.tube.server.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.coordinate.Pos;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.nows.tube.api.Server;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.world.GameRule;
import space.nows.tube.api.world.Weather;
import space.nows.tube.api.world.World;
import space.nows.tube.server.access.PermissionService;
import space.nows.tube.server.command.builtin.BuiltinCommands;
import space.nows.tube.server.player.MinestomPlayer;
import space.nows.tube.server.player.PlayerService;
import space.nows.tube.server.world.MinestomWorld;
import space.nows.tube.server.world.WorldService;

import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@EnvTest
class WorldStateCommandsTest {

    @TempDir
    Path directory;

    private record Setup(CommandService commands, MinestomPlayer player, World world) {
    }

    private Setup setup(Env env) {
        WorldService worlds = new WorldService(directory, "main");
        PermissionService permissions = new PermissionService(directory);
        PlayerService players = new PlayerService(worlds, permissions);
        CommandService commands = new CommandService(players);
        World world = worlds.defaultWorld();
        MinestomPlayer player = players.wrap(env.createPlayer(((MinestomWorld) world).instance(), new Pos(10, 42, 10)));
        permissions.setOp(player.uuid(), player.name(), true);
        Server server = (Server) Proxy.newProxyInstance(Server.class.getClassLoader(), new Class<?>[] {Server.class}, (proxy, method, args) -> switch (method.getName()) {
            case "players" -> List.copyOf(players.online());
            case "worlds" -> worlds;
            default -> null;
        });
        new BuiltinCommands(server, commands, null, null, null, null, false).registerAll();
        return new Setup(commands, player, world);
    }

    private static List<String> run(Setup setup, String line) {
        List<String> messages = new ArrayList<>();
        MinestomPlayer player = setup.player();
        CommandSender recorder = (CommandSender) Proxy.newProxyInstance(CommandSender.class.getClassLoader(),
                new Class<?>[] {CommandSender.class, space.nows.tube.api.entity.Player.class}, (proxy, method, args) -> {
                    if (method.getName().equals("sendMessage") && args[0] instanceof Component component) {
                        messages.add(PlainTextComponentSerializer.plainText().serialize(component));
                        return null;
                    }
                    if (method.getName().equals("sendMessage")) {
                        messages.add(String.valueOf(args[0]));
                        return null;
                    }
                    return method.invoke(player, args);
                });
        setup.commands().dispatch(recorder, line);
        return messages;
    }

    @Test
    void weatherChangesTheWorld(Env env) {
        Setup setup = setup(env);

        run(setup, "weather rain");
        assertEquals(Weather.RAIN, setup.world().weather());

        run(setup, "weather thunder 100");
        assertEquals(Weather.THUNDER, setup.world().weather());

        run(setup, "weather clear");
        assertEquals(Weather.CLEAR, setup.world().weather());
    }

    @Test
    void gameRulesAreReadAndChanged(Env env) {
        Setup setup = setup(env);

        assertEquals(List.of("Game rule keepInventory is currently set to: false"), run(setup, "gamerule keepInventory"));

        run(setup, "gamerule keepInventory true");

        assertTrue(setup.world().gameRule(GameRule.KEEP_INVENTORY));
        assertEquals(List.of("Unknown game rule nope"), run(setup, "gamerule nope true"));
    }

    @Test
    void tagsAreAddedListedAndSelected(Env env) {
        Setup setup = setup(env);

        run(setup, "tag @s add boss");

        assertEquals(List.of("The targets have 1 tags: boss"), run(setup, "tag @s list"));
        assertEquals(List.of("Added tag 'extra' to 1 entity"), run(setup, "tag @e[tag=boss] add extra"));
        assertTrue(run(setup, "tag @e[tag=!boss] list").get(0).startsWith("No entity"));
    }

    @Test
    void completesOptionsAndValues(Env env) {
        Setup setup = setup(env);

        assertEquals(List.of("clear", "rain", "thunder"), setup.commands().suggest(setup.player(), "weather "));
        assertEquals(List.of("keepInventory"), setup.commands().suggest(setup.player(), "gamerule keepI"));
        assertEquals(List.of("@e[tag="), setup.commands().suggest(setup.player(), "kill @e[ta"));
        assertFalse(setup.commands().suggest(setup.player(), "particle ").isEmpty());
    }
}
