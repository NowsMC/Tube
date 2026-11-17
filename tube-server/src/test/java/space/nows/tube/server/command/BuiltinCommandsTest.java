package space.nows.tube.server.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.listener.TabCompleteListener;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.nows.tube.api.Server;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.inventory.ItemStack;
import space.nows.tube.api.world.Block;
import space.nows.tube.api.world.World;
import space.nows.tube.server.access.PermissionService;
import space.nows.tube.server.command.builtin.BuiltinCommands;
import space.nows.tube.server.command.support.ExperiencePoints;
import space.nows.tube.server.player.MinestomPlayer;
import space.nows.tube.server.player.PlayerService;
import space.nows.tube.server.world.MinestomWorld;
import space.nows.tube.server.world.WorldService;

import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@EnvTest
class BuiltinCommandsTest {

    @TempDir
    Path directory;

    private record Setup(CommandService commands, MinestomPlayer player, World world) {
    }

    private Setup setup(Env env, boolean operator) {
        WorldService worlds = new WorldService(directory, "main");
        PermissionService permissions = new PermissionService(directory);
        PlayerService players = new PlayerService(worlds, permissions);
        CommandService commands = new CommandService(players);
        World world = worlds.defaultWorld();
        MinestomPlayer player = players.wrap(env.createPlayer(((MinestomWorld) world).instance(), new Pos(10, 42, 10)));
        if (operator) {
            permissions.setOp(player.uuid(), player.name(), true);
        }
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
    void suggestsGameModesWhileTyping(Env env) {
        Setup setup = setup(env, true);

        assertEquals(List.of("creative"), setup.commands().suggest(setup.player(), "gamemode cr"));
    }

    @Test
    void suggestsItemsByPathWithoutTheNamespace(Env env) {
        Setup setup = setup(env, true);

        List<String> options = setup.commands().suggest(setup.player(), "give " + setup.player().name() + " diamond_sw");

        assertEquals(List.of("minecraft:diamond_sword"), options);
    }

    @Test
    void suggestsOnlinePlayersAndSelectors(Env env) {
        Setup setup = setup(env, true);

        List<String> options = setup.commands().suggest(setup.player(), "tp ");

        assertTrue(options.contains(setup.player().name()));
        assertTrue(options.contains("@s"));
    }

    @Test
    void clientTabCompletionReplacesTheWordBeingTyped(Env env) {
        WorldService worlds = new WorldService(directory, "main");
        PermissionService permissions = new PermissionService(directory);
        PlayerService players = new PlayerService(worlds, permissions);
        CommandService commands = new CommandService(players);
        var minestomPlayer = env.createPlayer(((MinestomWorld) worlds.defaultWorld()).instance(), new Pos(0, 42, 0));
        permissions.setOp(minestomPlayer.getUuid(), minestomPlayer.getUsername(), true);
        Server server = (Server) Proxy.newProxyInstance(Server.class.getClassLoader(), new Class<?>[] {Server.class},
                (proxy, method, args) -> method.getName().equals("players") ? List.copyOf(players.online()) : null);
        new BuiltinCommands(server, commands, null, null, null, null, false).registerAll();

        var suggestion = TabCompleteListener.getSuggestion(minestomPlayer, "/gamemode cr");

        assertEquals(List.of("creative"), suggestion.getEntries().stream().map(SuggestionEntry::getEntry).toList());
        assertEquals(10, suggestion.getStart());
        assertEquals(2, suggestion.getLength());
    }

    @Test
    void suggestsNothingWithoutThePermission(Env env) {
        Setup setup = setup(env, false);

        assertEquals(List.of(), setup.commands().suggest(setup.player(), "gamemode cr"));
    }

    @Test
    void timeSetUsesNamedTimes(Env env) {
        Setup setup = setup(env, true);

        run(setup, "time set night");

        assertEquals(13000, setup.world().time());
    }

    @Test
    void fillCountsChangedBlocks(Env env) {
        Setup setup = setup(env, true);

        List<String> messages = run(setup, "fill 0 70 0 2 70 2 stone");

        assertEquals(List.of("Successfully filled 9 block(s)"), messages);
        assertEquals("minecraft:stone", setup.world().block(1, 70, 1).key());
    }

    @Test
    void fillRefusesHugeAreas(Env env) {
        Setup setup = setup(env, true);

        List<String> messages = run(setup, "fill 0 0 0 100 100 100 stone");

        assertTrue(messages.getFirst().startsWith("Too many blocks"));
    }

    @Test
    void cloneCopiesBlocks(Env env) {
        Setup setup = setup(env, true);
        setup.world().setBlock(0, 80, 0, Block.of("gold_block"));

        run(setup, "clone 0 80 0 0 80 0 5 80 5");

        assertEquals("minecraft:gold_block", setup.world().block(5, 80, 5).key());
    }

    @Test
    void clearRemovesMatchingItems(Env env) {
        Setup setup = setup(env, true);
        setup.player().inventory().add(ItemStack.of("apple", 5));
        setup.player().inventory().add(ItemStack.of("stick", 3));

        List<String> messages = run(setup, "clear @s apple");

        assertEquals(List.of("Removed 5 item(s) from " + setup.player().name()), messages);
        assertEquals("minecraft:stick", setup.player().inventory().item(1).type());
    }

    @Test
    void experiencePointsCarryIntoLevels(Env env) {
        Setup setup = setup(env, true);

        run(setup, "xp add @s 20 points");

        assertEquals(2, setup.player().level());
        assertEquals(20, ExperiencePoints.total(setup.player()));
    }

    @Test
    void effectGiveAppliesTheEffect(Env env) {
        Setup setup = setup(env, true);

        run(setup, "effect give @s speed 10 1");

        assertTrue(setup.player().effects().stream().anyMatch(effect -> effect.type().equals("minecraft:speed") && effect.amplifier() == 1));
    }

    @Test
    void setWorldSpawnMovesTheSpawn(Env env) {
        Setup setup = setup(env, true);

        run(setup, "setworldspawn 3 64 4");

        assertEquals(3.5, setup.world().spawn().x());
        assertEquals(64.0, setup.world().spawn().y());
    }
}
