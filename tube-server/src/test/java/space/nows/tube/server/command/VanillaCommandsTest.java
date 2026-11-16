package space.nows.tube.server.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.coordinate.Pos;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.nows.tube.api.CommandSender;
import space.nows.tube.api.GameMode;
import space.nows.tube.api.Server;
import space.nows.tube.server.access.PermissionService;
import space.nows.tube.server.player.MinestomPlayer;
import space.nows.tube.server.player.PlayerService;
import space.nows.tube.server.world.MinestomWorld;
import space.nows.tube.server.world.WorldService;

import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@EnvTest
class VanillaCommandsTest {

    @TempDir
    Path directory;

    private final List<String> broadcasts = new ArrayList<>();

    private record Setup(CommandService commands, MinestomPlayer player, PermissionService permissions) {
    }

    private Setup setup(Env env, boolean operator) {
        WorldService worlds = new WorldService(directory, "main");
        PermissionService permissions = new PermissionService(directory);
        PlayerService players = new PlayerService(worlds, permissions);
        CommandService commands = new CommandService(players);
        MinestomPlayer player = players.wrap(env.createPlayer(((MinestomWorld) worlds.defaultWorld()).instance(), new Pos(10, 42, 10)));
        if (operator) {
            permissions.setOp(player.uuid(), player.name(), true);
        }
        Server server = (Server) Proxy.newProxyInstance(Server.class.getClassLoader(), new Class<?>[] {Server.class}, (proxy, method, args) -> {
            if (method.getName().equals("players")) {
                return List.copyOf(players.online());
            }
            if (method.getName().equals("broadcast")) {
                broadcasts.add(args[0] instanceof Component component ? PlainTextComponentSerializer.plainText().serialize(component) : String.valueOf(args[0]));
            }
            return null;
        });
        new VanillaCommands(server).register(commands);
        return new Setup(commands, player, permissions);
    }

    private static List<String> capture(MinestomPlayer player, CommandService commands, String line) {
        List<String> messages = new ArrayList<>();
        CommandSender recorder = new CommandSender() {
            @Override
            public String name() {
                return player.name();
            }

            @Override
            public boolean hasPermission(String node) {
                return player.hasPermission(node);
            }

            @Override
            public void sendMessage(Component message) {
                messages.add(PlainTextComponentSerializer.plainText().serialize(message));
            }
        };
        commands.dispatch(recorder, line);
        return messages;
    }

    @Test
    void giveAddsItemsToTheInventory(Env env) {
        Setup setup = setup(env, true);

        capture(setup.player(), setup.commands(), "give " + setup.player().name() + " apple 5");

        assertEquals("minecraft:apple", setup.player().inventory().item(0).type());
        assertEquals(5, setup.player().inventory().item(0).amount());
    }

    @Test
    void invalidArgumentsGiveAMessageInsteadOfAnError(Env env) {
        Setup setup = setup(env, true);

        List<String> messages = capture(setup.player(), setup.commands(), "give " + setup.player().name() + " tube:nothing");

        assertEquals(List.of("Unknown item: tube:nothing"), messages);
    }

    @Test
    void gamemodeChangesTheCaller(Env env) {
        Setup setup = setup(env, true);

        setup.commands().dispatch(setup.player(), "gamemode creative");

        assertEquals(GameMode.CREATIVE, setup.player().gameMode());
    }

    @Test
    void teleportSupportsRelativeCoordinates(Env env) {
        Setup setup = setup(env, true);

        setup.commands().dispatch(setup.player(), "tp ~5 50 ~");

        assertEquals(15.0, setup.player().location().x());
        assertEquals(50.0, setup.player().location().y());
        assertEquals(10.0, setup.player().location().z());
    }

    @Test
    void summonSpawnsAnEntityAtThePlayer(Env env) {
        Setup setup = setup(env, true);

        setup.commands().dispatch(setup.player(), "summon zombie");

        assertTrue(setup.player().world().entities().stream().anyMatch(entity -> entity.type().equals("minecraft:zombie")));
    }

    @Test
    void sayBroadcastsWithTheSenderName(Env env) {
        Setup setup = setup(env, true);

        capture(setup.player(), setup.commands(), "say hello everyone");

        assertEquals(List.of("[" + setup.player().name() + "] hello everyone"), broadcasts);
    }

    @Test
    void restrictedCommandsNeedAPermission(Env env) {
        Setup setup = setup(env, false);

        List<String> denied = capture(setup.player(), setup.commands(), "gamemode creative");
        assertEquals(1, denied.size());
        assertTrue(denied.getFirst().startsWith("I'm sorry"));
        assertEquals(GameMode.SURVIVAL, setup.player().gameMode());

        setup.permissions().grant(setup.player().uuid(), "tube.command.gamemode");
        setup.commands().dispatch(setup.player(), "gamemode creative");
        assertEquals(GameMode.CREATIVE, setup.player().gameMode());
    }

    @Test
    void msgIsOpenToEveryoneAndConfirmsToTheSender(Env env) {
        Setup setup = setup(env, false);

        List<String> messages = capture(setup.player(), setup.commands(), "msg " + setup.player().name() + " hi there");

        assertEquals(List.of("You whisper to " + setup.player().name() + ": hi there"), messages);
    }
}
