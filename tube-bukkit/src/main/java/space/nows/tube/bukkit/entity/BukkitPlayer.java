package space.nows.tube.bukkit.entity;

import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import space.nows.tube.bukkit.BukkitRuntime;
import space.nows.tube.bukkit.inventory.BukkitPlayerInventory;

import java.net.InetSocketAddress;
import java.util.Set;

public final class BukkitPlayer extends BukkitLivingEntity implements Player {

    private final space.nows.tube.api.Player tube;

    public BukkitPlayer(BukkitRuntime runtime, space.nows.tube.api.Player tube) {
        super(runtime, tube);
        this.tube = tube;
    }

    @Override
    protected boolean granted(String node) {
        return tube.hasPermission(node);
    }

    @Override
    public String getName() {
        return tube.name();
    }

    @Override
    public String getDisplayName() {
        return tube.name();
    }

    @Override
    public String getPlayerListName() {
        return tube.name();
    }

    @Override
    public boolean isOnline() {
        return !tube.isRemoved();
    }

    @Override
    public Player getPlayer() {
        return this;
    }

    @Override
    public EntityType getType() {
        return EntityType.PLAYER;
    }

    @Override
    public GameMode getGameMode() {
        return GameMode.valueOf(tube.gameMode().name());
    }

    @Override
    public void setGameMode(GameMode mode) {
        tube.setGameMode(space.nows.tube.api.GameMode.valueOf(mode.name()));
    }

    @Override
    public int getFoodLevel() {
        return tube.food();
    }

    @Override
    public void setFoodLevel(int level) {
        tube.setFood(level);
    }

    @Override
    public int getLevel() {
        return tube.level();
    }

    @Override
    public void setLevel(int level) {
        tube.setLevel(level);
    }

    @Override
    public PlayerInventory getInventory() {
        return new BukkitPlayerInventory(tube, this);
    }

    @Override
    public ItemStack getItemInHand() {
        return getInventory().getItemInMainHand();
    }

    @Override
    public void setItemInHand(ItemStack item) {
        getInventory().setItemInMainHand(item);
    }

    @Override
    public int getPing() {
        return tube.latency();
    }

    @Override
    public void sendPluginMessage(Plugin source, String channel, byte[] message) {
        runtime.messenger().send(source, tube, channel, message);
    }

    @Override
    public Set<String> getListeningPluginChannels() {
        return runtime.messenger().listening();
    }

    @Override
    public InetSocketAddress getAddress() {
        return tube.address().orElse(null);
    }

    @Override
    public void kickPlayer(String message) {
        tube.kick(message == null ? "" : message);
    }

    @Override
    public boolean performCommand(String command) {
        return runtime.commands().dispatch(this, command);
    }

    @Override
    public boolean isOp() {
        return runtime.tube().permissions().isOp(tube.uuid());
    }

    @Override
    public void setOp(boolean value) {
        runtime.tube().permissions().setOp(tube.uuid(), tube.name(), value);
    }

    @Override
    public boolean isBanned() {
        return false;
    }

    @Override
    public boolean isWhitelisted() {
        return false;
    }

    @Override
    public void setWhitelisted(boolean whitelisted) {
    }

    @Override
    public long getFirstPlayed() {
        return 0;
    }

    @Override
    public long getLastPlayed() {
        return System.currentTimeMillis();
    }

    @Override
    public boolean hasPlayedBefore() {
        return true;
    }

    @Override
    public void sendMessage(String message) {
        tube.sendMessage(LEGACY.deserialize(message));
    }

    @Override
    public void sendMessage(String... messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }

    @Override
    public String toString() {
        return "CraftPlayer{name=" + tube.name() + "}";
    }
}
