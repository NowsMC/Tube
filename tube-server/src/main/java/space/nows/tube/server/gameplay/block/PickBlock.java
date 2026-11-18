package space.nows.tube.server.gameplay.block;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerPickBlockEvent;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

final class PickBlock {

    private static final int HOTBAR = 9;
    private static final int STORAGE = 36;

    private PickBlock() {
    }

    static void install(GlobalEventHandler handler) {
        handler.addListener(PlayerPickBlockEvent.class, PickBlock::pick);
    }

    private static void pick(PlayerPickBlockEvent event) {
        Player player = event.getPlayer();
        GameMode mode = player.getGameMode();
        Material material = material(event.getBlock());
        if (material == null || mode != GameMode.CREATIVE && mode != GameMode.SURVIVAL) {
            return;
        }
        PlayerInventory inventory = player.getInventory();
        int found = find(inventory, material);
        if (found >= 0) {
            select(player, inventory, found);
        } else if (mode == GameMode.CREATIVE) {
            int slot = freeHotbarSlot(inventory, player.getHeldSlot());
            inventory.setItemStack(slot, ItemStack.of(material));
            player.setHeldItemSlot((byte) slot);
        }
    }

    private static Material material(Block block) {
        Material material = block.registry().material();
        if (material != null) {
            return material;
        }
        return Material.fromKey(block.key());
    }

    private static int find(PlayerInventory inventory, Material material) {
        for (int slot = 0; slot < STORAGE; slot++) {
            if (inventory.getItemStack(slot).material() == material) {
                return slot;
            }
        }
        return -1;
    }

    private static void select(Player player, PlayerInventory inventory, int found) {
        if (found < HOTBAR) {
            player.setHeldItemSlot((byte) found);
            return;
        }
        int target = freeHotbarSlot(inventory, player.getHeldSlot());
        ItemStack moved = inventory.getItemStack(found);
        inventory.setItemStack(found, inventory.getItemStack(target));
        inventory.setItemStack(target, moved);
        player.setHeldItemSlot((byte) target);
    }

    private static int freeHotbarSlot(PlayerInventory inventory, int held) {
        for (int offset = 0; offset < HOTBAR; offset++) {
            int slot = (held + offset) % HOTBAR;
            if (inventory.getItemStack(slot).isAir()) {
                return slot;
            }
        }
        return held;
    }
}
