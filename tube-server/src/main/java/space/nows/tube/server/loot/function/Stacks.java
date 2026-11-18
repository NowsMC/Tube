package space.nows.tube.server.loot.function;

import net.minestom.server.item.ItemStack;

final class Stacks {

    private Stacks() {
    }

    static ItemStack withCount(ItemStack item, int count) {
        if (count <= 0) {
            return ItemStack.AIR;
        }
        return item.withAmount(Math.min(count, item.maxStackSize()));
    }
}
