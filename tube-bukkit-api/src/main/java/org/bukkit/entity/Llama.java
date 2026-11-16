package org.bukkit.entity;

public interface Llama extends org.bukkit.entity.ChestedHorse {

    org.bukkit.entity.Llama.Color getColor();

    void setColor(org.bukkit.entity.Llama.Color p0);

    org.bukkit.inventory.LlamaInventory getInventory();

    public enum Color {
        CREAMY,
        WHITE,
        BROWN,
        GRAY
    }
}
