package space.nows.tube.server;

import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.Material;
import space.nows.tube.api.ItemStack;

import java.util.List;

final class ItemMapper {

    private ItemMapper() {
    }

    static ItemStack fromMinestom(net.minestom.server.item.ItemStack stack) {
        if (stack.isAir()) {
            return ItemStack.AIR;
        }
        Component name = stack.get(DataComponents.CUSTOM_NAME);
        List<Component> lore = stack.get(DataComponents.LORE);
        return new ItemStack(stack.material().key().asString(), stack.amount(), name, lore == null ? List.of() : lore);
    }

    static net.minestom.server.item.ItemStack toMinestom(ItemStack stack) {
        if (stack.isAir()) {
            return net.minestom.server.item.ItemStack.AIR;
        }
        Material material = Material.fromKey(stack.type());
        if (material == null) {
            throw new IllegalArgumentException("Unknown item: " + stack.type());
        }
        net.minestom.server.item.ItemStack result = net.minestom.server.item.ItemStack.of(material, stack.amount());
        if (stack.name() != null) {
            result = result.withCustomName(stack.name());
        }
        if (!stack.lore().isEmpty()) {
            result = result.with(DataComponents.LORE, stack.lore());
        }
        return result;
    }
}
