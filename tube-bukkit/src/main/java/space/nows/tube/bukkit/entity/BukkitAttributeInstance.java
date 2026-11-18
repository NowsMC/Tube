package space.nows.tube.bukkit.entity;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;

final class BukkitAttributeInstance implements AttributeInstance {

    private final space.nows.tube.api.entity.LivingEntity tube;
    private final Attribute attribute;
    private final String key;

    BukkitAttributeInstance(space.nows.tube.api.entity.LivingEntity tube, Attribute attribute) {
        this.tube = tube;
        this.attribute = attribute;
        this.key = attribute.getKey().toString();
    }

    @Override
    public Attribute getAttribute() {
        return attribute;
    }

    @Override
    public double getBaseValue() {
        return tube.baseAttribute(key);
    }

    @Override
    public void setBaseValue(double value) {
        tube.setBaseAttribute(key, value);
    }

    @Override
    public double getValue() {
        return tube.attribute(key);
    }

    @Override
    public double getDefaultValue() {
        return tube.defaultAttribute(key);
    }
}
