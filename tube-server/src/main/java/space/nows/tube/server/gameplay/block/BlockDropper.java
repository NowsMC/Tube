package space.nows.tube.server.gameplay.block;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import space.nows.tube.server.gameplay.loot.LootTables;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public final class BlockDropper {

    private BlockDropper() {
    }

    public static void drop(Instance instance, Point position, Block block) {
        for (ItemStack item : LootTables.forBlock(block, LootTables.Context.block(block, ItemStack.AIR, ThreadLocalRandom.current()))) {
            spawn(instance, position.add(0.5, 0.25, 0.5), item);
        }
    }

    public static void spawn(Instance instance, Point position, ItemStack item) {
        if (item.isAir()) {
            return;
        }
        ItemEntity entity = new ItemEntity(item);
        entity.setPickupDelay(Duration.ofMillis(500));
        ThreadLocalRandom random = ThreadLocalRandom.current();
        entity.setVelocity(new Vec(random.nextDouble(-1, 1), 3, random.nextDouble(-1, 1)));
        entity.setInstance(instance, position);
    }

    public static Block lootState(Block block) {
        if ("upper".equals(block.getProperty("half")) && block.properties().containsKey("half")) {
            return block.withProperty("half", "lower");
        }
        if ("foot".equals(block.getProperty("part"))) {
            return block.withProperty("part", "head");
        }
        return block;
    }
}
