package space.nows.tube.bukkit.entity;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import space.nows.tube.bukkit.BukkitRuntime;
import space.nows.tube.bukkit.permission.BukkitPermissible;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BukkitEntity extends BukkitPermissible implements Entity {

    static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private final space.nows.tube.api.entity.Entity tube;

    public BukkitEntity(BukkitRuntime runtime, space.nows.tube.api.entity.Entity tube) {
        super(runtime);
        this.tube = tube;
    }

    @Override
    protected boolean granted(String node) {
        return false;
    }

    @Override
    public boolean isOp() {
        return false;
    }

    @Override
    public void setOp(boolean value) {
    }

    @Override
    public void sendMessage(String message) {
    }

    @Override
    public void sendMessage(String... messages) {
    }

    @Override
    public String getName() {
        return tube.customName().map(LEGACY::serialize).orElse(tube.type());
    }

    @Override
    public Server getServer() {
        return runtime.server();
    }

    @Override
    public int getEntityId() {
        return tube.uuid().hashCode();
    }

    @Override
    public UUID getUniqueId() {
        return tube.uuid();
    }

    @Override
    public EntityType getType() {
        EntityType type = EntityType.fromName(tube.type());
        return type == null ? EntityType.UNKNOWN : type;
    }

    @Override
    public Location getLocation() {
        return runtime.adapters().location(tube.location());
    }

    @Override
    public World getWorld() {
        return runtime.adapters().world(tube.world());
    }

    @Override
    public boolean teleport(Location location) {
        tube.teleport(runtime.adapters().tubeLocation(location));
        return true;
    }

    @Override
    public String getCustomName() {
        return tube.customName().map(LEGACY::serialize).orElse(null);
    }

    @Override
    public void setCustomName(String name) {
        tube.setCustomName(name == null ? null : LEGACY.deserialize(name));
    }

    @Override
    public boolean isValid() {
        return !tube.isRemoved();
    }

    @Override
    public boolean isDead() {
        return tube.isRemoved();
    }

    @Override
    public void remove() {
        tube.remove();
    }

    @Override
    public Set<String> getScoreboardTags() {
        return new HashSet<>(tube.tags());
    }

    @Override
    public boolean addScoreboardTag(String tag) {
        return tube.addTag(tag);
    }

    @Override
    public boolean removeScoreboardTag(String tag) {
        return tube.removeTag(tag);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Entity entity && entity.getUniqueId().equals(tube.uuid());
    }

    @Override
    public int hashCode() {
        return tube.uuid().hashCode();
    }
}
