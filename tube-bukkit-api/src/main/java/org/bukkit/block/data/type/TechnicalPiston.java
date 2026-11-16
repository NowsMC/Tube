package org.bukkit.block.data.type;

public interface TechnicalPiston extends org.bukkit.block.data.Directional {

    org.bukkit.block.data.type.TechnicalPiston.Type getType();

    public enum Type {
        NORMAL,
        STICKY
    }
}
