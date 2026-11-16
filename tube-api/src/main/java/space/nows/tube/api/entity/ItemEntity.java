package space.nows.tube.api.entity;

import space.nows.tube.api.inventory.ItemStack;

public interface ItemEntity extends Entity {

    ItemStack item();

    void setItem(ItemStack item);
}
