package org.bukkit.attribute;

public interface AttributeInstance {

    org.bukkit.attribute.Attribute getAttribute();

    double getBaseValue();

    void setBaseValue(double p0);


    double getValue();

    double getDefaultValue();
}
