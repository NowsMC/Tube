package space.nows.tube.server.mob.kind.animal;

import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.EntityTags;
import space.nows.tube.server.mob.brain.combat.SwimStrike;
import space.nows.tube.server.mob.brain.idle.SwimAbout;
import space.nows.tube.server.mob.brain.idle.Tempted;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.social.Breed;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.interact.ItemTags;
import space.nows.tube.server.mob.life.Lifecycle;

import java.util.Set;

public final class Axolotl extends Animal {

    private static final CreatureStats STATS = CreatureStats.animal(14, 1.0).withDamage(2);
    private static final Set<Material> FOOD = ItemTags.of("minecraft:axolotl_food");
    private static final Set<EntityType> PREY = EntityTags.of("minecraft:axolotl_hunt_targets");
    private static final Set<EntityType> FOES = EntityTags.of("minecraft:axolotl_always_hostiles");
    private static final double SWIM = 0.06;
    private static final double CHASE = 0.1;

    @Override
    public EntityType type() {
        return EntityType.AXOLOTL;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public boolean aquatic() {
        return true;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        Lifecycle life = lifecycle(creature);
        brain.add(1, new SwimStrike(creature, CHASE));
        brain.add(2, new Breed(creature, life, 0.2));
        brain.add(3, new Tempted(creature, 1.1, FOOD));
        brain.add(5, new SwimAbout(creature, SWIM));
        brain.add(7, Wander.onLand(creature, 0.15));
        brain.add(1, HuntNearest.of(creature, entity -> FOES.contains(entity.getEntityType()), 10, true, target -> creature.inWater()));
        brain.add(2, HuntNearest.of(creature, entity -> PREY.contains(entity.getEntityType()), 10, true, target -> creature.inWater())
                .when(axolotl -> !axolotl.isBaby()));
    }

    @Override
    public Set<Material> breedingFood() {
        return FOOD;
    }
}
