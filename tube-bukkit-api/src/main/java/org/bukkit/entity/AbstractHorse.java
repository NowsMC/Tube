package org.bukkit.entity;

public interface AbstractHorse extends org.bukkit.entity.Vehicle, org.bukkit.inventory.InventoryHolder, org.bukkit.entity.Tameable {

    int getDomestication();

    void setDomestication(int p0);

    int getMaxDomestication();

    void setMaxDomestication(int p0);

    double getJumpStrength();

    void setJumpStrength(double p0);
}
