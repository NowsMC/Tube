package space.nows.tube.api.event;

import space.nows.tube.api.entity.Player;
import space.nows.tube.api.world.Block;
import space.nows.tube.api.world.World;

public final class PlayerInteractEvent implements Cancellable {

    private final Player player;
    private final World world;
    private final int x;
    private final int y;
    private final int z;
    private final Block block;
    private final boolean mainHand;
    private final InteractAction action;
    private boolean cancelled;

    public PlayerInteractEvent(Player player, World world, int x, int y, int z, Block block, boolean mainHand) {
        this(player, world, x, y, z, block, mainHand, InteractAction.RIGHT_CLICK_BLOCK);
    }

    public PlayerInteractEvent(Player player, World world, int x, int y, int z, Block block, boolean mainHand, InteractAction action) {
        this.action = action;
        this.player = player;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.block = block;
        this.mainHand = mainHand;
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

    public InteractAction action() {
        return action;
    }

    public boolean mainHand() {
        return mainHand;
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
