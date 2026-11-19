package space.nows.tube.server.mob.kind.animal.horse;

import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.item.Material;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
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

abstract class Equine extends Animal {

    private static final Set<Material> TEMPT = ItemTags.of("minecraft:horse_tempt_items");

    protected boolean floats() {
        return true;
    }

    protected boolean panics() {
        return true;
    }

    protected boolean tempted() {
        return true;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        Lifecycle life = lifecycle(creature);
        Rearing rearing = new Rearing(creature);
        creature.addTrait(rearing);
        if (floats()) {
            brain.add(0, new StayAfloat(creature));
        }
        if (panics()) {
            brain.add(1, new Panic(creature, 1.2));
        }
        if (tempted()) {
            brain.add(3, new Tempted(creature, 1.25, TEMPT));
        }
        brain.add(4, new FollowParent(creature, life, 1.0));
        brain.add(6, Wander.onLand(creature, 0.7));
        brain.add(7, WatchNearby.players(creature, 6f));
        brain.add(8, new LookAround(creature));
        brain.add(9, new Rear(creature, rearing));
    }

    @Override
    public Set<Material> breedingFood() {
        return Set.of();
    }

    protected static void rollHealth(Creature creature, RandomGenerator random) {
        double health = 15 + random.nextInt(8) + random.nextInt(9);
        creature.getAttribute(Attribute.MAX_HEALTH).setBaseValue(health);
        creature.setHealth((float) health);
    }

    protected static void rollSpeedAndJump(Creature creature, RandomGenerator random) {
        double speed = (0.45 + random.nextDouble() * 0.3 + random.nextDouble() * 0.3 + random.nextDouble() * 0.3) * 0.25;
        double jump = 0.4 + random.nextDouble() * 0.2 + random.nextDouble() * 0.2 + random.nextDouble() * 0.2;
        creature.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(speed);
        creature.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(jump);
    }
}
