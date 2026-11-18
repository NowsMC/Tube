package space.nows.tube.server.mob.interact;

import net.minestom.server.color.DyeColor;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import space.nows.tube.server.gameplay.Durability;
import space.nows.tube.server.gameplay.block.BlockDropper;
import space.nows.tube.server.mob.Creature;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

final class Shearing {

    private Shearing() {
    }

    static boolean apply(Player player, Creature creature) {
        if (player.getItemInMainHand().material() != Material.SHEARS || !(creature.getEntityMeta() instanceof SheepMeta sheep) || sheep.isSheared()) {
            return false;
        }
        sheep.setSheared(true);
        DyeColor color = creature.get(DataComponents.SHEEP_COLOR);
        Material wool = Material.fromKey("minecraft:" + (color == null ? DyeColor.WHITE : color).name().toLowerCase(Locale.ROOT) + "_wool");
        int count = 1 + ThreadLocalRandom.current().nextInt(3);
        BlockDropper.spawn(creature.getInstance(), creature.getPosition().add(0, 1, 0), ItemStack.of(wool == null ? Material.WHITE_WOOL : wool, count));
        Durability.damage(player, EquipmentSlot.MAIN_HAND, 1);
        return true;
    }
}
