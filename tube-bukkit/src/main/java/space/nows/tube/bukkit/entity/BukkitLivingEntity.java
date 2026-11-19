package space.nows.tube.bukkit.entity;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import space.nows.tube.bukkit.BukkitRuntime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BukkitLivingEntity extends BukkitEntity implements LivingEntity {

    private final space.nows.tube.api.entity.LivingEntity tube;

    public BukkitLivingEntity(BukkitRuntime runtime, space.nows.tube.api.entity.LivingEntity tube) {
        super(runtime, tube);
        this.tube = tube;
    }

    @Override
    public boolean isDead() {
        return tube.health() <= 0;
    }

    @Override
    public void damage(double amount) {
        tube.damage(amount);
    }

    @Override
    public org.bukkit.Location getEyeLocation() {
        return runtime.adapters().location(tube.eyeLocation());
    }

    @Override
    public double getEyeHeight() {
        return tube.eyeHeight();
    }

    @Override
    public boolean isSwimming() {
        return tube.isSwimming();
    }

    @Override
    public org.bukkit.attribute.AttributeInstance getAttribute(org.bukkit.attribute.Attribute attribute) {
        return new BukkitAttributeInstance(tube, attribute);
    }

    @Override
    public boolean hasLineOfSight(org.bukkit.entity.Entity other) {
        return other instanceof BukkitEntity entity && tube.hasLineOfSight(entity.tube());
    }

    @Override
    public boolean isGliding() {
        return tube.isGliding();
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
        tube.addEffect(new space.nows.tube.api.entity.effect.PotionEffect(effect.getType().getKey().toString(), effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.hasParticles(), effect.hasIcon()));
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
        for (space.nows.tube.api.entity.effect.PotionEffect effect : tube.effects()) {
            PotionEffectType type = PotionEffectType.getByKey(NamespacedKey.minecraft(effect.type().replaceFirst("^minecraft:", "")));
            if (type != null) {
                result.add(new PotionEffect(type, effect.duration(), effect.amplifier(), effect.ambient(), effect.particles(), effect.icon()));
            }
        }
        return result;
    }
}
