package space.nows.tube.server.loot.function;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import space.nows.tube.server.loot.LootContext;

final class FurnaceSmelt implements LootFunction {

    @Override
    public ItemStack apply(ItemStack item, LootContext context) {
        Material cooked = switch (item.material().key().asString()) {
            case "minecraft:beef" -> Material.COOKED_BEEF;
            case "minecraft:porkchop" -> Material.COOKED_PORKCHOP;
            case "minecraft:chicken" -> Material.COOKED_CHICKEN;
            case "minecraft:mutton" -> Material.COOKED_MUTTON;
            case "minecraft:rabbit" -> Material.COOKED_RABBIT;
            case "minecraft:cod" -> Material.COOKED_COD;
            case "minecraft:salmon" -> Material.COOKED_SALMON;
            default -> null;
        };
        return cooked == null ? item : ItemStack.of(cooked, item.amount());
    }
}
