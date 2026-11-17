package space.nows.tube.server.event;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;

import java.util.List;

@FunctionalInterface
public interface DeathDrops {

    DeathDrops NONE = entity -> new Drops(List.of(), 0);

    record Drops(List<ItemStack> items, int experience) {
    }

    Drops collect(LivingEntity entity);
}
