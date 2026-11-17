package space.nows.tube.server.gameplay.physics;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.timer.TaskSchedule;
import space.nows.tube.server.gameplay.block.BlockDropper;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class Growth {

    private static final int RANDOM_TICKS_PER_SECTION = 3;

    private final int simulationDistance;

    public Growth(int simulationDistance) {
        this.simulationDistance = Math.max(2, simulationDistance);
    }

    public void install(GlobalEventHandler handler) {
        handler.addListener(PlayerBlockBreakEvent.class, event -> {
            String name = event.getBlock().key().value();
            if (!event.isCancelled() && (name.endsWith("_log") || name.endsWith("_wood") || name.endsWith("_leaves"))) {
                scheduleDecay(event.getInstance(), event.getBlockPosition());
            }
        });
        MinecraftServer.getSchedulerManager().buildTask(this::randomTicks).repeat(TaskSchedule.tick(1)).schedule();
    }

    private void randomTicks() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (Instance instance : MinecraftServer.getInstanceManager().getInstances()) {
            Set<Long> chunks = new HashSet<>();
            for (Player player : instance.getPlayers()) {
                int cx = player.getPosition().blockX() >> 4;
                int cz = player.getPosition().blockZ() >> 4;
                for (int dx = -simulationDistance; dx <= simulationDistance; dx++) {
                    for (int dz = -simulationDistance; dz <= simulationDistance; dz++) {
                        chunks.add(((long) (cx + dx) << 32) | ((cz + dz) & 0xFFFFFFFFL));
                    }
                }
            }
            for (long key : chunks) {
                Chunk chunk = instance.getChunk((int) (key >> 32), (int) key);
                if (chunk == null || !chunk.isLoaded()) {
                    continue;
                }
                var sections = chunk.getSections();
                for (int index = 0; index < sections.size(); index++) {
                    Section section = sections.get(index);
                    if (section.blockPalette().count() == 0) {
                        continue;
                    }
                    int baseY = (chunk.getMinSection() + index) * 16;
                    for (int i = 0; i < RANDOM_TICKS_PER_SECTION; i++) {
                        int x = (chunk.getChunkX() << 4) + random.nextInt(16);
                        int y = baseY + random.nextInt(16);
                        int z = (chunk.getChunkZ() << 4) + random.nextInt(16);
                        Block block = chunk.getBlock(x, y, z, Block.Getter.Condition.TYPE);
                        if (block != null && !block.air()) {
                            randomTick(instance, new BlockVec(x, y, z), block, random);
                        }
                    }
                }
            }
        }
    }

    private static void randomTick(Instance instance, BlockVec position, Block block, ThreadLocalRandom random) {
        String name = block.key().value();
        switch (name) {
            case "wheat", "carrots", "potatoes", "beetroots" -> {
                int max = name.equals("beetroots") ? 3 : 7;
                int age = Integer.parseInt(block.getProperty("age"));
                Block soil = instance.getBlock(position.relative(BlockFace.BOTTOM));
                boolean wet = soil.compare(Block.FARMLAND) && !"0".equals(soil.getProperty("moisture"));
                if (age < max && random.nextInt(wet ? 3 : 7) == 0 && light(instance, position) >= 9) {
                    instance.setBlock(position, block.withProperty("age", Integer.toString(age + 1)));
                }
            }
            case "farmland" -> {
                boolean water = nearWater(instance, position);
                int moisture = Integer.parseInt(block.getProperty("moisture"));
                if (water) {
                    if (moisture < 7) {
                        instance.setBlock(position, block.withProperty("moisture", "7"));
                    }
                } else if (moisture > 0) {
                    instance.setBlock(position, block.withProperty("moisture", Integer.toString(moisture - 1)));
                } else if (instance.getBlock(position.relative(BlockFace.TOP)).air()) {
                    instance.setBlock(position, Block.DIRT);
                }
            }
            case "sugar_cane", "cactus" -> {
                if (!instance.getBlock(position.relative(BlockFace.TOP)).air()) {
                    return;
                }
                int height = 1;
                while (height < 3 && instance.getBlock(position.add(0, -height, 0)).compare(block)) {
                    height++;
                }
                if (height >= 3) {
                    return;
                }
                int age = Integer.parseInt(block.getProperty("age"));
                if (age >= 15) {
                    instance.setBlock(position, block.withProperty("age", "0"));
                    instance.setBlock(position.relative(BlockFace.TOP), block.withProperty("age", "0"));
                } else {
                    instance.setBlock(position, block.withProperty("age", Integer.toString(age + 1)), false);
                }
            }
            case "grass_block" -> {
                Block above = instance.getBlock(position.relative(BlockFace.TOP));
                if (above.solid() && above.registry().collisionShape().isFaceFull(BlockFace.BOTTOM) || above.liquid()) {
                    instance.setBlock(position, Block.DIRT);
                    return;
                }
                BlockVec target = position.add(random.nextInt(-1, 2), random.nextInt(-3, 2), random.nextInt(-1, 2));
                if (instance.getBlock(target).compare(Block.DIRT) && instance.getBlock(target.relative(BlockFace.TOP)).air()
                        && light(instance, target.relative(BlockFace.TOP)) >= 9) {
                    instance.setBlock(target, Block.GRASS_BLOCK);
                }
            }
            case "fire" -> {
                Block below = instance.getBlock(position.relative(BlockFace.BOTTOM));
                if (!below.compare(Block.NETHERRACK) && !below.compare(Block.MAGMA_BLOCK) && random.nextInt(3) == 0) {
                    instance.setBlock(position, Block.AIR);
                }
            }
            default -> {
                if (name.endsWith("_sapling") && light(instance, position.relative(BlockFace.TOP)) >= 9 && random.nextInt(7) == 0) {
                    if ("0".equals(block.getProperty("stage"))) {
                        instance.setBlock(position, block.withProperty("stage", "1"), false);
                    } else {
                        growTree(instance, position, name.substring(0, name.length() - "_sapling".length()), random);
                    }
                }
            }
        }
    }

    private static int light(Instance instance, Point position) {
        int sky = instance.getSkyLight(position.blockX(), position.blockY(), position.blockZ());
        long time = Math.floorMod(instance.getTime(), 24000L);
        if (time >= 13000 && time < 23000) {
            sky = Math.max(0, sky - 11);
        }
        return Math.max(sky, instance.getBlockLight(position.blockX(), position.blockY(), position.blockZ()));
    }

    private static boolean nearWater(Instance instance, BlockVec position) {
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                for (int dy = 0; dy <= 1; dy++) {
                    if (instance.getBlock(position.add(dx, dy, dz)).compare(Block.WATER)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    static boolean boneMeal(Instance instance, BlockVec position, Block block) {
        String name = block.key().value();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (name.equals("wheat") || name.equals("carrots") || name.equals("potatoes") || name.equals("beetroots")) {
            int max = name.equals("beetroots") ? 3 : 7;
            int age = Integer.parseInt(block.getProperty("age"));
            if (age >= max) {
                return false;
            }
            instance.setBlock(position, block.withProperty("age", Integer.toString(Math.min(max, age + 2 + random.nextInt(4)))));
            return true;
        }
        if (name.endsWith("_sapling")) {
            if (random.nextFloat() < 0.45f) {
                growTree(instance, position, name.substring(0, name.length() - "_sapling".length()), random);
            }
            return true;
        }
        if (name.equals("grass_block") && instance.getBlock(position.relative(BlockFace.TOP)).air()) {
            for (int i = 0; i < 24; i++) {
                BlockVec target = position.add(random.nextInt(-3, 4), 1, random.nextInt(-3, 4));
                if (instance.getBlock(target).air() && instance.getBlock(target.relative(BlockFace.BOTTOM)).compare(Block.GRASS_BLOCK)) {
                    int roll = random.nextInt(10);
                    instance.setBlock(target, roll == 0 ? Block.DANDELION : roll == 1 ? Block.POPPY : Block.SHORT_GRASS);
                }
            }
            return true;
        }
        return false;
    }

    static void growTree(Instance instance, BlockVec base, String wood, ThreadLocalRandom random) {
        Block log = Block.fromKey("minecraft:" + wood + "_log");
        Block leaves = Block.fromKey("minecraft:" + wood + "_leaves");
        if (log == null || leaves == null) {
            return;
        }
        boolean spruce = wood.equals("spruce");
        int height = (spruce ? 6 : wood.equals("birch") ? 5 : 4) + random.nextInt(3);
        for (int y = 1; y <= height + 1; y++) {
            Block at = instance.getBlock(base.add(0, y, 0));
            if (!at.air() && !at.key().value().endsWith("_leaves")) {
                return;
            }
        }
        leaves = leaves.withProperty("persistent", "false").withProperty("distance", "1");
        int top = base.blockY() + height;
        if (spruce) {
            int radius = 0;
            for (int y = top; y >= base.blockY() + 2; y--) {
                square(instance, base.blockX(), y, base.blockZ(), radius, leaves, false, random);
                radius = radius >= 2 || radius == 1 && random.nextBoolean() ? 0 : radius + 1;
            }
            setIfAir(instance, new BlockVec(base.blockX(), top + 1, base.blockZ()), leaves);
        } else {
            for (int y = top - 3; y <= top; y++) {
                square(instance, base.blockX(), y, base.blockZ(), y >= top - 1 ? 1 : 2, leaves, y == top, random);
            }
        }
        for (int y = 0; y < height; y++) {
            instance.setBlock(base.add(0, y, 0), log);
        }
    }

    private static void square(Instance instance, int x, int y, int z, int radius, Block leaves, boolean trimCorners, ThreadLocalRandom random) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                boolean corner = radius > 0 && Math.abs(dx) == radius && Math.abs(dz) == radius;
                if (corner && (trimCorners || random.nextBoolean())) {
                    continue;
                }
                setIfAir(instance, new BlockVec(x + dx, y, z + dz), leaves);
            }
        }
    }

    private static void setIfAir(Instance instance, BlockVec position, Block block) {
        if (instance.getBlock(position).air()) {
            instance.setBlock(position, block);
        }
    }

    private static void scheduleDecay(Instance instance, BlockVec origin) {
        instance.scheduler().buildTask(() -> {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            for (int dx = -4; dx <= 4; dx++) {
                for (int dy = -4; dy <= 4; dy++) {
                    for (int dz = -4; dz <= 4; dz++) {
                        BlockVec position = origin.add(dx, dy, dz);
                        Block block = instance.getBlock(position);
                        if (block.key().value().endsWith("_leaves") && !"true".equals(block.getProperty("persistent")) && !supported(instance, position)) {
                            instance.scheduler().buildTask(() -> {
                                Block current = instance.getBlock(position);
                                if (current.compare(block)) {
                                    instance.setBlock(position, Block.AIR);
                                    BlockDropper.drop(instance, position, current);
                                }
                            }).delay(TaskSchedule.tick(20 + random.nextInt(200))).schedule();
                        }
                    }
                }
            }
        }).delay(TaskSchedule.tick(5)).schedule();
    }

    private static boolean supported(Instance instance, BlockVec start) {
        ArrayDeque<BlockVec> queue = new ArrayDeque<>();
        Set<BlockVec> seen = new HashSet<>();
        queue.add(start);
        seen.add(start);
        while (!queue.isEmpty()) {
            BlockVec position = queue.poll();
            if (Math.abs(position.blockX() - start.blockX()) + Math.abs(position.blockY() - start.blockY()) + Math.abs(position.blockZ() - start.blockZ()) > 6) {
                continue;
            }
            for (BlockFace face : BlockFace.values()) {
                BlockVec next = position.relative(face);
                if (!seen.add(next)) {
                    continue;
                }
                String name = instance.getBlock(next).key().value();
                if (name.endsWith("_log") || name.endsWith("_wood")) {
                    return true;
                }
                if (name.endsWith("_leaves")) {
                    queue.add(next);
                }
            }
        }
        return false;
    }
}
