package org.bukkit.inventory.meta;

public final class ItemMetas {

    private ItemMetas() {
    }

    public static ItemMeta create() {
        return new SimpleItemMeta();
    }
}
