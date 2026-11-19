package space.nows.tube.server.mob.kind.monster.creeper;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.metadata.monster.CreeperMeta;
import net.minestom.server.instance.Instance;
import space.nows.tube.server.gameplay.Explosions;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.brain.combat.Swelling;
import space.nows.tube.server.mob.trait.Trait;
import space.nows.tube.server.world.effect.SoundPlayer;

final class CreeperFuse implements Trait, Swelling {

    private static final int MAX_SWELL = 30;
    private static final float RADIUS = 3f;
    private static final float CHARGED_MULTIPLIER = 2f;

    private final Creature creeper;
    private int swell;
    private int direction = -1;

    CreeperFuse(Creature creeper) {
        this.creeper = creeper;
    }

    @Override
    public int direction() {
        return direction;
    }

    @Override
    public void setDirection(int direction) {
        if (this.direction != direction && creeper.getEntityMeta() instanceof CreeperMeta meta) {
            meta.setState(direction > 0 ? CreeperMeta.State.FUSE : CreeperMeta.State.IDLE);
        }
        this.direction = direction;
    }

    @Override
    public void tick() {
        if (creeper.isDead()) {
            return;
        }
        Instance instance = creeper.getInstance();
        if (direction > 0 && swell == 0) {
            SoundPlayer.play(instance, "minecraft:entity.creeper.primed", Sound.Source.HOSTILE, creeper.getPosition(), 1f, 0.5f);
        }
        swell = Math.max(0, swell + direction);
        if (swell >= MAX_SWELL) {
            explode(instance);
        }
    }

    private void explode(Instance instance) {
        boolean charged = creeper.getEntityMeta() instanceof CreeperMeta meta && meta.isCharged();
        var position = creeper.getPosition();
        creeper.remove();
        Explosions.explode(instance, position, RADIUS * (charged ? CHARGED_MULTIPLIER : 1f), true, creeper);
    }
}
