package space.nows.tube.bukkit.entity;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import space.nows.tube.bukkit.BukkitRuntime;
import space.nows.tube.bukkit.permission.BukkitPermissible;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class BukkitLivingEntity extends BukkitPermissible implements LivingEntity {

    static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private final space.nows.tube.api.entity.LivingEntity tube;

    public BukkitLivingEntity(BukkitRuntime runtime, space.nows.tube.api.entity.LivingEntity tube) {
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
        return tube.health() <= 0;
    }

    @Override
    public void remove() {
        tube.remove();
    }

    @Override
    public double getHealth() {
        return tube.health();
    }

    @Override
    public void setHealth(double health) {
        tube.setHealth(health);
    }

    @Override
    public double getMaxHealth() {
        return tube.maxHealth();
    }

    @Override
    public boolean addPotionEffect(PotionEffect effect) {
        tube.addEffect(new space.nows.tube.api.entity.PotionEffect(effect.getType().getKey().toString(), effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.hasParticles(), effect.hasIcon()));
        return true;
    }

    @Override
    public boolean hasPotionEffect(PotionEffectType type) {
        return getPotionEffect(type) != null;
    }

    @Override
    public PotionEffect getPotionEffect(PotionEffectType type) {
        return getActivePotionEffects().stream().filter(effect -> effect.getType() == type).findFirst().orElse(null);
    }

    @Override
    public void removePotionEffect(PotionEffectType type) {
        tube.removeEffect(type.getKey().toString());
    }

    @Override
    public Collection<PotionEffect> getActivePotionEffects() {
        List<PotionEffect> result = new ArrayList<>();
        for (space.nows.tube.api.entity.PotionEffect effect : tube.effects()) {
            PotionEffectType type = PotionEffectType.getByKey(NamespacedKey.minecraft(effect.type().replaceFirst("^minecraft:", "")));
            if (type != null) {
                result.add(new PotionEffect(type, effect.duration(), effect.amplifier(), effect.ambient(), effect.particles(), effect.icon()));
            }
        }
        return result;
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
