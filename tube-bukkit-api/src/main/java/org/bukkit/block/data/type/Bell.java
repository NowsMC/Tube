package org.bukkit.block.data.type;

public interface Bell extends org.bukkit.block.data.Directional, org.bukkit.block.data.Powerable {

    org.bukkit.block.data.type.Bell.Attachment getAttachment();

    public enum Attachment {
        FLOOR,
        CEILING,
        SINGLE_WALL,
        DOUBLE_WALL
    }
}
