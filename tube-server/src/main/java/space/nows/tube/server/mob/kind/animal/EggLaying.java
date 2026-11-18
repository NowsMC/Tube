package space.nows.tube.server.mob.kind.animal;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.registry.RegistryKey;
import space.nows.tube.server.gameplay.block.BlockDropper;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.trait.Trait;
import space.nows.tube.server.world.effect.SoundPlayer;

import java.util.Map;

final class EggLaying implements Trait {

    private static final int MIN_DELAY = 6000;
    private static final int EXTRA_DELAY = 6000;
    private static final Map<String, Material> EGGS = Map.of(
            "minecraft:temperate", Material.EGG,
            "minecraft:warm", Material.BROWN_EGG,
            "minecraft:cold", Material.BLUE_EGG);

    private final Creature chicken;
    private int countdown;

    EggLaying(Creature chicken) {
        this.chicken = chicken;
        this.countdown = nextDelay();
    }

    @Override
    public void tick() {
        if (chicken.isDead() || chicken.isBaby() || --countdown > 0) {
            return;
        }
        countdown = nextDelay();
        RegistryKey<?> variant = chicken.get(DataComponents.CHICKEN_VARIANT);
        Material egg = variant == null ? Material.EGG : EGGS.getOrDefault(variant.key().asString(), Material.EGG);
        BlockDropper.spawn(chicken.getInstance(), chicken.getPosition(), ItemStack.of(egg));
        float pitch = (chicken.random().nextFloat() - chicken.random().nextFloat()) * 0.2f + 1f;
        SoundPlayer.play(chicken.getInstance(), "minecraft:entity.chicken.egg", Sound.Source.NEUTRAL, chicken.getPosition(), 1f, pitch);
    }

    private int nextDelay() {
        return chicken.random().nextInt(EXTRA_DELAY) + MIN_DELAY;
    }
}
