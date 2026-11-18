package space.nows.tube.server.mob.loot;

import net.minestom.server.color.DyeColor;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import space.nows.tube.server.gameplay.loot.LootTables;
import space.nows.tube.server.mob.Creature;

import java.util.Locale;

final class SheepLoot {

    private SheepLoot() {
    }

    static LootTables.Context describe(Creature creature, LootTables.Context context) {
        if (!(creature.getEntityMeta() instanceof SheepMeta sheep)) {
            return context;
        }
        DyeColor color = creature.get(DataComponents.SHEEP_COLOR);
        String name = (color == null ? DyeColor.WHITE : color).name().toLowerCase(Locale.ROOT);
        return context.withSheep(name, sheep.isSheared());
    }
}
