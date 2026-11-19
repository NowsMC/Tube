package space.nows.tube.server.mob.kind.monster.silverfish;

import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import space.nows.tube.server.item.Enchantments;

public final class InfestedBreak {

    private InfestedBreak() {
    }

    public static void install(GlobalEventHandler handler) {
        handler.addListener(PlayerBlockBreakEvent.class, event -> {
            if (Infestation.infested(event.getBlock())
                    && Enchantments.level(event.getPlayer().getItemInMainHand(), "minecraft:silk_touch") == 0) {
                Infestation.release(event.getInstance(), event.getBlockPosition());
            }
        });
    }
}
