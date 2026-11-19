package space.nows.tube.server.mob.kind.monster.zombie;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.sense.Grudge;
import space.nows.tube.server.mob.trait.Trait;

final class PiglinAlarm implements Trait {

    private static final AttributeModifier ATTACKING = new AttributeModifier("minecraft:attacking", 0.05, AttributeOperation.ADD_VALUE);
    private static final int MIN_INTERVAL = 80;
    private static final int MAX_INTERVAL = 120;
    private static final double ALERT_HEIGHT = 10;

    private final Creature piglin;
    private final Grudge grudge;
    private LivingEntity lastTarget;
    private int nextAlert;

    PiglinAlarm(Creature piglin, Grudge grudge) {
        this.piglin = piglin;
        this.grudge = grudge;
    }

    @Override
    public void tick() {
        var speed = piglin.getAttribute(Attribute.MOVEMENT_SPEED);
        boolean boosted = speed.getModifiers().stream().anyMatch(modifier -> modifier.id().equals(ATTACKING.id()));
        if (grudge.angry() && !piglin.isBaby()) {
            if (!boosted) {
                speed.addModifier(ATTACKING);
            }
        } else if (boosted) {
            speed.removeModifier(ATTACKING);
        }
        LivingEntity target = piglin.target();
        if (target != lastTarget && target != null) {
            nextAlert = interval();
        }
        lastTarget = target;
        if (target == null) {
            return;
        }
        if (nextAlert > 0) {
            nextAlert--;
            return;
        }
        if (piglin.sight().canSee(target)) {
            alert(target);
        }
        nextAlert = interval();
    }

    private int interval() {
        return MIN_INTERVAL + piglin.random().nextInt(MAX_INTERVAL - MIN_INTERVAL + 1);
    }

    private void alert(LivingEntity target) {
        double range = piglin.getAttributeValue(Attribute.FOLLOW_RANGE);
        for (Entity entity : piglin.getInstance().getNearbyEntities(piglin.getPosition(), range)) {
            if (entity instanceof Creature other && other != piglin && other.kind() == piglin.kind() && other.target() == null
                    && Math.abs(other.getPosition().y() - piglin.getPosition().y()) <= ALERT_HEIGHT) {
                other.setTarget(target);
            }
        }
    }
}
