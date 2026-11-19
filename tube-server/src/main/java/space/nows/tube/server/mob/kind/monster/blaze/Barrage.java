package space.nows.tube.server.mob.kind.monster.blaze;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.metadata.monster.BlazeMeta;
import space.nows.tube.api.entity.mob.brain.Behavior;
import space.nows.tube.api.entity.mob.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.brain.target.Targeting;
import space.nows.tube.server.mob.projectile.Fireball;
import space.nows.tube.server.mob.projectile.Scorch;
import space.nows.tube.server.world.effect.SoundPlayer;

final class Barrage extends Behavior {

    private static final double MELEE_RANGE = 4;
    private static final int MELEE_INTERVAL = 20;
    private static final int CHARGE = 60;
    private static final int BETWEEN_SHOTS = 6;
    private static final int SHOTS_END = 4;
    private static final int REST = 100;
    private static final double SPREAD = 2.297;
    private static final int CHASE_UNSEEN = 5;

    private final Creature blaze;
    private int step;
    private int waitFor;
    private int unseen;

    Barrage(Creature blaze) {
        super(Control.MOVE, Control.LOOK);
        this.blaze = blaze;
    }

    @Override
    public boolean canStart() {
        LivingEntity target = blaze.target();
        return target != null && Targeting.combat(0).blind().test(blaze, target);
    }

    @Override
    public void start() {
        step = 0;
    }

    @Override
    public void stop() {
        charged(false);
        unseen = 0;
    }

    @Override
    public boolean everyTick() {
        return true;
    }

    @Override
    public void tick() {
        waitFor--;
        LivingEntity target = blaze.target();
        if (target == null) {
            return;
        }
        boolean visible = blaze.sight().canSee(target);
        unseen = visible ? 0 : unseen + 1;
        double distance = blaze.getPosition().distanceSquared(target.getPosition());
        double range = blaze.getAttributeValue(Attribute.FOLLOW_RANGE);
        if (distance < MELEE_RANGE) {
            if (!visible) {
                return;
            }
            if (waitFor <= 0) {
                waitFor = MELEE_INTERVAL;
                blaze.strike(target);
            }
            blaze.navigation().moveTo(target, 1.0);
        } else if (distance < range * range && visible) {
            if (waitFor <= 0) {
                step++;
                if (step == 1) {
                    waitFor = CHARGE;
                    charged(true);
                } else if (step <= SHOTS_END) {
                    waitFor = BETWEEN_SHOTS;
                } else {
                    waitFor = REST;
                    step = 0;
                    charged(false);
                }
                if (step > 1) {
                    shoot(target, distance);
                }
            }
            blaze.gaze().lookAt(target, 10f, 10f);
        } else if (unseen < CHASE_UNSEEN) {
            blaze.navigation().moveTo(target, 1.0);
        }
    }

    private void shoot(LivingEntity target, double distance) {
        double spread = Math.sqrt(Math.sqrt(distance)) * 0.5;
        double dx = target.getPosition().x() - blaze.getPosition().x();
        double dy = target.getPosition().y() + target.getBoundingBox().height() / 2 - (blaze.getPosition().y() + blaze.getBoundingBox().height() / 2);
        double dz = target.getPosition().z() - blaze.getPosition().z();
        Vec direction = new Vec(triangle(dx, SPREAD * spread), dy, triangle(dz, SPREAD * spread));
        Pos from = blaze.getPosition().add(0, blaze.getBoundingBox().height() / 2 + 0.5, 0);
        new Fireball(EntityType.SMALL_FIREBALL, blaze, direction, new Scorch()).setInstance(blaze.getInstance(), from);
        SoundPlayer.play(blaze, "minecraft:entity.blaze.shoot", Sound.Source.HOSTILE, 1f, 1f);
    }

    private double triangle(double mode, double deviation) {
        return mode + deviation * (blaze.random().nextDouble() - blaze.random().nextDouble());
    }

    private void charged(boolean charged) {
        if (blaze.getEntityMeta() instanceof BlazeMeta meta) {
            meta.setOnFire(charged);
        }
    }
}
