package space.nows.tube.server.gameplay.melee;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.sound.SoundEvent;
import space.nows.tube.server.gameplay.Falling;
import space.nows.tube.server.registry.RegistryTags;
import space.nows.tube.server.world.effect.SoundPlayer;

public final class CriticalHit {

    public static final float MULTIPLIER = 1.5f;
    private static final double FULL_CHARGE = 0.9;

    private CriticalHit() {
    }

    public static boolean lands(Player attacker, Entity target, double charge) {
        return charge > FULL_CHARGE
                && target instanceof LivingEntity
                && Falling.distance(attacker) > 0
                && !attacker.isOnGround()
                && !attacker.isFlying()
                && !attacker.isSprinting()
                && attacker.getVehicle() == null
                && !attacker.hasEffect(PotionEffect.BLINDNESS)
                && !climbing(attacker)
                && !swimming(attacker);
    }

    public static void show(Player attacker, Entity target) {
        SoundPlayer.play(attacker.getInstance(), SoundEvent.ENTITY_PLAYER_ATTACK_CRIT, Sound.Source.PLAYER, attacker.getPosition(), 1f, 1f);
        target.sendPacketToViewersAndSelf(new EntityAnimationPacket(target.getEntityId(), EntityAnimationPacket.Animation.CRITICAL_EFFECT));
    }

    private static boolean climbing(Player player) {
        return RegistryTags.has(player.getInstance().getBlock(player.getPosition()), "minecraft:climbable");
    }

    private static boolean swimming(Player player) {
        return player.getInstance().getBlock(player.getPosition()).liquid();
    }
}
