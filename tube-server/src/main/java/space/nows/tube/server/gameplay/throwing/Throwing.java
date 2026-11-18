package space.nows.tube.server.gameplay.throwing;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.network.packet.server.play.SetCooldownPacket;
import net.minestom.server.tag.Tag;
import space.nows.tube.server.gameplay.SweptProjectile;
import space.nows.tube.server.world.effect.SoundPlayer;

import java.util.concurrent.ThreadLocalRandom;

public final class Throwing {

    private static final Tag<Long> READY_AT = Tag.Transient("tube:pearl_ready");
    private static final double SPEED = 1.5;
    private static final double SPREAD = 1;

    private Throwing() {
    }

    public static void install(GlobalEventHandler handler) {
        handler.addListener(PlayerUseItemEvent.class, event -> ThrownKind.of(event.getItemStack().material()).ifPresent(kind -> {
            Player player = event.getPlayer();
            if (cooling(player, kind)) {
                event.setCancelled(true);
                return;
            }
            launch(player, kind);
            if (player.getGameMode() != GameMode.CREATIVE) {
                player.setItemInHand(event.getHand(), event.getItemStack().consume(1));
            }
            startCooldown(player, kind);
        }));
        Impacts.install(handler);
    }

    private static boolean cooling(Player player, ThrownKind kind) {
        Long readyAt = player.getTag(READY_AT);
        return kind.cooldownTicks() > 0 && readyAt != null && player.getAliveTicks() < readyAt;
    }

    private static void startCooldown(Player player, ThrownKind kind) {
        if (kind.cooldownTicks() > 0) {
            player.setTag(READY_AT, player.getAliveTicks() + kind.cooldownTicks());
            player.sendPacket(new SetCooldownPacket(kind.item().key().asString(), kind.cooldownTicks()));
        }
    }

    private static void launch(Player player, ThrownKind kind) {
        Pos eye = player.getPosition().add(0, player.getEyeHeight(), 0);
        SweptProjectile projectile = new SweptProjectile(player, kind.entity());
        projectile.launch(player.getInstance(), eye, eye.direction(), SPEED, SPREAD);
        float pitch = 0.4f / (ThreadLocalRandom.current().nextFloat() * 0.4f + 0.8f);
        SoundPlayer.play(player.getInstance(), kind.sound(), Sound.Source.PLAYER, player.getPosition(), 0.5f, pitch);
    }
}
