package space.nows.tube.server;

import net.minestom.server.entity.attribute.Attribute;
import space.nows.tube.api.EquipmentSlot;
import space.nows.tube.api.ItemStack;
import space.nows.tube.api.LivingEntity;

class TubeLivingEntity extends TubeEntity implements LivingEntity {

    private final net.minestom.server.entity.LivingEntity living;

    TubeLivingEntity(net.minestom.server.entity.LivingEntity handle, WorldService worlds) {
        super(handle, worlds);
        this.living = handle;
    }

    @Override
    net.minestom.server.entity.LivingEntity minestom() {
        return living;
    }

    @Override
    public double health() {
        return living.getHealth();
    }

    @Override
    public void setHealth(double health) {
        living.setHealth((float) health);
    }

    @Override
    public double maxHealth() {
        return living.getAttributeValue(Attribute.MAX_HEALTH);
    }

    @Override
    public ItemStack equipment(EquipmentSlot slot) {
        return ItemMapper.fromMinestom(living.getEquipment(toMinestom(slot)));
    }

    @Override
    public void setEquipment(EquipmentSlot slot, ItemStack item) {
        living.setEquipment(toMinestom(slot), ItemMapper.toMinestom(item));
    }

    private static net.minestom.server.entity.EquipmentSlot toMinestom(EquipmentSlot slot) {
        return switch (slot) {
            case MAIN_HAND -> net.minestom.server.entity.EquipmentSlot.MAIN_HAND;
            case OFF_HAND -> net.minestom.server.entity.EquipmentSlot.OFF_HAND;
            case HEAD -> net.minestom.server.entity.EquipmentSlot.HELMET;
            case CHEST -> net.minestom.server.entity.EquipmentSlot.CHESTPLATE;
            case LEGS -> net.minestom.server.entity.EquipmentSlot.LEGGINGS;
            case FEET -> net.minestom.server.entity.EquipmentSlot.BOOTS;
        };
    }
}
