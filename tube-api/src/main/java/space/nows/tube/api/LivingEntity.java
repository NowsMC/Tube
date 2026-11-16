package space.nows.tube.api;

public interface LivingEntity extends Entity {

    double health();

    void setHealth(double health);

    double maxHealth();

    ItemStack equipment(EquipmentSlot slot);

    void setEquipment(EquipmentSlot slot, ItemStack item);
}
