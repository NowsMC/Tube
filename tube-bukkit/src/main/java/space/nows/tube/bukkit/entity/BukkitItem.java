package space.nows.tube.bukkit.entity;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import space.nows.tube.api.entity.ItemEntity;
import space.nows.tube.bukkit.BukkitRuntime;
import space.nows.tube.bukkit.inventory.ItemConverter;

public final class BukkitItem extends BukkitEntity implements Item {

    private final ItemEntity tube;

    public BukkitItem(BukkitRuntime runtime, ItemEntity tube) {
        super(runtime, tube);
        this.tube = tube;
    }

    @Override
    public EntityType getType() {
        return EntityType.ITEM;
    }

    @Override
    public ItemStack getItemStack() {
        return ItemConverter.nonNull(ItemConverter.fromTube(tube.item()));
    }

    @Override
    public void setItemStack(ItemStack stack) {
        tube.setItem(ItemConverter.toTube(stack));
    }
}
