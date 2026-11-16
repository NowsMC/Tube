package space.nows.tube.bukkit.entity;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import space.nows.tube.bukkit.BukkitRuntime;

import java.util.UUID;

public final class BukkitOfflinePlayer implements OfflinePlayer {

    private final BukkitRuntime runtime;
    private final UUID uuid;
    private final String name;

    public BukkitOfflinePlayer(BukkitRuntime runtime, UUID uuid, String name) {
        this.runtime = runtime;
        this.uuid = uuid;
        this.name = name;
    }

    @Override
    public boolean isOnline() {
        return getPlayer() != null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
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
    public Player getPlayer() {
        return runtime.server().getPlayer(uuid);
    }

    @Override
    public long getFirstPlayed() {
        return 0;
    }

    @Override
    public long getLastPlayed() {
        return 0;
    }

    @Override
    public boolean hasPlayedBefore() {
        return false;
    }

    @Override
    public boolean isOp() {
        return runtime.tube().permissions().isOp(uuid);
    }

    @Override
    public void setOp(boolean value) {
        runtime.tube().permissions().setOp(uuid, name == null ? uuid.toString() : name, value);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof OfflinePlayer player && uuid.equals(player.getUniqueId());
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
