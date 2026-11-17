package space.nows.tube.server.gameplay.physics;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.instance.InstanceBlockUpdateEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.timer.TaskSchedule;
import space.nows.tube.server.gameplay.block.BlockDropper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.WeakHashMap;

public final class Fluids {

    private static final int[][] SIDES = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private static final int MAX_PER_TICK = 4096;

    private record Pending(long due, BlockVec position) {
    }

    private final Map<Instance, PriorityQueue<Pending>> queues = new WeakHashMap<>();
    private final Map<Instance, java.util.Set<BlockVec>> queued = new WeakHashMap<>();
    private long tick;

    public void install(GlobalEventHandler handler) {
        handler.addListener(InstanceBlockUpdateEvent.class, event -> changed(event.getInstance(), event.getBlockPosition()));
        MinecraftServer.getSchedulerManager().buildTask(this::run).repeat(TaskSchedule.tick(1)).schedule();
    }

    public static boolean isWater(Block block) {
        return block.compare(Block.WATER);
    }

    public static boolean isLava(Block block) {
        return block.compare(Block.LAVA);
    }

    private static boolean isFluid(Block block) {
        return isWater(block) || isLava(block);
    }

    synchronized void changed(Instance instance, BlockVec position) {
        schedule(instance, position);
        for (BlockFace face : BlockFace.values()) {
            schedule(instance, position.relative(face));
        }
    }

    private synchronized void schedule(Instance instance, BlockVec position) {
        Block block = instance.getBlock(position, Block.Getter.Condition.TYPE);
        if (block == null || !isFluid(block)) {
            return;
        }
        java.util.Set<BlockVec> pending = queued.computeIfAbsent(instance, ignored -> new java.util.HashSet<>());
        if (!pending.add(position)) {
            return;
        }
        int delay = isWater(block) ? 5 : instance.getCachedDimensionType().hasCeiling() ? 10 : 30;
        queues.computeIfAbsent(instance, ignored -> new PriorityQueue<>((a, b) -> Long.compare(a.due(), b.due())))
                .add(new Pending(tick + delay, position));
    }

    private void run() {
        List<Map.Entry<Instance, BlockVec>> work = new ArrayList<>();
        synchronized (this) {
            tick++;
            for (var entry : queues.entrySet()) {
                PriorityQueue<Pending> queue = entry.getValue();
                java.util.Set<BlockVec> pending = queued.get(entry.getKey());
                while (!queue.isEmpty() && queue.peek().due() <= tick && work.size() < MAX_PER_TICK) {
                    Pending next = queue.poll();
                    pending.remove(next.position());
                    work.add(Map.entry(entry.getKey(), next.position()));
                }
            }
        }
        for (var item : work) {
            Instance instance = item.getKey();
            if (instance.isChunkLoaded(item.getValue())) {
                update(instance, item.getValue());
            }
        }
    }

    private void update(Instance instance, BlockVec position) {
        Block block = instance.getBlock(position);
        if (!isFluid(block)) {
            return;
        }
        boolean water = isWater(block);
        int level = Integer.parseInt(block.getProperty("level"));
        int drop = water || instance.getCachedDimensionType().hasCeiling() ? 1 : 2;

        if (!water) {
            for (BlockFace face : new BlockFace[]{BlockFace.TOP, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
                if (isWater(instance.getBlock(position.relative(face)))) {
                    instance.setBlock(position, level == 0 ? Block.OBSIDIAN : Block.COBBLESTONE);
                    return;
                }
            }
        }

        if (level != 0) {
            int expected = expectedLevel(instance, position, block, water, drop);
            if (expected < 0) {
                instance.setBlock(position, Block.AIR);
                return;
            }
            if (expected != level) {
                block = block.withProperty("level", Integer.toString(expected));
                instance.setBlock(position, block);
                level = expected;
            }
        }

        BlockVec below = position.relative(BlockFace.BOTTOM);
        Block under = instance.getBlock(below);
        if (!water && isWater(under)) {
            instance.setBlock(below, Block.STONE);
            return;
        }
        boolean canFall = canFlowInto(under, block) && below.y() >= instance.getCachedDimensionType().minY();
        if (canFall) {
            flowInto(instance, below, under, block.withProperty("level", "8"));
            if (level != 0) {
                return;
            }
        }
        int next = (level >= 8 ? 0 : level) + drop;
        if (next > 7 || level >= 8 && isFluid(under)) {
            return;
        }
        for (int[] side : SIDES) {
            BlockVec target = position.add(side[0], 0, side[1]);
            Block current = instance.getBlock(target);
            if (current.compare(block)) {
                int existing = Integer.parseInt(current.getProperty("level"));
                if (existing != 0 && existing < 8 && existing > next) {
                    instance.setBlock(target, current.withProperty("level", Integer.toString(next)));
                }
                continue;
            }
            if (canFlowInto(current, block)) {
                flowInto(instance, target, current, block.withProperty("level", Integer.toString(next)));
            }
        }
    }

    private int expectedLevel(Instance instance, BlockVec position, Block block, boolean water, int drop) {
        Block above = instance.getBlock(position.relative(BlockFace.TOP));
        if (above.compare(block)) {
            return 8;
        }
        int best = Integer.MAX_VALUE;
        int sources = 0;
        for (int[] side : SIDES) {
            Block neighbour = instance.getBlock(position.add(side[0], 0, side[1]));
            if (!neighbour.compare(block)) {
                continue;
            }
            int level = Integer.parseInt(neighbour.getProperty("level"));
            if (level == 0) {
                sources++;
            }
            best = Math.min(best, level >= 8 ? 0 : level);
        }
        if (water && sources >= 2) {
            Block under = instance.getBlock(position.relative(BlockFace.BOTTOM));
            if (solidBelow(under) || isWater(under) && "0".equals(under.getProperty("level"))) {
                return 0;
            }
        }
        if (best == Integer.MAX_VALUE || best + drop > 7) {
            return -1;
        }
        return best + drop;
    }

    private static boolean solidBelow(Block block) {
        return block.solid() && !block.liquid();
    }

    static boolean canFlowInto(Block target, Block fluid) {
        if (target.air()) {
            return true;
        }
        if (target.liquid()) {
            return false;
        }
        String name = target.key().value();
        if (target.properties().containsKey("waterlogged") || name.endsWith("_door") || name.endsWith("_sign") || name.equals("ladder")
                || name.endsWith("_portal") || name.equals("bedrock")) {
            return false;
        }
        return target.registry().isReplaceable() || !target.solid() && !target.registry().collisionShape().isFaceFull(BlockFace.TOP);
    }

    private static void flowInto(Instance instance, BlockVec position, Block current, Block fluid) {
        if (!current.air() && !current.liquid()) {
            if (isLava(fluid)) {
                instance.setBlock(position, fluid);
                return;
            }
            BlockDropper.drop(instance, position, current);
        }
        instance.setBlock(position, fluid);
    }
}
