package org.bukkit.block.data;

public interface FaceAttachable extends org.bukkit.block.data.BlockData {

    org.bukkit.block.data.FaceAttachable.AttachedFace getAttachedFace();

    public enum AttachedFace {
        FLOOR,
        WALL,
        CEILING
    }
}
