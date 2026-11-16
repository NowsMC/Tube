package space.nows.tube.api.inventory;

public interface Inventory {

    int size();

    ItemStack item(int slot);

    void setItem(int slot, ItemStack item);

    boolean add(ItemStack item);

    void clear();
}
