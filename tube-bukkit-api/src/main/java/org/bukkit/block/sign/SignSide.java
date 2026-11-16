package org.bukkit.block.sign;

public interface SignSide extends org.bukkit.material.Colorable {

    java.lang.String[] getLines();

    java.lang.String getLine(int p0);

    void setLine(int p0, java.lang.String p1);

    boolean isGlowingText();

    void setGlowingText(boolean p0);
}
