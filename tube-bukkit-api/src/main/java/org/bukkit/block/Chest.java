package org.bukkit.block;

public interface Chest extends org.bukkit.block.Container, org.bukkit.loot.Lootable, org.bukkit.block.Lidded {

    org.bukkit.inventory.Inventory getBlockInventory();
}
