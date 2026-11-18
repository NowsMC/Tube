package space.nows.tube.server.loot.entry;

import net.minestom.server.item.ItemStack;
import space.nows.tube.server.loot.LootContext;

import java.util.List;

public interface LootEntry {

    void expand(LootContext context, List<Leaf> out);

    interface Leaf extends LootEntry {

        int weight(LootContext context);

        void produce(LootContext context, List<ItemStack> out);
    }
}
