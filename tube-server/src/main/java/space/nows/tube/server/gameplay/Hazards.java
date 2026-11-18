package space.nows.tube.server.gameplay;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;
import space.nows.tube.server.item.Enchantments;

final class Hazards {

    private static final int MAX_AIR = 300;

    private Hazards() {
    }

    static void install() {
        MinecraftServer.getSchedulerManager().buildTask(Hazards::tick).repeat(TaskSchedule.tick(1)).schedule();
    }

    private static void tick() {
        for (Instance instance : MinecraftServer.getInstanceManager().getInstances()) {
            for (Entity entity : instance.getEntities()) {
                if (entity instanceof LivingEntity living && !living.isDead() && !(entity instanceof Player player && immune(player))) {
                    tick(instance, living);
                }
            }
        }
    }

    private static boolean immune(Player player) {
        return player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR;
    }

    private static void tick(Instance instance, LivingEntity entity) {
        Pos position = entity.getPosition();
        long age = entity.getAliveTicks();
        Block feet = instance.getBlock(position);
        Block eyes = instance.getBlock(position.add(0, entity.getEyeHeight(), 0));
        boolean inWater = feet.compare(Block.WATER) || "true".equals(feet.getProperty("waterlogged"));
        boolean inLava = feet.compare(Block.LAVA) || instance.getBlock(position.add(0, 0.3, 0)).compare(Block.LAVA);

        if (inLava && entity.getEntityType() != EntityType.STRIDER) {
            entity.setFireTicks(300);
            if (age % 10 == 0) {
                entity.damage(DamageType.LAVA, 4);
            }
        } else if ((feet.compare(Block.FIRE) || feet.compare(Block.SOUL_FIRE)) && age % 10 == 0) {
            entity.setFireTicks(Math.max(entity.getFireTicks(), 160));
            entity.damage(DamageType.IN_FIRE, 1);
        }
        if (inWater && entity.getFireTicks() > 0) {
            entity.setFireTicks(0);
        }
        if (entity.getFireTicks() > 0 && age % 20 == 0 && !inLava) {
            entity.damage(DamageType.ON_FIRE, 1);
        }

        if (entity.getEntityType() == EntityType.PLAYER || entity.getEntityType() == EntityType.ZOMBIE
                || entity.getEntityType() == EntityType.SKELETON || entity.getEntityType() == EntityType.CREEPER
                || entity.getEntityType() == EntityType.SPIDER || entity.getEntityType() == EntityType.COW
                || entity.getEntityType() == EntityType.PIG || entity.getEntityType() == EntityType.SHEEP
                || entity.getEntityType() == EntityType.CHICKEN) {
            boolean submerged = eyes.compare(Block.WATER) || "true".equals(eyes.getProperty("waterlogged"));
            var meta = entity.getEntityMeta();
            int air = meta.getAirTicks();
            if (submerged) {
                int respiration = Enchantments.level(entity.getEquipment(net.minestom.server.entity.EquipmentSlot.HELMET), "minecraft:respiration");
                if (respiration == 0 || java.util.concurrent.ThreadLocalRandom.current().nextInt(respiration + 1) == 0) {
                    air--;
                }
                if (air <= -20) {
                    air = 0;
                    entity.damage(DamageType.DROWN, 2);
                }
                meta.setAirTicks(air);
            } else if (air < MAX_AIR) {
                meta.setAirTicks(Math.min(MAX_AIR, Math.max(0, air) + 4));
            }
        }

        if (age % 10 == 0 && eyes.registry().collisionShape().isFaceFull(net.minestom.server.instance.block.BlockFace.TOP)
                && eyes.solid() && !eyes.compare(Block.GLASS) && entity.getEntityType() == EntityType.PLAYER) {
            entity.damage(DamageType.IN_WALL, 1);
        }

        if (age % 10 == 0 && touching(instance, position, Block.CACTUS)) {
            entity.damage(DamageType.CACTUS, 1);
        }
        if (age % 10 == 0 && instance.getBlock(position.add(0, -0.2, 0)).compare(Block.MAGMA_BLOCK) && !entity.isSneaking()) {
            entity.damage(DamageType.HOT_FLOOR, 1);
        }
        if (instance.getBlock(position).compare(Block.SWEET_BERRY_BUSH) && age % 20 == 0) {
            entity.damage(DamageType.SWEET_BERRY_BUSH, 1);
        }
    }

    private static boolean touching(Instance instance, Pos position, Block block) {
        double r = 0.31;
        return instance.getBlock(position.add(r, 0, 0)).compare(block) || instance.getBlock(position.add(-r, 0, 0)).compare(block)
                || instance.getBlock(position.add(0, 0, r)).compare(block) || instance.getBlock(position.add(0, 0, -r)).compare(block)
                || instance.getBlock(position.add(0, -0.1, 0)).compare(block);
    }
}
