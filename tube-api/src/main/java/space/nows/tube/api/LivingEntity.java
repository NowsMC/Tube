package space.nows.tube.api;

public interface LivingEntity extends Entity {

    double health();

    void setHealth(double health);

    double maxHealth();

    ItemStack equipment(EquipmentSlot slot);

    void setEquipment(EquipmentSlot slot, ItemStack item);

    java.util.List<PotionEffect> effects();

    void addEffect(PotionEffect effect);

    void removeEffect(String type);

    void clearEffects();
}
