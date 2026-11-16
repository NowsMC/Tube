package org.bukkit.potion;

import org.bukkit.entity.LivingEntity;

import java.util.Objects;

public class PotionEffect {

    private final PotionEffectType type;
    private final int duration;
    private final int amplifier;
    private final boolean ambient;
    private final boolean particles;
    private final boolean icon;

    public PotionEffect(PotionEffectType type, int duration, int amplifier) {
        this(type, duration, amplifier, false);
    }

    public PotionEffect(PotionEffectType type, int duration, int amplifier, boolean ambient) {
        this(type, duration, amplifier, ambient, true);
    }

    public PotionEffect(PotionEffectType type, int duration, int amplifier, boolean ambient, boolean particles) {
        this(type, duration, amplifier, ambient, particles, particles);
    }

    public PotionEffect(PotionEffectType type, int duration, int amplifier, boolean ambient, boolean particles, boolean icon) {
        this.type = Objects.requireNonNull(type, "type");
        this.duration = duration;
        this.amplifier = amplifier;
        this.ambient = ambient;
        this.particles = particles;
        this.icon = icon;
    }

    public PotionEffectType getType() {
        return type;
    }

    public int getDuration() {
        return duration;
    }

    public int getAmplifier() {
        return amplifier;
    }

    public boolean isAmbient() {
        return ambient;
    }

    public boolean hasParticles() {
        return particles;
    }

    public boolean hasIcon() {
        return icon;
    }

    public boolean apply(LivingEntity entity) {
        return entity.addPotionEffect(this);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof PotionEffect effect && type == effect.type && duration == effect.duration && amplifier == effect.amplifier
                && ambient == effect.ambient && particles == effect.particles && icon == effect.icon;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, duration, amplifier, ambient, particles, icon);
    }

    @Override
    public String toString() {
        return type.getName() + ":" + duration + "t x" + amplifier;
    }
}
