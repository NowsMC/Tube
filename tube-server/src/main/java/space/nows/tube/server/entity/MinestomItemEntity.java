package space.nows.tube.server.entity;

import space.nows.tube.api.entity.ItemEntity;
import space.nows.tube.api.inventory.ItemStack;
import space.nows.tube.server.inventory.ItemMapper;
import space.nows.tube.server.world.WorldService;

public final class MinestomItemEntity extends MinestomEntity implements ItemEntity {

    private final net.minestom.server.entity.ItemEntity item;

    public MinestomItemEntity(net.minestom.server.entity.ItemEntity handle, WorldService worlds) {
        super(handle, worlds);
        this.item = handle;
    }

    @Override
    public net.minestom.server.entity.ItemEntity minestom() {
        return item;
    }

    @Override
    public ItemStack item() {
        return ItemMapper.fromMinestom(item.getItemStack());
    }

    @Override
    public void setItem(ItemStack stack) {
        item.setItemStack(ItemMapper.toMinestom(stack));
    }
}
