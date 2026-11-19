package space.nows.tube.server.mob.projectile;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.PotionContents;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.PotionType;

public enum Brew {
    HARMING(PotionType.HARMING, PotionEffect.INSTANT_DAMAGE, 0),
    HEALING(PotionType.HEALING, PotionEffect.INSTANT_HEALTH, 0),
    POISON(PotionType.POISON, PotionEffect.POISON, 900),
    SLOWNESS(PotionType.SLOWNESS, PotionEffect.SLOWNESS, 1800),
    WEAKNESS(PotionType.WEAKNESS, PotionEffect.WEAKNESS, 1800),
    REGENERATION(PotionType.REGENERATION, PotionEffect.REGENERATION, 900),
    SWIFTNESS(PotionType.SWIFTNESS, PotionEffect.SPEED, 3600),
    FIRE_RESISTANCE(PotionType.FIRE_RESISTANCE, PotionEffect.FIRE_RESISTANCE, 3600),
    WATER_BREATHING(PotionType.WATER_BREATHING, PotionEffect.WATER_BREATHING, 3600);

    private static final float INSTANT_HARM = 6;
    private static final float INSTANT_HEAL = 4;
    private static final int SHORTEST = 20;

    private final PotionType type;
    private final PotionEffect effect;
    private final int duration;

    Brew(PotionType type, PotionEffect effect, int duration) {
        this.type = type;
        this.effect = effect;
        this.duration = duration;
    }

    public ItemStack item(Material material) {
        return ItemStack.of(material).with(DataComponents.POTION_CONTENTS, new PotionContents(type));
    }

    public void apply(LivingEntity target, double strength) {
        if (this == HARMING) {
            target.damage(DamageType.MAGIC, (float) (INSTANT_HARM * strength));
        } else if (this == HEALING) {
            target.setHealth((float) Math.min(target.getHealth() + INSTANT_HEAL * strength, target.getAttributeValue(Attribute.MAX_HEALTH)));
        } else {
            int ticks = (int) (strength * duration + 0.5);
            if (ticks > SHORTEST) {
                target.addEffect(new Potion(effect, 0, ticks));
            }
        }
    }

    public boolean active(LivingEntity target) {
        return target.hasEffect(effect);
    }

    public int color() {
        return effect.color();
    }

    public boolean instant() {
        return effect.instantaneous();
    }
}
