package space.nows.tube.server.mob.ai.idle;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.item.Material;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.ai.Control;
import space.nows.tube.server.mob.ai.Goal;
import space.nows.tube.server.mob.ai.target.Targeting;

import java.util.Set;

public final class Tempted extends Goal {

    private static final double STOP_DISTANCE = 2.5;
    private static final int CALM_DOWN = 100;

    private final Creature creature;
    private final double speed;
    private final Set<Material> lures;
    private Player player;
    private int calmDown;

    public Tempted(Creature creature, double speed, Set<Material> lures) {
        super(Control.MOVE, Control.LOOK);
        this.creature = creature;
        this.speed = speed;
        this.lures = lures;
    }

    @Override
    public boolean canStart() {
        if (calmDown > 0) {
            calmDown--;
            return false;
        }
        Targeting targeting = Targeting.peaceful(creature.getAttributeValue(Attribute.TEMPT_RANGE)).blind().only(this::holdsLure);
        player = targeting.nearestPlayer(creature);
        return player != null;
    }

    private boolean holdsLure(LivingEntity entity) {
        return lures.contains(entity.getItemInMainHand().material()) || lures.contains(entity.getItemInOffHand().material());
    }

    @Override
    public void stop() {
        player = null;
        creature.navigation().stop();
        calmDown = halved(CALM_DOWN);
    }

    @Override
    public void tick() {
        creature.gaze().lookAt(player, creature.kind().maxHeadTurn() + 20, creature.kind().maxHeadPitch());
        if (creature.getPosition().distanceSquared(player.getPosition()) < STOP_DISTANCE * STOP_DISTANCE) {
            creature.navigation().stop();
        } else {
            creature.navigation().moveTo(player, speed);
        }
    }
}
