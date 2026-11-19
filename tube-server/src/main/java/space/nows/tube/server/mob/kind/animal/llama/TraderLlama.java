package space.nows.tube.server.mob.kind.animal.llama;

import net.minestom.server.entity.EntityType;
import space.nows.tube.api.entity.mob.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.EntityTags;
import space.nows.tube.server.mob.brain.idle.Panic;
import space.nows.tube.server.mob.brain.target.HuntNearest;

import java.util.Set;

public final class TraderLlama extends Llama {

    private static final Set<EntityType> FOES = EntityTags.of("minecraft:illager");
    private static final Set<EntityType> ZOMBIES = EntityTags.of("minecraft:zombies");

    @Override
    public EntityType type() {
        return EntityType.TRADER_LLAMA;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        super.configure(creature, brain);
        brain.add(1, new Panic(creature, 2.0));
        brain.add(2, HuntNearest.of(creature, entity -> ZOMBIES.contains(entity.getEntityType())
                && entity.getEntityType() != EntityType.ZOMBIFIED_PIGLIN, 10, true, target -> true));
        brain.add(2, HuntNearest.of(creature, entity -> FOES.contains(entity.getEntityType()), 10, true, target -> true));
    }
}
