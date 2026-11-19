package space.nows.tube.server.mob.kind.animal;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.animal.RabbitMeta;
import net.minestom.server.item.Material;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.flee.AvoidNearby;
import space.nows.tube.server.mob.brain.idle.ClimbPowderSnow;
import space.nows.tube.server.mob.brain.idle.Panic;
import space.nows.tube.server.mob.brain.idle.StayAfloat;
import space.nows.tube.server.mob.brain.idle.Tempted;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.social.Breed;
import space.nows.tube.server.mob.interact.ItemTags;
import space.nows.tube.server.mob.life.Lifecycle;

import java.util.Set;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

public final class Rabbit extends Animal {

    private static final CreatureStats STATS = CreatureStats.animal(3, 0.3).withDamage(3);
    private static final Set<Material> FOOD = ItemTags.of("minecraft:rabbit_food");
    private static final Set<String> WHITE_BIOMES = Set.of("snowy_plains", "ice_spikes", "frozen_ocean", "snowy_taiga", "frozen_river",
            "snowy_beach", "frozen_peaks", "jagged_peaks", "snowy_slopes", "grove");
    private static final Set<String> GOLD_BIOMES = Set.of("desert");
    private static final double FLEE_SPEED = 2.2;

    @Override
    public EntityType type() {
        return EntityType.RABBIT;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        Lifecycle life = lifecycle(creature);
        brain.add(1, new StayAfloat(creature));
        brain.add(1, new ClimbPowderSnow(creature));
        brain.add(1, new Panic(creature, FLEE_SPEED));
        brain.add(2, new Breed(creature, life, 0.8));
        brain.add(3, new Tempted(creature, 1.0, FOOD));
        brain.add(4, avoid(creature, entity -> entity instanceof Player, 8f));
        brain.add(4, avoid(creature, entity -> entity.getEntityType() == EntityType.WOLF, 10f));
        brain.add(4, avoid(creature, entity -> entity instanceof Creature other && other.kind().disposition().hostile(), 4f));
        brain.add(6, Wander.onLand(creature, 0.6));
        brain.add(11, WatchNearby.players(creature, 10f));
    }

    private static AvoidNearby avoid(Creature rabbit, Predicate<Entity> kind, float distance) {
        return new AvoidNearby(rabbit, kind, entity -> rabbit.get(DataComponents.RABBIT_VARIANT) != RabbitMeta.Variant.KILLER_BUNNY,
                distance, FLEE_SPEED, FLEE_SPEED);
    }

    @Override
    public void prepare(Creature creature, RandomGenerator random) {
        String biome = creature.getInstance().getBiome(creature.getPosition()).key().value();
        int roll = random.nextInt(100);
        RabbitMeta.Variant variant;
        if (WHITE_BIOMES.contains(biome)) {
            variant = roll < 80 ? RabbitMeta.Variant.WHITE : RabbitMeta.Variant.BLACK_AND_WHITE;
        } else if (GOLD_BIOMES.contains(biome)) {
            variant = RabbitMeta.Variant.GOLD;
        } else {
            variant = roll < 50 ? RabbitMeta.Variant.BROWN : roll < 90 ? RabbitMeta.Variant.SALT_AND_PEPPER : RabbitMeta.Variant.BLACK;
        }
        creature.set(DataComponents.RABBIT_VARIANT, variant);
    }

    @Override
    public Set<Material> breedingFood() {
        return FOOD;
    }

    @Override
    public void inherit(Creature child, Creature mother, Creature father, RandomGenerator random) {
        inheritComponent(child, mother, father, DataComponents.RABBIT_VARIANT, random);
    }
}
