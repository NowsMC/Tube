package space.nows.tube.server.mob.brain.social;

import net.minestom.server.entity.Entity;
import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.move.Swim;

import java.util.ArrayList;
import java.util.List;

public final class School extends Behavior {

    private static final int SEARCH = 8;
    private static final double LEADER_RANGE = 121;
    private static final double CLOSE_ENOUGH = 2.5;

    private final Creature fish;
    private final int maxSize;
    private final double speed;
    private School leader;
    private int followers;
    private int nextLook;

    public School(Creature fish, int maxSize, double blocksPerTick) {
        super(Control.MOVE);
        this.fish = fish;
        this.maxSize = maxSize;
        this.speed = blocksPerTick;
        this.nextLook = delay();
    }

    private int delay() {
        return halved(200 + fish.random().nextInt(200) % 20);
    }

    private boolean following() {
        return leader != null && !leader.fish.isRemoved() && !leader.fish.isDead();
    }

    private boolean hasRoom() {
        return followers > 0 && followers < maxSize;
    }

    @Override
    public boolean canStart() {
        if (followers > 0) {
            return false;
        }
        if (following()) {
            return true;
        }
        if (nextLook > 0) {
            nextLook--;
            return false;
        }
        nextLook = delay();
        List<School> nearby = new ArrayList<>();
        for (Entity entity : fish.getInstance().getNearbyEntities(fish.getPosition(), SEARCH)) {
            if (entity instanceof Creature other && other.kind() == fish.kind()) {
                other.brain().behaviors().stream().filter(School.class::isInstance).map(School.class::cast)
                        .filter(school -> school.hasRoom() || !school.following()).findFirst().ifPresent(nearby::add);
            }
        }
        School chosen = nearby.stream().filter(School::hasRoom).findAny().orElse(this);
        for (School other : nearby) {
            if (other != chosen && !other.following() && chosen.followers < chosen.maxSize) {
                other.leader = chosen;
                chosen.followers++;
            }
        }
        return following();
    }

    @Override
    public boolean canContinue() {
        return following() && fish.getPosition().distanceSquared(leader.fish.getPosition()) <= LEADER_RANGE;
    }

    @Override
    public boolean everyTick() {
        return true;
    }

    @Override
    public void stop() {
        if (leader != null) {
            leader.followers--;
            leader = null;
        }
    }

    @Override
    public void tick() {
        if (fish.inWater() && fish.getPosition().distance(leader.fish.getPosition()) > CLOSE_ENOUGH) {
            Swim.steer(fish, leader.fish.getPosition(), speed);
        }
    }
}
