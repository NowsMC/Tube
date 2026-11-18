package space.nows.tube.server.gameplay.fishing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.RegistryKey;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import space.nows.tube.server.loot.LootContext;
import space.nows.tube.server.loot.LootTables;

import java.util.List;
import java.util.Random;

@EnvTest
class FishingTest {

    private Instance instance;
    private Player player;

    private void join(Env env, GameMode mode, float pitch) {
        Fishing.install(MinecraftServer.getGlobalEventHandler());
        instance = env.createFlatInstance();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                instance.loadChunk(x, z).join();
            }
        }
        player = env.createPlayer(instance, new Pos(0, 40, -10, 0, pitch));
        player.setGameMode(mode);
    }

    private void pond() {
        for (int x = -6; x <= 6; x++) {
            for (int z = -6; z <= 6; z++) {
                for (int y = 40; y <= 42; y++) {
                    instance.setBlock(x, y, z, Block.WATER, false);
                }
            }
        }
    }

    private void rod(int lure) {
        ItemStack rod = ItemStack.of(Material.FISHING_ROD);
        if (lure > 0) {
            rod = rod.with(DataComponents.ENCHANTMENTS, new EnchantmentList(RegistryKey.<Enchantment>of("minecraft:lure"), lure));
        }
        player.setItemInMainHand(rod);
    }

    private void use() {
        EventDispatcher.call(new PlayerUseItemEvent(player, PlayerHand.MAIN, player.getItemInMainHand(), 0));
    }

    private Bobber bobber() {
        return instance.getEntities().stream().filter(entity -> entity instanceof Bobber).map(Bobber.class::cast).findFirst().orElse(null);
    }

    private List<ItemEntity> items() {
        return instance.getEntities().stream().filter(entity -> entity instanceof ItemEntity).map(ItemEntity.class::cast).toList();
    }

    private static ItemStack fish(boolean openWater, int luck, Random random) {
        List<ItemStack> items = LootTables.gameplay("minecraft:fishing", LootContext.fishing(ItemStack.AIR, luck, openWater, random));
        return items.isEmpty() ? ItemStack.AIR : items.getFirst();
    }

    private static void ticks(Env env, int count) {
        for (int i = 0; i < count; i++) {
            env.tick();
        }
    }

    @Test
    void castingThrowsABobberAndAReelRemovesIt(Env env) {
        join(env, GameMode.SURVIVAL, 0);
        rod(0);

        use();
        env.tick();

        assertNotNull(bobber());
        assertEquals(EntityType.FISHING_BOBBER, bobber().getEntityType());

        use();
        env.tick();

        assertEquals(null, bobber());
    }

    @Test
    void theBobberFloatsOnTheSurface(Env env) {
        join(env, GameMode.SURVIVAL, 10);
        pond();
        rod(0);

        use();
        ticks(env, 80);

        Bobber bobber = bobber();
        assertNotNull(bobber);
        assertTrue(bobber.getPosition().y() > 42.5 && bobber.getPosition().y() < 44, "the bobber is at " + bobber.getPosition());
    }

    @Test
    void reelingDuringABiteCatchesSomethingAndWearsTheRod(Env env) {
        join(env, GameMode.SURVIVAL, 10);
        pond();
        rod(3);

        use();
        for (int i = 0; i < 600 && (bobber() == null || !bobber().biting()); i++) {
            env.tick();
        }
        assertTrue(bobber() != null && bobber().biting(), "nothing bit");

        use();
        env.tick();

        assertEquals(1, items().size());
        assertEquals(1, player.getItemInMainHand().get(DataComponents.DAMAGE));
        assertEquals(null, bobber());
    }

    @Test
    void reelingTooEarlyCatchesNothing(Env env) {
        join(env, GameMode.SURVIVAL, 10);
        pond();
        rod(0);

        use();
        ticks(env, 30);
        use();
        env.tick();

        assertTrue(items().isEmpty());
        assertFalse(player.getItemInMainHand().has(DataComponents.DAMAGE) && player.getItemInMainHand().get(DataComponents.DAMAGE) > 0);
    }

    @Test
    void aBobberOnTheGroundCostsTwoDurability(Env env) {
        join(env, GameMode.SURVIVAL, 20);
        rod(0);

        use();
        ticks(env, 60);
        assertTrue(bobber() != null && bobber().grounded(), "the bobber is not on the ground");
        use();

        assertEquals(2, player.getItemInMainHand().get(DataComponents.DAMAGE));
    }

    @Test
    void aBobberHooksAMobAndReelingPullsItIn(Env env) {
        join(env, GameMode.SURVIVAL, 0);
        rod(0);
        EntityCreature cow = new EntityCreature(EntityType.COW);
        cow.setInstance(instance, new Pos(0, 40, -5)).join();

        use();
        ticks(env, 5);
        assertEquals(cow, bobber() == null ? null : bobber().hookedEntity());
        double before = cow.getPosition().distance(player.getPosition());
        use();
        ticks(env, 6);

        assertTrue(cow.getPosition().distance(player.getPosition()) < before, "the cow did not come closer");
        assertEquals(5, player.getItemInMainHand().get(DataComponents.DAMAGE));
    }

    @Test
    void lootMostlyComesFromTheFishTable(Env env) {
        int fish = 0;
        int treasure = 0;
        Random random = new Random(42);
        for (int i = 0; i < 2000; i++) {
            ItemStack item = fish(true, 0, random);
            Material material = item.material();
            if (material == Material.COD || material == Material.SALMON || material == Material.PUFFERFISH || material == Material.TROPICAL_FISH) {
                fish++;
            }
            if (material == Material.NAME_TAG || material == Material.SADDLE || material == Material.BOW || material == Material.ENCHANTED_BOOK || material == Material.NAUTILUS_SHELL
                    || material == Material.FISHING_ROD && item.has(DataComponents.ENCHANTMENTS)) {
                treasure++;
            }
        }
        assertTrue(fish > 1500 && fish < 1900, "fish " + fish);
        assertTrue(treasure > 40 && treasure < 180, "treasure " + treasure);
    }

    @Test
    void treasureNeedsOpenWaterAndLuckImprovesIt(Env env) {
        Random random = new Random(7);
        int closed = 0;
        int lucky = 0;
        int plain = 0;
        for (int i = 0; i < 3000; i++) {
            if (fish(false, 0, random).material() == Material.NAME_TAG) {
                closed++;
            }
            if (fish(true, 3, random).material() == Material.NAME_TAG) {
                lucky++;
            }
            if (fish(true, 0, random).material() == Material.NAME_TAG) {
                plain++;
            }
        }
        assertEquals(0, closed);
        assertTrue(lucky > plain, "lucky " + lucky + " plain " + plain);
    }

    @Test
    void treasureBooksCarryAnEnchantment(Env env) {
        Random random = new Random(3);
        ItemStack book = null;
        for (int i = 0; i < 20000 && book == null; i++) {
            ItemStack item = fish(true, 3, random);
            if (item.material() == Material.ENCHANTED_BOOK) {
                book = item;
            }
        }
        assertNotNull(book, "no enchanted book was fished");
        assertFalse(book.get(DataComponents.STORED_ENCHANTMENTS).enchantments().isEmpty());
    }
}
