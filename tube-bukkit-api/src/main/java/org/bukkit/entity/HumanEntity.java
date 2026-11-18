package org.bukkit.entity;

import org.bukkit.GameMode;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public interface HumanEntity extends LivingEntity, InventoryHolder {

    GameMode getGameMode();

    void setGameMode(GameMode mode);

    @Override
    PlayerInventory getInventory();

    ItemStack getItemInHand();

    org.bukkit.inventory.InventoryView getOpenInventory();

    void setItemInHand(ItemStack item);
}
