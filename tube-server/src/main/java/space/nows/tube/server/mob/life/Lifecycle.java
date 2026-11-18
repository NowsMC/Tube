package space.nows.tube.server.mob.life;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.AgeableMobMeta;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.trait.Trait;
import space.nows.tube.server.world.effect.ParticleSpawner;

public final class Lifecycle implements Trait {

    public static final int BABY_AGE = -24000;
    public static final int BREEDING_COOLDOWN = 6000;
    private static final int LOVE_TICKS = 600;
    private static final int HEART_EVERY = 10;
    private static final byte LOVE_EVENT = 18;
    private static final float FEEDING_SPEED_UP = 0.1f;

    private final Creature creature;
    private int age;
    private int love;
    private Player lover;
    private int seenDamage;

    public Lifecycle(Creature creature) {
        this.creature = creature;
    }

    public boolean baby() {
        return age < 0;
    }

    public int age() {
        return age;
    }

    public void setAge(int age) {
        boolean wasBaby = baby();
        this.age = age;
        if (wasBaby != baby() && creature.getEntityMeta() instanceof AgeableMobMeta meta) {
            meta.setBaby(baby());
        }
    }

    public void makeBaby() {
        setAge(BABY_AGE);
    }

    public void feedBaby() {
        ageUp((int) ((-age / 20) * FEEDING_SPEED_UP));
    }

    public void ageUp(int seconds) {
        if (baby()) {
            setAge(Math.min(0, age + seconds * 20));
        }
    }

    public boolean inLove() {
        return love > 0;
    }

    public boolean canFallInLove() {
        return age == 0 && love <= 0;
    }

    public void fallInLove(Player player) {
        love = LOVE_TICKS;
        lover = player;
        creature.triggerStatus(LOVE_EVENT);
    }

    public Player lover() {
        return lover;
    }

    public void bred() {
        setAge(BREEDING_COOLDOWN);
        love = 0;
        creature.triggerStatus(LOVE_EVENT);
    }

    @Override
    public void tick() {
        if (creature.harm().damageCount() != seenDamage) {
            seenDamage = creature.harm().damageCount();
            love = 0;
        }
        if (age < 0) {
            setAge(age + 1);
        } else if (age > 0) {
            setAge(age - 1);
        }
        if (age != 0) {
            love = 0;
        }
        if (love > 0 && --love % HEART_EVERY == 0) {
            ParticleSpawner.spawn(creature.getInstance(), "heart", creature.getPosition().add(0, creature.getBoundingBox().height() + 0.5, 0), 1, 0.4, 0.02);
        }
    }
}
