package org.bukkit.block;

public interface Sign extends org.bukkit.block.TileState, org.bukkit.material.Colorable {

    java.lang.String[] getLines();

    java.lang.String getLine(int p0);

    void setLine(int p0, java.lang.String p1);

    boolean isWaxed();

    void setWaxed(boolean p0);

    boolean isGlowingText();

    void setGlowingText(boolean p0);

    org.bukkit.DyeColor getColor();

    void setColor(org.bukkit.DyeColor p0);

    org.bukkit.block.sign.SignSide getSide(org.bukkit.block.sign.Side p0);
}
