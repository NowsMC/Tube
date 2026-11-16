package space.nows.tube.api.event;

import space.nows.tube.api.entity.Player;
import space.nows.tube.api.world.Block;
import space.nows.tube.api.world.World;

public final class BlockBreakEvent implements Cancellable {

    private final Player player;
    private final World world;
    private final int x;
    private final int y;
    private final int z;
    private final Block block;
    private boolean cancelled;

    public BlockBreakEvent(Player player, World world, int x, int y, int z, Block block) {
        this.player = player;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.block = block;
    }

    public Player player() {
        return player;
    }

    public World world() {
        return world;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    public Block block() {
        return block;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
