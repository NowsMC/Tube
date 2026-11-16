package org.bukkit.event.block;

public class BlockMultiPlaceEvent extends org.bukkit.event.block.BlockPlaceEvent {
    private java.util.List<org.bukkit.block.BlockState> states;

    public BlockMultiPlaceEvent(java.util.List<org.bukkit.block.BlockState> p0, org.bukkit.block.Block p1, org.bukkit.inventory.ItemStack p2, org.bukkit.entity.Player p3, boolean p4) {
        super(p0.get(0).getBlock(), p0.get(0), p1, p2, p3, p4);
        this.states = java.util.List.copyOf(p0);
    }

    public java.util.List<org.bukkit.block.BlockState> getReplacedBlockStates() {
        return states;
    }
}
