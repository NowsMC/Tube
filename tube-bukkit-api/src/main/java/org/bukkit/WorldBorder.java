package org.bukkit;

public interface WorldBorder {

    void reset();

    double getSize();

    void setSize(double p0);

    void setSize(double p0, long p1);

    org.bukkit.Location getCenter();

    void setCenter(double p0, double p1);

    double getDamageBuffer();

    void setDamageBuffer(double p0);

    double getDamageAmount();

    void setDamageAmount(double p0);

    int getWarningTime();

    void setWarningTime(int p0);

    int getWarningDistance();

    void setWarningDistance(int p0);

    boolean isInside(org.bukkit.Location p0);
}
