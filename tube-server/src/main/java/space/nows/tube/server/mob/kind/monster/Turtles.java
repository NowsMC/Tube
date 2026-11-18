package space.nows.tube.server.mob.kind.monster;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.AgeableMobMeta;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.ai.target.HuntNearest;
import space.nows.tube.server.mob.move.Liquids;

public final class Turtles {

    private static final int INTERVAL = 10;

    private Turtles() {
    }

    public static HuntNearest hatchlingHunt(Creature hunter) {
        return HuntNearest.of(hunter, entity -> entity.getEntityType() == EntityType.TURTLE, INTERVAL, true,
                turtle -> turtle.getEntityMeta() instanceof AgeableMobMeta ageable && ageable.isBaby()
                        && !Liquids.isWater(turtle.getInstance().getBlock(turtle.getPosition())));
    }
}
