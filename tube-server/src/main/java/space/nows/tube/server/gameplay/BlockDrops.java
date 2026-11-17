package space.nows.tube.server.gameplay;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import space.nows.tube.server.gameplay.block.BlockDropper;
import space.nows.tube.server.gameplay.loot.LootTables;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

final class BlockDrops {

    private BlockDrops() {
    }

    static void install(GlobalEventHandler handler) {
        handler.addListener(PlayerBlockBreakEvent.class, event -> {
            if (event.isCancelled()) {
                return;
            }
            Player player = event.getPlayer();
            if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
                return;
            }
            Block block = BlockDropper.lootState(event.getBlock());
            if (block.key().value().endsWith("shulker_box")) {
                return;
            }
            ItemStack tool = player.getItemInMainHand();
            var position = event.getBlockPosition().add(0.5, 0.25, 0.5);
            for (ItemStack drop : dropsFor(block, tool)) {
                Drops.spawn(event.getInstance(), position, drop);
            }
            int xp = experienceFor(block, tool);
            if (xp > 0) {
                Experience.spawn(event.getInstance(), position, xp);
            }
            if (block.registry().hardness() > 0 && tool.has(DataComponents.TOOL)) {
                Durability.damage(player, EquipmentSlot.MAIN_HAND, 1);
            }
            Hunger.exhaust(player, 0.005f);
        });
    }

    static List<ItemStack> dropsFor(Block block, ItemStack tool) {
        if (block.registry().requiresTool()) {
            var rules = tool.get(DataComponents.TOOL);
            if (rules == null || !rules.isCorrectForDrops(block)) {
                return List.of();
            }
        }
        return LootTables.forBlock(block, LootTables.Context.block(block, tool, ThreadLocalRandom.current()));
    }

    static int experienceFor(Block block, ItemStack tool) {
        if (LootTables.level(tool, "minecraft:silk_touch") > 0 || dropsFor(block, tool).isEmpty() && block.registry().requiresTool()) {
            return 0;
        }
        String key = block.key().value().replace("deepslate_", "");
        return switch (key) {
            case "coal_ore" -> Experience.between(0, 2);
            case "diamond_ore", "emerald_ore" -> Experience.between(3, 7);
            case "lapis_ore", "nether_quartz_ore" -> Experience.between(2, 5);
            case "redstone_ore" -> Experience.between(1, 5);
            case "nether_gold_ore" -> Experience.between(0, 1);
            default -> 0;
        };
    }
}
