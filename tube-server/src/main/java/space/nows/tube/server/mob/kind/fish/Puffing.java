package space.nows.tube.server.mob.kind.fish;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.water.fish.PufferfishMeta;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.EntityTags;
import space.nows.tube.server.mob.sound.MobVoice;
import space.nows.tube.server.mob.trait.Trait;

import java.util.Set;

final class Puffing implements Trait {

    private static final Set<net.minestom.server.entity.EntityType> NOT_SCARY = EntityTags.of("minecraft:not_scary_for_pufferfish");
    private static final int FULL_AFTER = 40;
    private static final int HALF_DEFLATE_AFTER = 60;
    private static final int DEFLATE_AFTER = 100;
    private static final double STING_REACH = 0.3;
    private static final int POISON_PER_PUFF = 60;

    private final Creature fish;
    private int inflating;
    private int deflating;
    private int puff;

    Puffing(Creature fish) {
        this.fish = fish;
    }

    static boolean scary(Entity entity) {
        if (entity instanceof Player player) {
            return player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR;
        }
        return entity instanceof LivingEntity && !NOT_SCARY.contains(entity.getEntityType());
    }

    void inflate() {
        inflating = 1;
        deflating = 0;
    }

    void calm() {
        inflating = 0;
    }

    @Override
    public void tick() {
        if (fish.isDead()) {
            return;
        }
        if (inflating > 0) {
            if (puff == 0) {
                setPuff(1, "blow_up");
            } else if (inflating > FULL_AFTER && puff == 1) {
                setPuff(2, "blow_up");
            }
            inflating++;
        } else if (puff != 0) {
            if (deflating > HALF_DEFLATE_AFTER && puff == 2) {
                setPuff(1, "blow_out");
            } else if (deflating > DEFLATE_AFTER && puff == 1) {
                setPuff(0, "blow_out");
            }
            deflating++;
        }
        if (puff > 0) {
            sting();
        }
    }

    private void setPuff(int state, String sound) {
        puff = state;
        if (fish.getEntityMeta() instanceof PufferfishMeta meta) {
            meta.setState(PufferfishMeta.State.values()[state]);
        }
        MobVoice.play(fish, sound);
    }

    private void sting() {
        double reach = fish.getBoundingBox().width() / 2 + STING_REACH;
        for (Entity entity : fish.getInstance().getNearbyEntities(fish.getPosition(), reach + 1)) {
            if (entity == fish || !(entity instanceof LivingEntity living) || living.isDead() || !scary(entity)
                    || !fish.getBoundingBox().expand(STING_REACH * 2, STING_REACH * 2, STING_REACH * 2)
                    .intersectEntity(fish.getPosition(), entity)) {
                continue;
            }
            if (living.damage(Damage.fromEntity(fish, 1 + puff))) {
                living.addEffect(new Potion(PotionEffect.POISON, 0, POISON_PER_PUFF * puff));
                MobVoice.play(fish, "sting");
            }
        }
    }
}
