package space.nows.tube.server.mob.kind.boss;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.sound.MobVoice;
import space.nows.tube.server.mob.trait.Trait;

final class Soar implements Trait {

    private static final double TICKS_PER_SECOND = 20;
    private static final double RADIUS = 40;
    private static final double HEIGHT = 75;
    private static final double CRUISE = 0.6;
    private static final double CHARGE = 1.0;
    private static final double TURN = 0.06;
    private static final double SIGHT = 150;
    private static final int MIN_WAIT = 200;
    private static final int MAX_WAIT = 500;
    private static final double TOSS = 0.8 * 20;

    private final Creature dragon;
    private double angle;
    private LivingEntity prey;
    private int nextCharge = MIN_WAIT;

    Soar(Creature dragon) {
        this.dragon = dragon;
        dragon.setNoGravity(true);
    }

    @Override
    public void tick() {
        if (dragon.isDead()) {
            return;
        }
        Point aim;
        double speed;
        if (prey != null && !prey.isDead() && prey.getInstance() == dragon.getInstance()) {
            aim = prey.getPosition().add(0, prey.getBoundingBox().height() / 2, 0);
            speed = CHARGE;
            if (dragon.getBoundingBox().expand(1, 1, 1).intersectEntity(dragon.getPosition(), prey)) {
                dragon.strike(prey);
                Vec away = prey.getPosition().sub(dragon.getPosition()).asVec().withY(0).normalize();
                prey.setVelocity(prey.getVelocity().add(away.x() * TOSS, TOSS * 0.5, away.z() * TOSS));
                prey = null;
            }
        } else {
            prey = null;
            angle += 0.01;
            aim = new Vec(Math.cos(angle) * RADIUS, HEIGHT + Math.sin(angle * 3) * 5, Math.sin(angle) * RADIUS);
            speed = CRUISE;
            if (--nextCharge <= 0) {
                nextCharge = MIN_WAIT + dragon.random().nextInt(MAX_WAIT - MIN_WAIT);
                prey = nearestPlayer();
                if (prey != null) {
                    MobVoice.play(dragon, "growl");
                }
            }
        }
        Vec heading = aim.sub(dragon.getPosition()).asVec();
        if (heading.lengthSquared() < 1.0E-6) {
            return;
        }
        Vec current = dragon.getVelocity().div(TICKS_PER_SECOND);
        Vec next = current.add(heading.normalize().mul(speed).sub(current).mul(TURN));
        dragon.setVelocity(next.mul(TICKS_PER_SECOND));
        dragon.setView((float) Math.toDegrees(-Math.atan2(next.x(), next.z())) + 180f, 0);
    }

    private Player nearestPlayer() {
        Player nearest = null;
        double best = SIGHT * SIGHT;
        for (Player player : dragon.getInstance().getPlayers()) {
            double distance = player.getPosition().distanceSquared(dragon.getPosition());
            if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR && distance < best) {
                best = distance;
                nearest = player;
            }
        }
        return nearest;
    }
}
