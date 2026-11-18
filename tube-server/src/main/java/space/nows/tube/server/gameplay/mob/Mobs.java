package space.nows.tube.server.gameplay.mob;

import net.minestom.server.MinecraftServer;
import net.minestom.server.color.DyeColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.world.Difficulty;
import space.nows.tube.server.event.DeathDrops;
import space.nows.tube.server.gameplay.Archery;
import space.nows.tube.server.gameplay.Durability;
import space.nows.tube.server.gameplay.block.BlockDropper;
import space.nows.tube.server.gameplay.loot.LootTables;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class Mobs {

    public static final Tag<Long> LAST_PLAYER_HIT = Tag.Transient("tube:last_player_hit");
    public static final Tag<Player> LAST_PLAYER = Tag.Transient("tube:last_player");

    private static final Mob.Kind[] ANIMALS = {Mob.Kind.COW, Mob.Kind.PIG, Mob.Kind.SHEEP, Mob.Kind.CHICKEN};
    private static final Mob.Kind[] MONSTERS = {Mob.Kind.ZOMBIE, Mob.Kind.ZOMBIE, Mob.Kind.SKELETON, Mob.Kind.SKELETON, Mob.Kind.SPIDER, Mob.Kind.CREEPER};
    private static final int HOSTILE_CAP = 16;
    private static final int ANIMAL_CAP = 10;
    private static final double CAP_RANGE = 128;

    private Mobs() {
    }

    public static void install(GlobalEventHandler handler) {
        Archery.install(handler);
        HurtSound.install(handler);
        handler.addListener(EntityDamageEvent.class, event -> {
            if (!event.isCancelled() && event.getEntity() instanceof Mob mob) {
                mob.panic(event.getDamage().getAttacker());
            }
        });
        handler.addListener(PlayerEntityInteractEvent.class, Mobs::interact);
        MinecraftServer.getSchedulerManager().buildTask(Mobs::tick).repeat(TaskSchedule.tick(20)).schedule();
    }

    public static boolean isHostile(Entity entity) {
        return entity instanceof Mob mob && mob.kind().hostile();
    }

    public static DeathDrops.Drops loot(Mob mob) {
        Long hit = mob.getTag(LAST_PLAYER_HIT);
        Player killer = mob.getTag(LAST_PLAYER);
        boolean byPlayer = hit != null && killer != null && mob.getAliveTicks() - hit < 100;
        LootTables.Context context = LootTables.Context.entity(byPlayer ? killer.getItemInMainHand() : ItemStack.AIR, byPlayer,
                mob.getFireTicks() > 0, ThreadLocalRandom.current());
        if (mob.getEntityMeta() instanceof SheepMeta sheep) {
            context = context.withSheep(sheep.getColor().name().toLowerCase(java.util.Locale.ROOT), sheep.isSheared());
        }
        List<ItemStack> items = LootTables.forEntity(mob.getEntityType().key().asString(), context);
        int experience = !byPlayer ? 0 : mob.kind().hostile() ? 5 : 1 + ThreadLocalRandom.current().nextInt(3);
        return new DeathDrops.Drops(items, experience);
    }

    private static void interact(PlayerEntityInteractEvent event) {
        if (event.getHand() != PlayerHand.MAIN || !(event.getTarget() instanceof Mob mob)) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack held = player.getItemInMainHand();
        if (held.material() == Material.SHEARS && mob.getEntityMeta() instanceof SheepMeta sheep && !sheep.isSheared()) {
            sheep.setSheared(true);
            Material wool = Material.fromKey("minecraft:" + sheep.getColor().name().toLowerCase(java.util.Locale.ROOT) + "_wool");
            int count = 1 + ThreadLocalRandom.current().nextInt(3);
            BlockDropper.spawn(mob.getInstance(), mob.getPosition().add(0, 1, 0), ItemStack.of(wool == null ? Material.WHITE_WOOL : wool, count));
            Durability.damage(player, net.minestom.server.entity.EquipmentSlot.MAIN_HAND, 1);
        } else if (held.material() == Material.BUCKET && mob.getEntityType() == EntityType.COW) {
            ItemStack milk = ItemStack.of(Material.MILK_BUCKET);
            if (player.getGameMode() == GameMode.CREATIVE) {
                player.getInventory().addItemStack(milk);
            } else if (held.amount() == 1) {
                player.setItemInMainHand(milk);
            } else {
                player.setItemInMainHand(held.consume(1));
                if (!player.getInventory().addItemStack(milk)) {
                    player.dropItem(milk);
                }
            }
        }
    }

    private static void tick() {
        Difficulty difficulty = MinecraftServer.getDifficulty();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (Instance instance : MinecraftServer.getInstanceManager().getInstances()) {
            List<Player> players = new ArrayList<>(instance.getPlayers());
            List<Mob> mobs = new ArrayList<>();
            for (Entity entity : instance.getEntities()) {
                if (entity instanceof Mob mob && !mob.isDead()) {
                    mobs.add(mob);
                }
            }
            boolean day = !isNight(instance);
            for (Mob mob : mobs) {
                double nearest = Double.MAX_VALUE;
                for (Player player : players) {
                    nearest = Math.min(nearest, player.getPosition().distanceSquared(mob.getPosition()));
                }
                if (nearest > CAP_RANGE * CAP_RANGE || mob.kind().hostile() && difficulty == Difficulty.PEACEFUL
                        || mob.kind().hostile() && nearest > 32 * 32 && random.nextInt(40) == 0) {
                    mob.remove();
                    continue;
                }
                if (day && mob.kind().burnsInDaylight() && mob.getFireTicks() == 0 && exposedToSky(instance, mob.getPosition())
                        && !instance.getBlock(mob.getPosition()).liquid()) {
                    mob.setFireTicks(160);
                }
            }
            if (!isOverworld(instance)) {
                continue;
            }
            for (Player player : players) {
                if (player.getGameMode() == GameMode.SPECTATOR) {
                    continue;
                }
                int hostiles = 0;
                int animals = 0;
                for (Mob mob : mobs) {
                    if (mob.getPosition().distanceSquared(player.getPosition()) < CAP_RANGE * CAP_RANGE) {
                        if (mob.kind().hostile()) {
                            hostiles++;
                        } else {
                            animals++;
                        }
                    }
                }
                if (difficulty != Difficulty.PEACEFUL && hostiles < HOSTILE_CAP) {
                    trySpawnMonster(instance, player, random);
                }
                if (animals < ANIMAL_CAP && random.nextInt(4) == 0) {
                    trySpawnAnimals(instance, player, random);
                }
            }
        }
    }

    static boolean isNight(Instance instance) {
        long time = Math.floorMod(instance.getTime(), 24000L);
        return time >= 13000 && time < 23000;
    }

    private static boolean isOverworld(Instance instance) {
        return instance.getCachedDimensionType().hasSkylight() && !instance.getCachedDimensionType().hasCeiling() && !instance.getCachedDimensionType().hasEnderDragonFight();
    }

    static boolean exposedToSky(Instance instance, Pos position) {
        int maxY = instance.getCachedDimensionType().maxY();
        for (int y = position.blockY() + 1; y < maxY; y++) {
            Block block = instance.getBlock(position.blockX(), y, position.blockZ());
            if (!block.air() && block.registry().collisionShape().isFaceFull(net.minestom.server.instance.block.BlockFace.BOTTOM) || block.liquid()) {
                return false;
            }
        }
        return true;
    }

    private static void trySpawnMonster(Instance instance, Player player, ThreadLocalRandom random) {
        Pos center = player.getPosition();
        double angle = random.nextDouble(Math.PI * 2);
        double distance = 24 + random.nextDouble(24);
        int x = (int) Math.floor(center.x() + Math.cos(angle) * distance);
        int z = (int) Math.floor(center.z() + Math.sin(angle) * distance);
        if (!instance.isChunkLoaded(x >> 4, z >> 4)) {
            return;
        }
        int startY = Math.min(instance.getCachedDimensionType().maxY() - 3, center.blockY() + random.nextInt(-24, 25));
        for (int y = startY; y > startY - 16 && y > instance.getCachedDimensionType().minY(); y--) {
            if (!standable(instance, x, y, z)) {
                continue;
            }
            Pos at = new Pos(x + 0.5, y, z + 0.5);
            if (instance.getBlockLight(x, y, z) > 0) {
                return;
            }
            boolean sky = exposedToSky(instance, at);
            if (sky && !isNight(instance) || !sky && instance.getSkyLight(x, y, z) > 7) {
                return;
            }
            if (center.distanceSquared(at) < 24 * 24) {
                return;
            }
            Mob.Kind kind = MONSTERS[random.nextInt(MONSTERS.length)];
            Mob mob = new Mob(kind);
            mob.setInstance(instance, at);
            return;
        }
    }

    private static void trySpawnAnimals(Instance instance, Player player, ThreadLocalRandom random) {
        Pos center = player.getPosition();
        double angle = random.nextDouble(Math.PI * 2);
        double distance = 24 + random.nextDouble(40);
        int x = (int) Math.floor(center.x() + Math.cos(angle) * distance);
        int z = (int) Math.floor(center.z() + Math.sin(angle) * distance);
        if (!instance.isChunkLoaded(x >> 4, z >> 4)) {
            return;
        }
        int startY = Math.min(instance.getCachedDimensionType().maxY() - 3, center.blockY() + 32);
        for (int y = startY; y > center.blockY() - 32; y--) {
            Block ground = instance.getBlock(x, y - 1, z);
            if (ground.air()) {
                continue;
            }
            if (!ground.compare(Block.GRASS_BLOCK) || !standable(instance, x, y, z)) {
                return;
            }
            Mob.Kind kind = ANIMALS[random.nextInt(ANIMALS.length)];
            int group = 2 + random.nextInt(3);
            for (int i = 0; i < group; i++) {
                int dx = x + random.nextInt(-2, 3);
                int dz = z + random.nextInt(-2, 3);
                if (standable(instance, dx, y, dz)) {
                    Mob mob = new Mob(kind);
                    if (mob.getEntityMeta() instanceof SheepMeta sheep) {
                        sheep.setColor(sheepColor(random));
                    }
                    mob.setInstance(instance, new Pos(dx + 0.5, y, dz + 0.5, random.nextFloat() * 360, 0));
                }
            }
            return;
        }
    }

    private static DyeColor sheepColor(ThreadLocalRandom random) {
        int roll = random.nextInt(1000);
        if (roll < 50) {
            return DyeColor.BLACK;
        }
        if (roll < 100) {
            return DyeColor.GRAY;
        }
        if (roll < 150) {
            return DyeColor.LIGHT_GRAY;
        }
        if (roll < 180) {
            return DyeColor.BROWN;
        }
        if (roll < 182) {
            return DyeColor.PINK;
        }
        return DyeColor.WHITE;
    }

    private static boolean standable(Instance instance, int x, int y, int z) {
        Block ground = instance.getBlock(x, y - 1, z);
        Block feet = instance.getBlock(x, y, z);
        Block head = instance.getBlock(x, y + 1, z);
        return ground.registry().collisionShape().isFaceFull(net.minestom.server.instance.block.BlockFace.TOP) && !ground.liquid()
                && !feet.solid() && !feet.liquid() && !head.solid() && !head.liquid();
    }

    static boolean livingTarget(Entity entity) {
        return entity instanceof LivingEntity living && !living.isDead();
    }
}
