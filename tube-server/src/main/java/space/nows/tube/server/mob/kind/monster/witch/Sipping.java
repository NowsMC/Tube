package space.nows.tube.server.mob.kind.monster.witch;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.entity.metadata.monster.raider.WitchMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.projectile.Brew;
import space.nows.tube.server.mob.trait.Trait;
import space.nows.tube.server.world.effect.Pitch;
import space.nows.tube.server.world.effect.SoundPlayer;

final class Sipping implements Trait {

    private static final AttributeModifier DRINKING = new AttributeModifier("minecraft:drinking", -0.25, AttributeOperation.ADD_VALUE);
    private static final int DRINK_TICKS = 32;
    private static final double FAR = 121;

    private final Creature witch;
    private Brew drinking;
    private int left;
    private boolean recentlyBurned;

    Sipping(Creature witch) {
        this.witch = witch;
    }

    boolean drinking() {
        return drinking != null;
    }

    void burned() {
        recentlyBurned = true;
    }

    @Override
    public void tick() {
        if (witch.isDead()) {
            return;
        }
        if (drinking != null) {
            if (left-- <= 0) {
                drinking.apply(witch, 1.0);
                drinking = null;
                witch.setItemInMainHand(ItemStack.AIR);
                show(false);
                witch.getAttribute(Attribute.MOVEMENT_SPEED).removeModifier(DRINKING);
            }
            return;
        }
        Brew choice = choose();
        if (choice == null) {
            return;
        }
        drinking = choice;
        left = DRINK_TICKS;
        witch.setItemInMainHand(choice.item(Material.POTION));
        show(true);
        SoundPlayer.play(witch, "minecraft:entity.witch.drink", Sound.Source.HOSTILE, 1f,
                Pitch.between(witch.random(), 0.8f, 1.2f));
        witch.getAttribute(Attribute.MOVEMENT_SPEED).removeModifier(DRINKING);
        witch.getAttribute(Attribute.MOVEMENT_SPEED).addModifier(DRINKING);
    }

    private Brew choose() {
        if (witch.random().nextFloat() < 0.15f && witch.inWater() && !Brew.WATER_BREATHING.active(witch)) {
            return Brew.WATER_BREATHING;
        }
        if (witch.random().nextFloat() < 0.15f && (witch.getFireTicks() > 0 || recentlyBurned) && !Brew.FIRE_RESISTANCE.active(witch)) {
            recentlyBurned = false;
            return Brew.FIRE_RESISTANCE;
        }
        if (witch.random().nextFloat() < 0.05f && witch.getHealth() < witch.getAttributeValue(Attribute.MAX_HEALTH)) {
            return Brew.HEALING;
        }
        LivingEntity target = witch.target();
        if (witch.random().nextFloat() < 0.5f && target != null && !Brew.SWIFTNESS.active(witch)
                && target.getPosition().distanceSquared(witch.getPosition()) > FAR) {
            return Brew.SWIFTNESS;
        }
        return null;
    }

    private void show(boolean value) {
        if (witch.getEntityMeta() instanceof WitchMeta meta) {
            meta.setDrinkingPotion(value);
        }
    }
}
