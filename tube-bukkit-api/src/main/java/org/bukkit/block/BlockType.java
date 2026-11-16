package org.bukkit.block;

public interface BlockType extends org.bukkit.Keyed, org.bukkit.Translatable, org.bukkit.registry.RegistryAware {

    org.bukkit.block.data.BlockData createBlockData();

    org.bukkit.block.data.BlockData createBlockData(java.lang.String p0);

    public interface Typed<B extends org.bukkit.block.data.BlockData> extends org.bukkit.block.BlockType {
    }
}
