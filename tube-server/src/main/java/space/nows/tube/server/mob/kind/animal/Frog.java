package space.nows.tube.server.mob.kind.animal;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.cube.AbstractCubeMeta;
import net.minestom.server.item.Material;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.combat.MeleeAttack;
import space.nows.tube.server.mob.brain.idle.SwimAbout;
import space.nows.tube.server.mob.brain.idle.Tempted;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.social.Breed;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.interact.ItemTags;
import space.nows.tube.server.mob.life.Lifecycle;

import java.util.Set;

public final class Frog extends Animal {

    private static final CreatureStats STATS = CreatureStats.animal(10, 1.0).withDamage(10);
    private static final Set<Material> FOOD = ItemTags.of("minecraft:frog_food");
    private static final double SWIM = 0.05;

    @Override
    public EntityType type() {
        return EntityType.FROG;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        Lifecycle life = lifecycle(creature);
        brain.add(1, new MeleeAttack(creature, 0.2, true));
        brain.add(2, new Breed(creature, life, 0.15));
        brain.add(3, new Tempted(creature, 0.2, FOOD));
        brain.add(5, new SwimAbout(creature, SWIM));
        brain.add(6, Wander.onLand(creature, 0.15));
        brain.add(7, WatchNearby.players(creature, 6f));
        brain.add(1, HuntNearest.of(creature, entity -> (entity.getEntityType() == EntityType.SLIME
                || entity.getEntityType() == EntityType.MAGMA_CUBE)
                && entity.getEntityMeta() instanceof AbstractCubeMeta cube && cube.getSize() == 1, 10, true, target -> true));
    }

    @Override
    public Set<Material> breedingFood() {
        return FOOD;
    }
}
