package org.bukkit.inventory.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class SimpleItemMeta implements ItemMeta {

    private String displayName;
    private List<String> lore;

    @Override
    public boolean hasDisplayName() {
        return displayName != null;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String name) {
        this.displayName = name;
    }

    @Override
    public boolean hasLore() {
        return lore != null && !lore.isEmpty();
    }

    @Override
    public List<String> getLore() {
        return lore == null ? null : new ArrayList<>(lore);
    }

    @Override
    public void setLore(List<String> lore) {
        this.lore = lore == null ? null : new ArrayList<>(lore);
    }

    @Override
    public ItemMeta clone() {
        SimpleItemMeta copy = new SimpleItemMeta();
        copy.displayName = displayName;
        copy.lore = lore == null ? null : new ArrayList<>(lore);
        return copy;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SimpleItemMeta meta && Objects.equals(displayName, meta.displayName) && Objects.equals(lore, meta.lore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayName, lore);
    }
}
