package space.nows.tube.server.mob.kind.monster.illager;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.combat.BowAttack;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.kind.monster.skeleton.SkeletonVolley;

import java.util.random.RandomGenerator;

public final class Illusioner extends Illager {

    private static final CreatureStats STATS = CreatureStats.monster(32, 0.5).withFollowRange(18);

    @Override
    public EntityType type() {
        return EntityType.ILLUSIONER;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    protected boolean seesVillagers() {
        return false;
    }

    @Override
    protected void behaviors(Creature creature, Brain brain) {
        Spellbook book = new Spellbook(creature);
        creature.addTrait(book);
        brain.add(1, new Casting(creature, book));
        brain.add(4, new MirrorSpell(creature, book));
        brain.add(5, new BlindnessSpell(creature, book));
        brain.add(6, new BowAttack(creature, 0.5, 20, 15f, new SkeletonVolley(null)));
        brain.add(9, WatchNearby.of(creature, entity -> entity instanceof Player, 3f));
    }

    @Override
    public void prepare(Creature creature, RandomGenerator random) {
        creature.setItemInMainHand(ItemStack.of(Material.BOW));
    }
}
