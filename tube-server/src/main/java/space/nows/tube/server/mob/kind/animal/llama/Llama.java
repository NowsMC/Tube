package space.nows.tube.server.mob.kind.animal.llama;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.metadata.animal.LlamaMeta;
import net.minestom.server.item.Material;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.combat.RangedAttack;
import space.nows.tube.server.mob.brain.idle.LookAround;
import space.nows.tube.server.mob.brain.idle.Panic;
import space.nows.tube.server.mob.brain.idle.StayAfloat;
import space.nows.tube.server.mob.brain.idle.Tempted;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.social.FollowParent;
import space.nows.tube.server.mob.interact.ItemTags;
import space.nows.tube.server.mob.kind.animal.Animal;
import space.nows.tube.server.mob.life.Lifecycle;

import java.util.Set;
import java.util.random.RandomGenerator;

public class Llama extends Animal {

    private static final CreatureStats STATS = CreatureStats.animal(53, 0.175);
    private static final Set<Material> TEMPT = ItemTags.of("minecraft:llama_tempt_items");
    private static final double JUMP = 0.5;
    private static final float STRONG_CHANCE = 0.04f;

    @Override
    public EntityType type() {
        return EntityType.LLAMA;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        Lifecycle life = lifecycle(creature);
        brain.add(0, new StayAfloat(creature));
        brain.add(3, new RangedAttack(creature, 1.25, 40, 20f, new Spit()));
        brain.add(3, new Panic(creature, 1.2));
        brain.add(5, new Tempted(creature, 1.25, TEMPT));
        brain.add(6, new FollowParent(creature, life, 1.0));
        brain.add(7, Wander.onLand(creature, 0.7));
        brain.add(8, WatchNearby.players(creature, 6f));
        brain.add(9, new LookAround(creature));
        brain.add(1, new SpitBack(creature));
    }

    @Override
    public void prepare(Creature creature, RandomGenerator random) {
        double health = 15 + random.nextInt(8) + random.nextInt(9);
        creature.getAttribute(Attribute.MAX_HEALTH).setBaseValue(health);
        creature.setHealth((float) health);
        creature.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(JUMP);
        LlamaMeta.Variant[] variants = LlamaMeta.Variant.values();
        creature.set(DataComponents.LLAMA_VARIANT, variants[random.nextInt(variants.length)]);
        if (creature.getEntityMeta() instanceof LlamaMeta meta) {
            meta.setStrength(1 + random.nextInt(random.nextFloat() < STRONG_CHANCE ? 5 : 3));
        }
    }

    @Override
    public Set<Material> breedingFood() {
        return Set.of();
    }

    @Override
    public void inherit(Creature child, Creature mother, Creature father, RandomGenerator random) {
        inheritComponent(child, mother, father, DataComponents.LLAMA_VARIANT, random);
    }
}
