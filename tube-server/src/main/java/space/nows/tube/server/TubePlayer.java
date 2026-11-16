package space.nows.tube.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import space.nows.tube.api.GameMode;
import space.nows.tube.api.Inventory;
import space.nows.tube.api.Player;

import java.net.InetSocketAddress;
import java.util.Optional;

final class TubePlayer extends TubeLivingEntity implements Player {

    private final net.minestom.server.entity.Player player;
    private final PermissionService permissions;

    TubePlayer(net.minestom.server.entity.Player player, WorldService worlds, PermissionService permissions) {
        super(player, worlds);
        this.player = player;
        this.permissions = permissions;
    }

    @Override
    public String name() {
        return player.getUsername();
    }

    @Override
    public boolean hasPermission(String node) {
        return permissions.has(player.getUuid(), node);
    }

    @Override
    public Optional<InetSocketAddress> address() {
        return player.getPlayerConnection().getRemoteAddress() instanceof InetSocketAddress address ? Optional.of(address) : Optional.empty();
    }

    @Override
    public GameMode gameMode() {
        return GameMode.valueOf(player.getGameMode().name());
    }

    @Override
    public void setGameMode(GameMode gameMode) {
        player.setGameMode(net.minestom.server.entity.GameMode.valueOf(gameMode.name()));
    }

    @Override
    public Inventory inventory() {
        return new TubeInventory(player.getInventory());
    }

    @Override
    public int food() {
        return player.getFood();
    }

    @Override
    public void setFood(int food) {
        player.setFood(food);
    }

    @Override
    public int level() {
        return player.getLevel();
    }

    @Override
    public void setLevel(int level) {
        player.setLevel(level);
    }

    @Override
    public int heldSlot() {
        return player.getHeldSlot();
    }

    @Override
    public void setHeldSlot(int slot) {
        if (slot < 0 || slot > 8) {
            throw new IllegalArgumentException("Held slot must be between 0 and 8");
        }
        player.setHeldItemSlot((byte) slot);
    }

    @Override
    public void openInventory(Inventory inventory) {
        if (inventory instanceof TubeInventory tube && tube.handle() instanceof net.minestom.server.inventory.Inventory container) {
            player.openInventory(container);
            return;
        }
        throw new IllegalArgumentException("Only inventories created by the server can be opened");
    }

    @Override
    public void closeInventory() {
        player.closeInventory();
    }

    @Override
    public void sendActionBar(Component message) {
        player.sendActionBar(message);
    }

    @Override
    public void showTitle(Component title, Component subtitle) {
        player.showTitle(Title.title(title, subtitle));
    }

    @Override
    public void kick(Component reason) {
        player.kick(reason);
    }

    @Override
    public void remove() {
        player.kick(Component.text("Removed from the server"));
    }

    @Override
    public void sendMessage(Component message) {
        player.sendMessage(message);
    }
}
