package space.nows.tube.api.entity;

import space.nows.tube.api.entity.effect.PotionEffect;
import space.nows.tube.api.inventory.EquipmentSlot;
import space.nows.tube.api.inventory.ItemStack;

public interface LivingEntity extends Entity {

    double health();

    int fireTicks();

    void setFireTicks(int ticks);

    boolean isGliding();

    double attribute(String key);

    double baseAttribute(String key);

    double defaultAttribute(String key);

    void setBaseAttribute(String key, double value);

    boolean hasLineOfSight(Entity other);

    void setHealth(double health);

    double maxHealth();

    void damage(double amount);

    ItemStack equipment(EquipmentSlot slot);

    void setEquipment(EquipmentSlot slot, ItemStack item);

    java.util.List<PotionEffect> effects();

    void addEffect(PotionEffect effect);

    void removeEffect(String type);

    void clearEffects();
}
