package space.nows.tube.server.mob.kind.animal;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.animal.MooshroomMeta;
import space.nows.tube.server.mob.Creature;

import java.util.random.RandomGenerator;

public final class Mooshroom extends Cow {

    @Override
    public EntityType type() {
        return EntityType.MOOSHROOM;
    }

    @Override
    public void prepare(Creature creature, RandomGenerator random) {
        creature.set(DataComponents.MOOSHROOM_VARIANT, MooshroomMeta.Variant.RED);
    }

    @Override
    public void inherit(Creature child, Creature mother, Creature father, RandomGenerator random) {
        inheritComponent(child, mother, father, DataComponents.MOOSHROOM_VARIANT, random);
    }
}
