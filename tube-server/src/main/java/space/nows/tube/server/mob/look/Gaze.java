package space.nows.tube.server.mob.look;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;

public final class Gaze {

    private static final float IDLE_TURN = 10f;
    private static final int HOLD_TICKS = 2;

    private final EntityCreature creature;
    private final float turnSpeed;
    private final float maxPitch;
    private final float maxHeadTurn;
    private double wantedX;
    private double wantedY;
    private double wantedZ;
    private float yawSpeed;
    private float pitchLimit;
    private int hold;

    public Gaze(EntityCreature creature, float turnSpeed, float maxPitch, float maxHeadTurn) {
        this.creature = creature;
        this.turnSpeed = turnSpeed;
        this.maxPitch = maxPitch;
        this.maxHeadTurn = maxHeadTurn;
    }

    public void lookAt(double x, double y, double z) {
        lookAt(x, y, z, turnSpeed, maxPitch);
    }

    public void lookAt(double x, double y, double z, float yawSpeed, float pitchLimit) {
        this.wantedX = x;
        this.wantedY = y;
        this.wantedZ = z;
        this.yawSpeed = yawSpeed;
        this.pitchLimit = pitchLimit;
        this.hold = HOLD_TICKS;
    }

    public void lookAt(Entity target) {
        Point eye = target.getPosition().add(0, target.getEyeHeight(), 0);
        lookAt(eye.x(), eye.y(), eye.z());
    }

    public void lookAt(Entity target, float yawSpeed, float pitchLimit) {
        Point eye = target.getPosition().add(0, target.getEyeHeight(), 0);
        lookAt(eye.x(), eye.y(), eye.z(), yawSpeed, pitchLimit);
    }

    public boolean looking() {
        return hold > 0;
    }

    public void tick(boolean moving) {
        Pos position = creature.getPosition();
        float bodyYaw = position.yaw();
        float head = creature.getHeadRotation();
        float pitch = 0f;
        if (hold > 0) {
            hold--;
            double dx = wantedX - position.x();
            double dz = wantedZ - position.z();
            double dy = wantedY - (position.y() + creature.getEyeHeight());
            double flat = Math.sqrt(dx * dx + dz * dz);
            if (Math.abs(dx) > 1.0E-5 || Math.abs(dz) > 1.0E-5) {
                head = Angles.turn(head, (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f, yawSpeed);
            }
            if (Math.abs(dy) > 1.0E-5 || flat > 1.0E-5) {
                pitch = Angles.turn(position.pitch(), (float) -Math.toDegrees(Math.atan2(dy, flat)), pitchLimit);
            }
        } else {
            head = Angles.turn(head, bodyYaw, IDLE_TURN);
        }
        if (moving) {
            head = Angles.within(head, bodyYaw, maxHeadTurn);
        }
        creature.setView(bodyYaw, pitch, head);
    }
}
