package space.nows.tube.server;

import net.minestom.server.inventory.AbstractInventory;
import space.nows.tube.api.Inventory;
import space.nows.tube.api.ItemStack;

final class TubeInventory implements Inventory {

    private final AbstractInventory handle;

    TubeInventory(AbstractInventory handle) {
        this.handle = handle;
    }

    AbstractInventory handle() {
        return handle;
    }

    @Override
    public int size() {
        return handle.getSize();
    }

    @Override
    public ItemStack item(int slot) {
        return ItemMapper.fromMinestom(handle.getItemStack(slot));
    }

    @Override
    public void setItem(int slot, ItemStack item) {
        handle.setItemStack(slot, ItemMapper.toMinestom(item));
    }

    @Override
    public boolean add(ItemStack item) {
        return handle.addItemStack(ItemMapper.toMinestom(item));
    }

    @Override
    public void clear() {
        handle.clear();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TubeInventory inventory && inventory.handle == handle;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(handle);
    }
}
