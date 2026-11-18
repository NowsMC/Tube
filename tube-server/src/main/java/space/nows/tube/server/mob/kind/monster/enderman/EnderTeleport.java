package space.nows.tube.server.mob.kind.monster.enderman;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.move.Liquids;
import space.nows.tube.server.world.effect.SoundPlayer;

final class EnderTeleport {

    private static final double RANDOM_SPREAD = 64.0;
    private static final int RANDOM_HEIGHT = 64;
    private static final double TOWARDS_SPREAD = 8.0;
    private static final int TOWARDS_HEIGHT = 16;
    private static final double TOWARDS_DISTANCE = 16.0;
    private static final byte PORTAL_EVENT = 46;
    private static final String SOUND = "minecraft:entity.enderman.teleport";

    private EnderTeleport() {
    }

    static boolean randomly(Creature enderman) {
        Pos at = enderman.getPosition();
        return to(enderman, at.x() + (enderman.random().nextDouble() - 0.5) * RANDOM_SPREAD,
                at.y() + (enderman.random().nextInt(RANDOM_HEIGHT) - RANDOM_HEIGHT / 2),
                at.z() + (enderman.random().nextDouble() - 0.5) * RANDOM_SPREAD);
    }

    static boolean towards(Creature enderman, Entity target) {
        Pos at = enderman.getPosition();
        Pos other = target.getPosition();
        Vec direction = new Vec(at.x() - other.x(), at.y() + enderman.getBoundingBox().height() / 2 - (other.y() + target.getEyeHeight()),
                at.z() - other.z()).normalize();
        return to(enderman, at.x() + (enderman.random().nextDouble() - 0.5) * TOWARDS_SPREAD - direction.x() * TOWARDS_DISTANCE,
                at.y() + (enderman.random().nextInt(TOWARDS_HEIGHT) - TOWARDS_HEIGHT / 2) - direction.y() * TOWARDS_DISTANCE,
                at.z() + (enderman.random().nextDouble() - 0.5) * TOWARDS_SPREAD - direction.z() * TOWARDS_DISTANCE);
    }

    private static boolean to(Creature enderman, double x, double y, double z) {
        if (enderman.isDead()) {
            return false;
        }
        Instance instance = enderman.getInstance();
        int minY = instance.getCachedDimensionType().minY();
        int blockY = (int) Math.floor(y);
        while (blockY > minY && !instance.getBlock((int) Math.floor(x), blockY, (int) Math.floor(z)).solid()) {
            blockY--;
        }
        Block ground = instance.getBlock((int) Math.floor(x), blockY, (int) Math.floor(z));
        if (!ground.solid() || Liquids.isWater(ground)) {
            return false;
        }
        Pos landing = new Pos(x, blockY + 1, z, enderman.getPosition().yaw(), enderman.getPosition().pitch());
        if (!instance.isChunkLoaded(landing) || !clear(instance, landing, enderman)) {
            return false;
        }
        Pos from = enderman.getPosition();
        enderman.teleport(landing);
        enderman.navigation().stop();
        enderman.triggerStatus(PORTAL_EVENT);
        SoundPlayer.play(instance, SOUND, Sound.Source.HOSTILE, from, 1f, 1f);
        SoundPlayer.play(instance, SOUND, Sound.Source.HOSTILE, landing, 1f, 1f);
        return true;
    }

    private static boolean clear(Instance instance, Pos feet, Creature enderman) {
        int height = (int) Math.ceil(enderman.getBoundingBox().height());
        for (int dy = 0; dy < height; dy++) {
            Block block = instance.getBlock(feet.blockX(), feet.blockY() + dy, feet.blockZ());
            if (block.solid() || block.liquid() || Liquids.isWater(block)) {
                return false;
            }
        }
        return true;
    }
}
