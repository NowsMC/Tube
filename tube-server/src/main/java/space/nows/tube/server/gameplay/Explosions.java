package space.nows.tube.server.gameplay;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Explosion;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import space.nows.tube.api.event.EventBus;
import space.nows.tube.api.event.ExplosionEvent;
import space.nows.tube.api.world.Location;
import space.nows.tube.server.gameplay.loot.LootTables;
import space.nows.tube.server.world.WorldService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class Explosions {

    private static final Tag<BinaryTag> ITEMS = Tag.NBT("Items");

    private Explosions() {
    }

    private static volatile EventBus events;
    private static volatile WorldService worlds;

    static void configure(EventBus bus, WorldService worldService) {
        events = bus;
        worlds = worldService;
    }

    public static void explode(Instance instance, Point center, float power, boolean breakBlocks, Entity source) {
        List<Point> destroyed = breakBlocks ? affectedBlocks(instance, center, power) : List.of();
        EventBus bus = events;
        WorldService worldService = worlds;
        var world = worldService == null ? java.util.Optional.<space.nows.tube.server.world.MinestomWorld>empty() : worldService.find(instance);
        if (bus != null && world.isPresent()) {
            List<ExplosionEvent.BlockPosition> positions = new ArrayList<>();
            for (Point point : destroyed) {
                positions.add(new ExplosionEvent.BlockPosition(point.blockX(), point.blockY(), point.blockZ()));
            }
            var posted = bus.post(new ExplosionEvent(new Location(world.get(), center.x(), center.y(), center.z()),
                    source == null ? null : worldService.wrap(source), power, positions));
            if (posted.isCancelled()) {
                return;
            }
            power = posted.power();
            Set<ExplosionEvent.BlockPosition> allowed = new HashSet<>(posted.blocks());
            destroyed = destroyed.stream().filter(point -> allowed.contains(
                    new ExplosionEvent.BlockPosition(point.blockX(), point.blockY(), point.blockZ()))).toList();
        }
        if (power <= 0) {
            return;
        }
        List<Block> previous = new ArrayList<>();
        for (Point point : destroyed) {
            previous.add(instance.getBlock(point));
        }
        List<Point> removed = destroyed;
        new Explosion((float) center.x(), (float) center.y(), (float) center.z(), power) {
            @Override
            protected List<Point> prepare(Instance ignored) {
                return removed;
            }
        }.apply(instance);
        float strength = power;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < destroyed.size(); i++) {
            Point point = destroyed.get(i);
            Block block = previous.get(i);
            if (block.getTag(ITEMS) instanceof ListBinaryTag list) {
                for (BinaryTag element : list) {
                    if (element instanceof CompoundBinaryTag compound) {
                        Drops.spawn(instance, point.add(0.5, 0.5, 0.5), ItemStack.fromItemNBT(compound.remove("Slot")));
                    }
                }
            }
            if (random.nextFloat() < 1f / strength) {
                LootTables.Context context = new LootTables.Context(ItemStack.AIR, block, false, false, false, null, false, random);
                for (ItemStack drop : LootTables.forBlock(block, context)) {
                    Drops.spawn(instance, point.add(0.5, 0.5, 0.5), drop);
                }
            }
        }
        double radius = strength * 2;
        for (Entity entity : instance.getNearbyEntities(center, radius)) {
            Vec offset = entity.getPosition().add(0, entity.getEyeHeight(), 0).asVec().sub(center);
            double distance = offset.length() / radius;
            if (distance > 1) {
                continue;
            }
            double impact = 1 - distance;
            if (entity instanceof LivingEntity living && !(living instanceof Player player && (player.getGameMode() == GameMode.CREATIVE
                    || player.getGameMode() == GameMode.SPECTATOR))) {
                float damage = (float) ((impact * impact + impact) / 2 * 7 * radius + 1);
                living.damage(source == null ? new Damage(DamageType.EXPLOSION, null, null, center, damage)
                        : new Damage(DamageType.EXPLOSION, source, source, center, damage));
            }
            Vec push = offset.length() < 0.01 ? new Vec(0, 1, 0) : offset.normalize();
            entity.setVelocity(entity.getVelocity().add(push.mul(impact * 20)));
        }
    }

    private static List<Point> affectedBlocks(Instance instance, Point center, float power) {
        List<Point> blocks = new ArrayList<>();
        int range = (int) Math.ceil(power);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    Point point = new Vec(Math.floor(center.x()) + x, Math.floor(center.y()) + y, Math.floor(center.z()) + z);
                    Block block = instance.getBlock(point);
                    if (block.air() || block.liquid()) {
                        continue;
                    }
                    double distance = point.add(0.5, 0.5, 0.5).distance(center);
                    double strength = power * (0.7 + random.nextDouble() * 0.6) - distance * 0.75
                            - (block.registry().explosionResistance() + 0.3) * 0.3;
                    if (strength > 0) {
                        blocks.add(point);
                    }
                }
            }
        }
        return blocks;
    }
}
