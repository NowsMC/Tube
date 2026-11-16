package space.nows.tube.server.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.player.GameProfile;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.nows.tube.server.world.MinestomWorld;
import space.nows.tube.server.world.WorldService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@EnvTest
class PlayerDataStoreTest {

    private static final UUID ALICE = UUID.fromString("00000000-0000-0000-0000-0000000000a1");

    @TempDir
    Path directory;

    private Player join(Env env, WorldService worlds, String world) {
        var instance = ((MinestomWorld) worlds.load(world)).instance();
        return env.createConnection(new GameProfile(ALICE, "Alice")).connect(instance, new Pos(0, 42, 0));
    }

    @Test
    void inventoryAndVitalsSurviveARoundTrip(Env env) {
        WorldService worlds = new WorldService(directory.resolve("worlds"), "main");
        PlayerDataStore store = new PlayerDataStore(directory.resolve("playerdata"));
        Player first = join(env, worlds, "main");

        ItemStack sword = ItemStack.of(Material.DIAMOND_SWORD).with(DataComponents.DAMAGE, 7).withCustomName(Component.text("Blade"));
        first.getInventory().setItemStack(0, sword);
        first.getInventory().setItemStack(20, ItemStack.of(Material.APPLE, 12));
        first.setEquipment(EquipmentSlot.HELMET, ItemStack.of(Material.IRON_HELMET));
        first.setEquipment(EquipmentSlot.OFF_HAND, ItemStack.of(Material.SHIELD));
        first.setHeldItemSlot((byte) 3);
        first.setHealth(14);
        first.setFood(11);
        first.setLevel(5);
        first.setGameMode(GameMode.CREATIVE);
        store.save(first, "main");
        first.remove();

        Player second = join(env, worlds, "main");
        store.apply(store.read(ALICE).orElseThrow(), second, true);

        assertEquals(7, second.getInventory().getItemStack(0).get(DataComponents.DAMAGE));
        assertEquals(Material.DIAMOND_SWORD, second.getInventory().getItemStack(0).material());
        assertEquals(12, second.getInventory().getItemStack(20).amount());
        assertEquals(Material.IRON_HELMET, second.getEquipment(EquipmentSlot.HELMET).material());
        assertEquals(Material.SHIELD, second.getEquipment(EquipmentSlot.OFF_HAND).material());
        assertEquals(3, second.getHeldSlot());
        assertEquals(14f, second.getHealth());
        assertEquals(11, second.getFood());
        assertEquals(5, second.getLevel());
        assertEquals(GameMode.CREATIVE, second.getGameMode());
    }

    @Test
    void gamemodeIsLeftAloneWhenItIsForced(Env env) {
        WorldService worlds = new WorldService(directory.resolve("worlds"), "main");
        PlayerDataStore store = new PlayerDataStore(directory.resolve("playerdata"));
        Player first = join(env, worlds, "main");
        first.setGameMode(GameMode.CREATIVE);
        store.save(first, "main");
        first.remove();

        Player second = join(env, worlds, "main");
        store.apply(store.read(ALICE).orElseThrow(), second, false);

        assertEquals(GameMode.SURVIVAL, second.getGameMode());
    }

    @Test
    void remembersWhereThePlayerWas(Env env) {
        WorldService worlds = new WorldService(directory.resolve("worlds"), "main");
        PlayerDataStore store = new PlayerDataStore(directory.resolve("playerdata"));
        Player player = join(env, worlds, "nether-like");
        player.teleport(new Pos(5, 60, -3, 90f, 10f)).join();

        store.save(player, "nether-like");

        var spawn = store.spawn(ALICE).orElseThrow();
        assertEquals("nether-like", spawn.world());
        assertEquals(new Pos(5, 60, -3, 90f, 10f), spawn.position());
    }

    @Test
    void usesTheVanillaFileLayout(Env env) throws IOException {
        WorldService worlds = new WorldService(directory.resolve("worlds"), "main");
        PlayerDataStore store = new PlayerDataStore(directory.resolve("playerdata"));
        Player player = join(env, worlds, "main");
        player.getInventory().setItemStack(2, ItemStack.of(Material.STONE, 3));

        store.save(player, "main");

        Path file = directory.resolve("playerdata").resolve(ALICE + ".dat");
        var root = BinaryTagIO.reader().read(file, BinaryTagIO.Compression.GZIP);
        var item = root.getList("Inventory", BinaryTagTypes.COMPOUND).getCompound(0);
        assertEquals(2, item.getByte("Slot"));
        assertEquals("minecraft:stone", item.getString("id"));
        assertEquals(3, item.getInt("count"));
        assertEquals(3, root.getList("Pos", BinaryTagTypes.DOUBLE).size());

        store.save(player, "main");
        assertTrue(Files.exists(directory.resolve("playerdata").resolve(ALICE + ".dat_old")));
    }

    @Test
    void unknownPlayersHaveNoData() {
        PlayerDataStore store = new PlayerDataStore(directory.resolve("playerdata"));
        assertTrue(store.read(ALICE).isEmpty());
        assertTrue(store.spawn(ALICE).isEmpty());
    }
}
