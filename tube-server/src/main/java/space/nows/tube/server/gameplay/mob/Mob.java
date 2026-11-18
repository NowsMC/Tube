package space.nows.tube.server.gameplay.mob;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.EntityAIGroupBuilder;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.entity.ai.goal.RangedAttackGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.ai.target.LastEntityDamagerTarget;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.metadata.projectile.ArrowMeta;
import net.minestom.server.entity.EntityProjectile;
import space.nows.tube.server.gameplay.Archery;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

public final class Mob extends EntityCreature {

    public enum Kind {
        COW(EntityType.COW, 10, 0.2, 0, false, true),
        PIG(EntityType.PIG, 10, 0.25, 0, false, true),
        SHEEP(EntityType.SHEEP, 8, 0.23, 0, false, true),
        CHICKEN(EntityType.CHICKEN, 4, 0.25, 0, false, true),
        ZOMBIE(EntityType.ZOMBIE, 20, 0.23, 3, true, true),
        SKELETON(EntityType.SKELETON, 20, 0.25, 2, true, true),
        SPIDER(EntityType.SPIDER, 16, 0.3, 2, true, true),
        CREEPER(EntityType.CREEPER, 20, 0.25, 0, true, false);

        final EntityType type;
        final double health;
        final double speed;
        final double damage;
        final boolean hostile;
        final boolean ambient;

        Kind(EntityType type, double health, double speed, double damage, boolean hostile, boolean ambient) {
            this.type = type;
            this.health = health;
            this.speed = speed;
            this.damage = damage;
            this.hostile = hostile;
            this.ambient = ambient;
        }

        public boolean hostile() {
            return hostile;
        }

        boolean burnsInDaylight() {
            return this == ZOMBIE || this == SKELETON;
        }
    }

    private final Kind kind;
    private long panicUntil;

    public Mob(Kind kind) {
        super(kind.type);
        this.kind = kind;
        getAttribute(Attribute.MAX_HEALTH).setBaseValue(kind.health);
        setHealth((float) kind.health);
        getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(kind.speed);
        getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(kind.damage);
        if (kind.type == EntityType.SKELETON) {
            setItemInMainHand(net.minestom.server.item.ItemStack.of(net.minestom.server.item.Material.BOW));
        }
        Predicate<Entity> prey = entity -> entity instanceof Player player && !player.isDead()
                && (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE);
        EntityAIGroupBuilder ai = new EntityAIGroupBuilder();
        switch (kind) {
            case ZOMBIE, SPIDER -> ai.addGoalSelector(new MeleeAttackGoal(this, 1.6, Duration.ofMillis(1000)));
            case SKELETON -> {
                RangedAttackGoal ranged = new RangedAttackGoal(this, Duration.ofMillis(2000), 15, 10, false, 1.6, 6);
                ranged.setProjectileGenerator(shooter -> {
                    EntityProjectile arrow = new EntityProjectile(shooter, EntityType.ARROW);
                    ((ArrowMeta) arrow.getEntityMeta()).setCritical(false);
                    arrow.setTag(Archery.ARROW_DAMAGE, 2.0 + ThreadLocalRandom.current().nextDouble(2));
                    return arrow;
                });
                ai.addGoalSelector(ranged);
            }
            case CREEPER -> ai.addGoalSelector(new CreeperGoal(this));
            default -> {
            }
        }
        if (kind.hostile) {
            ai.addTargetSelector(new LastEntityDamagerTarget(this, 24));
            ai.addTargetSelector(new ClosestEntityTarget(this, 16, prey));
        }
        ai.addGoalSelector(new RandomStrollGoal(this, 8));
        ai.addGoalSelector(new RandomLookAroundGoal(this, 20));
        addAIGroup(ai.build());
        setRemovalAnimationDelay(1000);
    }

    public Kind kind() {
        return kind;
    }

    void panic(Entity from) {
        if (kind.hostile || from == null) {
            return;
        }
        panicUntil = getAliveTicks() + 60;
        Vec away = getPosition().sub(from.getPosition()).asVec().withY(0);
        if (away.lengthSquared() < 0.01) {
            away = new Vec(1, 0, 0);
        }
        Pos target = getPosition().add(away.normalize().mul(8));
        getNavigator().setPathTo(target);
    }

    @Override
    public void update(long time) {
        super.update(time);
        AmbientSound.tick(this);
        if (panicUntil > 0 && getAliveTicks() > panicUntil) {
            panicUntil = 0;
        }
    }

    @Override
    public void kill() {
        super.kill();
        DeathEffect.play(this);
    }

    boolean targets(LivingEntity entity) {
        return getTarget() == entity;
    }
}
