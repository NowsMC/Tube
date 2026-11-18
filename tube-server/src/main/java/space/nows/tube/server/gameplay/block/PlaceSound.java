package space.nows.tube.server.gameplay.block;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.block.BlockSoundType;
import net.minestom.server.network.packet.server.play.SoundEffectPacket;

public final class PlaceSound {

    private PlaceSound() {
    }

    public static void install(GlobalEventHandler handler) {
        handler.addListener(PlayerBlockPlaceEvent.class, event -> {
            if (event.isCancelled()) {
                return;
            }
            BlockSoundType type = event.getBlock().blockSoundType();
            if (type == null || type.placeSound() == null) {
                return;
            }
            Player placer = event.getPlayer();
            SoundEffectPacket packet = new SoundEffectPacket(type.placeSound(), Sound.Source.BLOCK,
                    event.getBlockPosition().add(0.5), (type.volume() + 1) / 2, type.pitch() * 0.8f, 0);
            for (Player viewer : placer.getViewers()) {
                viewer.sendPacket(packet);
            }
        });
    }
}
