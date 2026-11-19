package space.nows.tube.server.mob.kind.flier;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.world.Difficulty;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.Disposition;
import space.nows.tube.server.mob.brain.combat.AirStrike;
import space.nows.tube.server.mob.brain.idle.FloatAround;
import space.nows.tube.server.mob.brain.idle.StayAfloat;
import space.nows.tube.server.mob.brain.idle.Tempted;
import space.nows.tube.server.mob.brain.social.Breed;
import space.nows.tube.server.mob.brain.social.FollowParent;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.brain.target.Retaliate;
import space.nows.tube.server.mob.interact.ItemTags;
import space.nows.tube.server.mob.kind.animal.Animal;
import space.nows.tube.server.mob.life.Lifecycle;
import space.nows.tube.server.mob.move.FloatControl;
import space.nows.tube.server.mob.sense.Grudge;
import space.nows.tube.server.mob.trait.Lifespan;

import java.util.Set;

public final class Bee extends Animal {

    private static final CreatureStats STATS = CreatureStats.animal(10, 0.3).withDamage(2);
    private static final Set<Material> FOOD = ItemTags.of("minecraft:bee_food");
    private static final double FLYING_SPEED = 0.06;
    private static final int RANGE = 8;
    private static final int MIN_ANGER = 400;
    private static final int MAX_ANGER = 780;
    private static final int NORMAL_POISON = 200;
    private static final int HARD_POISON = 360;
    private static final int STINGLESS_LIFE = 600;

    @Override
    public EntityType type() {
        return EntityType.BEE;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public Disposition disposition() {
        return Disposition.NEUTRAL;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        creature.getAttribute(Attribute.FLYING_SPEED).setBaseValue(FLYING_SPEED);
        FloatControl control = new FloatControl(creature);
        Grudge grudge = new Grudge(creature, MIN_ANGER, MAX_ANGER);
        creature.addTrait(control);
        creature.addTrait(grudge);
        Lifecycle life = lifecycle(creature);
        brain.add(0, new AirStrike(creature, control));
        brain.add(2, new Breed(creature, life, 1.0));
        brain.add(3, new Tempted(creature, 1.25, FOOD));
        brain.add(5, new FollowParent(creature, life, 1.25));
        brain.add(8, new FloatAround(creature, control, RANGE));
        brain.add(9, new StayAfloat(creature));
        brain.add(1, new Retaliate(creature).alertingOthers());
        brain.add(2, HuntNearest.players(creature, true, grudge::holds));
    }

    @Override
    public void damaged(Creature creature, Damage damage) {
        if (damage.getAttacker() instanceof LivingEntity attacker) {
            creature.trait(Grudge.class).ifPresent(grudge -> grudge.provoke(attacker));
        }
    }

    @Override
    public void landedHit(Creature creature, LivingEntity target) {
        Difficulty difficulty = MinecraftServer.getDifficulty();
        int poison = difficulty == Difficulty.HARD ? HARD_POISON : difficulty == Difficulty.NORMAL ? NORMAL_POISON : 0;
        if (poison > 0) {
            target.addEffect(new Potion(PotionEffect.POISON, 0, poison));
        }
        creature.setTarget(null);
        creature.trait(Grudge.class).ifPresent(Grudge::forgive);
        creature.brain().setEnabled(Control.TARGET, false);
        creature.addTrait(new Lifespan(creature, STINGLESS_LIFE + creature.random().nextInt(STINGLESS_LIFE)));
    }

    @Override
    public Set<Material> breedingFood() {
        return FOOD;
    }
}
