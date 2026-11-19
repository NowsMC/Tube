package space.nows.tube.server.gameplay.fishing;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import space.nows.tube.server.gameplay.Durability;
import space.nows.tube.server.item.Enchantments;
import space.nows.tube.server.world.effect.Pitch;
import space.nows.tube.server.world.effect.SoundPlayer;

import java.util.concurrent.ThreadLocalRandom;

public final class Fishing {

    private static final Tag<Bobber> BOBBER = Tag.Transient("tube:bobber");

    private Fishing() {
    }

    public static void install(GlobalEventHandler handler) {
        handler.addListener(PlayerUseItemEvent.class, event -> {
            if (event.getItemStack().material() == Material.FISHING_ROD) {
                use(event.getPlayer(), event.getHand(), event.getItemStack());
            }
        });
        handler.addListener(PlayerDisconnectEvent.class, event -> retract(event.getPlayer()));
    }

    private static void use(Player player, PlayerHand hand, ItemStack rod) {
        Bobber cast = player.getTag(BOBBER);
        if (cast != null && !cast.isRemoved()) {
            int damage = cast.reel();
            player.removeTag(BOBBER);
            Durability.damage(player, slot(hand), damage);
            SoundPlayer.play(player, "minecraft:entity.fishing_bobber.retrieve", Sound.Source.NEUTRAL, 1f, Pitch.thrown(ThreadLocalRandom.current()));
            return;
        }
        Bobber bobber = new Bobber(player, Enchantments.level(rod, "minecraft:lure"), Enchantments.level(rod, "minecraft:luck_of_the_sea"));
        bobber.cast(player.getPosition().direction());
        player.setTag(BOBBER, bobber);
        SoundPlayer.play(player, "minecraft:entity.fishing_bobber.throw", Sound.Source.NEUTRAL, 0.5f, Pitch.thrown(ThreadLocalRandom.current()));
    }

    private static void retract(Player player) {
        Bobber cast = player.getTag(BOBBER);
        if (cast != null) {
            cast.remove();
            player.removeTag(BOBBER);
        }
    }

    private static EquipmentSlot slot(PlayerHand hand) {
        return hand == PlayerHand.MAIN ? EquipmentSlot.MAIN_HAND : EquipmentSlot.OFF_HAND;
    }
}
