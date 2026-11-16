package org.bukkit.block.data;

import org.bukkit.Material;

public interface BlockData extends Cloneable {

    Material getMaterial();

    Material getPlacementMaterial();

    String getAsString();

    String getAsString(boolean hideUnspecified);

    boolean matches(BlockData other);

    BlockData clone();
}
