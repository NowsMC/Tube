package space.nows.tube.server.gameplay.physics;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;
import space.nows.tube.api.world.Environment;
import space.nows.tube.api.world.WorldOptions;
import space.nows.tube.server.world.MinestomWorld;
import space.nows.tube.server.world.WorldService;

import java.util.concurrent.CompletableFuture;

public final class NetherPortals {

    private static final Tag<Integer> PORTAL_TICKS = Tag.Transient("tube:portal_ticks");
    private static final Tag<Long> COOLDOWN_UNTIL = Tag.Transient("tube:portal_cooldown");
    private static final int SEARCH_RADIUS = 16;

    private final WorldService worlds;

    public NetherPortals(WorldService worlds) {
        this.worlds = worlds;
    }

    public void install() {
        MinecraftServer.getBlockManager().registerBlockPlacementRule(new BlockPlacementRule(Block.NETHER_PORTAL) {
            @Override
            public Block blockPlace(PlacementState state) {
                return state.block();
            }

            @Override
            public Block blockUpdate(UpdateState state) {
                Block current = state.currentBlock();
                boolean alongX = "x".equals(current.getProperty("axis"));
                int[][] around = alongX ? new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}} : new int[][]{{0, 0, 1}, {0, 0, -1}, {0, 1, 0}, {0, -1, 0}};
                for (int[] offset : around) {
                    Block neighbour = state.instance().getBlock(state.blockPosition().add(offset[0], offset[1], offset[2]));
                    if (!neighbour.compare(Block.OBSIDIAN) && !neighbour.compare(Block.NETHER_PORTAL)) {
                        return Block.AIR;
                    }
                }
                return current;
            }
        });
        MinecraftServer.getSchedulerManager().buildTask(this::tick).repeat(TaskSchedule.tick(1)).schedule();
    }

    boolean tryLight(Instance instance, BlockVec inside) {
        if (!instance.getBlock(inside).air()) {
            return false;
        }
        return tryLight(instance, inside, true) || tryLight(instance, inside, false);
    }

    private static boolean tryLight(Instance instance, BlockVec inside, boolean alongX) {
        int dx = alongX ? 1 : 0;
        int dz = alongX ? 0 : 1;
        BlockVec bottom = inside;
        int steps = 0;
        while (instance.getBlock(bottom.add(0, -1, 0)).air() && steps++ < 21) {
            bottom = bottom.add(0, -1, 0);
        }
        if (!instance.getBlock(bottom.add(0, -1, 0)).compare(Block.OBSIDIAN)) {
            return false;
        }
        BlockVec corner = bottom;
        steps = 0;
        while (instance.getBlock(corner.add(-dx, 0, -dz)).air() && steps++ < 21) {
            corner = corner.add(-dx, 0, -dz);
        }
        if (!instance.getBlock(corner.add(-dx, 0, -dz)).compare(Block.OBSIDIAN)) {
            return false;
        }
        int width = 0;
        while (width < 22 && instance.getBlock(corner.add(dx * width, 0, dz * width)).air()) {
            width++;
        }
        if (width < 2 || width > 21 || !instance.getBlock(corner.add(dx * width, 0, dz * width)).compare(Block.OBSIDIAN)) {
            return false;
        }
        int height = 0;
        while (height < 22 && instance.getBlock(corner.add(0, height, 0)).air()) {
            height++;
        }
        if (height < 3 || height > 21) {
            return false;
        }
        for (int w = 0; w < width; w++) {
            if (!instance.getBlock(corner.add(dx * w, -1, dz * w)).compare(Block.OBSIDIAN)
                    || !instance.getBlock(corner.add(dx * w, height, dz * w)).compare(Block.OBSIDIAN)) {
                return false;
            }
            for (int h = 0; h < height; h++) {
                if (!instance.getBlock(corner.add(dx * w, h, dz * w)).air()) {
                    return false;
                }
            }
        }
        for (int h = 0; h < height; h++) {
            if (!instance.getBlock(corner.add(-dx, h, -dz)).compare(Block.OBSIDIAN)
                    || !instance.getBlock(corner.add(dx * width, h, dz * width)).compare(Block.OBSIDIAN)) {
                return false;
            }
        }
        Block portal = Block.NETHER_PORTAL.withProperty("axis", alongX ? "x" : "z");
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                instance.setBlock(corner.add(dx * w, h, dz * w), portal, false);
            }
        }
        return true;
    }

    private void tick() {
        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            Instance instance = player.getInstance();
            if (instance == null || player.isDead()) {
                continue;
            }
            Long cooldown = player.getTag(COOLDOWN_UNTIL);
            boolean inPortal = instance.getBlock(player.getPosition()).compare(Block.NETHER_PORTAL)
                    || instance.getBlock(player.getPosition().add(0, 1, 0)).compare(Block.NETHER_PORTAL);
            if (!inPortal) {
                player.removeTag(PORTAL_TICKS);
                if (cooldown != null && player.getAliveTicks() > cooldown) {
                    player.removeTag(COOLDOWN_UNTIL);
                }
                continue;
            }
            if (cooldown != null) {
                player.setTag(COOLDOWN_UNTIL, player.getAliveTicks() + 20);
                continue;
            }
            int ticks = player.getTag(PORTAL_TICKS) == null ? 1 : player.getTag(PORTAL_TICKS) + 1;
            player.setTag(PORTAL_TICKS, ticks);
            int needed = player.getGameMode() == GameMode.CREATIVE ? 1 : 80;
            if (ticks >= needed) {
                player.removeTag(PORTAL_TICKS);
                player.setTag(COOLDOWN_UNTIL, player.getAliveTicks() + 20);
                travel(player);
            }
        }
    }

    private void travel(Player player) {
        MinestomWorld from = worlds.of(player.getInstance());
        MinestomWorld defaultWorld = (MinestomWorld) worlds.defaultWorld();
        boolean toNether = from.options().environment() != Environment.NETHER;
        MinestomWorld to;
        if (toNether) {
            to = (MinestomWorld) worlds.create(defaultWorld.name() + "_nether",
                    new WorldOptions(WorldOptions.NETHER, defaultWorld.options().seed(), Environment.NETHER));
        } else {
            to = defaultWorld;
        }
        Pos at = player.getPosition();
        double scale = toNether ? 0.125 : 8;
        int x = (int) Math.floor(at.x() * scale);
        int z = (int) Math.floor(at.z() * scale);
        Instance target = to.instance();
        CompletableFuture<?>[] loads = new CompletableFuture<?>[25];
        int index = 0;
        for (int cx = -2; cx <= 2; cx++) {
            for (int cz = -2; cz <= 2; cz++) {
                loads[index++] = target.loadChunk((x >> 4) + cx, (z >> 4) + cz);
            }
        }
        CompletableFuture.allOf(loads).thenRun(() -> target.scheduleNextTick(ignored -> {
            BlockVec portal = findPortal(target, x, at.blockY(), z);
            if (portal == null) {
                portal = buildPortal(target, x, toNether ? Math.clamp(at.blockY() / 2, 40, 100) : at.blockY(), z, toNether);
            }
            player.setInstance(target, new Pos(portal.x() + 0.5, portal.y(), portal.z() + 0.5, at.yaw(), at.pitch()));
        }));
    }

    private static BlockVec findPortal(Instance instance, int x, int y, int z) {
        int minY = instance.getCachedDimensionType().minY();
        int maxY = minY + instance.getCachedDimensionType().height();
        BlockVec best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
            for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
                for (int py = minY; py < maxY; py++) {
                    if (instance.getBlock(x + dx, py, z + dz).compare(Block.NETHER_PORTAL)
                            && !instance.getBlock(x + dx, py - 1, z + dz).compare(Block.NETHER_PORTAL)) {
                        double distance = dx * dx + dz * dz + (py - y) * (double) (py - y);
                        if (distance < bestDistance) {
                            bestDistance = distance;
                            best = new BlockVec(x + dx, py, z + dz);
                        }
                    }
                }
            }
        }
        return best;
    }

    private static BlockVec buildPortal(Instance instance, int x, int y, int z, boolean nether) {
        int minY = instance.getCachedDimensionType().minY() + 2;
        int maxY = nether ? 120 : minY + instance.getCachedDimensionType().height() - 8;
        int baseY = Math.clamp(y, minY, maxY);
        for (int dy = 0; dy < 40; dy++) {
            for (int sign : new int[]{1, -1}) {
                int candidate = baseY + dy * sign;
                if (candidate > minY && candidate < maxY && instance.getBlock(x, candidate - 1, z).solid()
                        && clear(instance, x, candidate, z)) {
                    baseY = candidate;
                    dy = 40;
                    break;
                }
            }
        }
        for (int dx = -1; dx <= 2; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                instance.setBlock(x + dx, baseY - 1, z + dz, Block.OBSIDIAN);
                for (int dy = 0; dy < 4; dy++) {
                    instance.setBlock(x + dx, baseY + dy, z + dz, Block.AIR);
                }
            }
        }
        for (int dx = -1; dx <= 2; dx++) {
            instance.setBlock(x + dx, baseY - 1, z, Block.OBSIDIAN);
            instance.setBlock(x + dx, baseY + 3, z, Block.OBSIDIAN);
        }
        for (int dy = 0; dy < 3; dy++) {
            instance.setBlock(x - 1, baseY + dy, z, Block.OBSIDIAN);
            instance.setBlock(x + 2, baseY + dy, z, Block.OBSIDIAN);
        }
        Block portal = Block.NETHER_PORTAL.withProperty("axis", "x");
        for (int dx = 0; dx <= 1; dx++) {
            for (int dy = 0; dy < 3; dy++) {
                instance.setBlock(x + dx, baseY + dy, z, portal, false);
            }
        }
        return new BlockVec(x, baseY, z);
    }

    private static boolean clear(Instance instance, int x, int y, int z) {
        for (int dx = -1; dx <= 2; dx++) {
            for (int dy = 0; dy < 4; dy++) {
                Block block = instance.getBlock(x + dx, y + dy, z);
                if (!block.air()) {
                    return false;
                }
            }
        }
        return true;
    }
}
