package space.nows.tube.server.loot.function;

import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.PotionContents;
import net.minestom.server.potion.PotionType;
import space.nows.tube.server.loot.LootContext;

record SetPotion(PotionType type) implements LootFunction {

    @Override
    public ItemStack apply(ItemStack item, LootContext context) {
        return item.with(DataComponents.POTION_CONTENTS, new PotionContents(type));
    }
}
