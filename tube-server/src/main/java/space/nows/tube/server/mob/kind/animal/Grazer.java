package space.nows.tube.server.mob.kind.animal;

import net.minestom.server.item.Material;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.brain.idle.LookAround;
import space.nows.tube.server.mob.brain.idle.Panic;
import space.nows.tube.server.mob.brain.idle.StayAfloat;
import space.nows.tube.server.mob.brain.idle.Tempted;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.social.Breed;
import space.nows.tube.server.mob.brain.social.FollowParent;
import space.nows.tube.server.mob.interact.ItemTags;
import space.nows.tube.server.mob.life.Lifecycle;

import java.util.Set;

abstract class Grazer extends Animal {

    private Set<Material> food;

    protected abstract String foodTag();

    protected double pace() {
        return 1.0;
    }

    private Set<Material> food() {
        if (food == null) {
            food = ItemTags.of(foodTag());
        }
        return food;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        Lifecycle life = lifecycle(creature);
        brain.add(0, new StayAfloat(creature));
        brain.add(1, new Panic(creature, 2.0 * pace()));
        brain.add(2, new Breed(creature, life, pace()));
        brain.add(3, new Tempted(creature, 1.25 * pace(), food()));
        brain.add(4, new FollowParent(creature, life, 1.25 * pace()));
        brain.add(5, Wander.onLand(creature, pace()));
        brain.add(6, WatchNearby.players(creature, 6f));
        brain.add(7, new LookAround(creature));
    }

    @Override
    public Set<Material> breedingFood() {
        return food();
    }
}
