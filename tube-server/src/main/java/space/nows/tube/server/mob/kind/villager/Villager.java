package space.nows.tube.server.mob.kind.villager;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.VillagerProfession;
import net.minestom.server.entity.metadata.villager.VillagerMeta;
import space.nows.tube.api.entity.mob.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureKind;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.Disposition;
import space.nows.tube.server.mob.brain.flee.AvoidNearby;
import space.nows.tube.server.mob.brain.idle.LookAround;
import space.nows.tube.server.mob.brain.idle.Panic;
import space.nows.tube.server.mob.brain.idle.StayAfloat;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.idle.WatchNearby;

import java.util.Set;
import java.util.random.RandomGenerator;

public class Villager implements CreatureKind {

    private static final CreatureStats STATS = CreatureStats.mob(20, 0.5);
    private static final Set<EntityType> DANGER = Set.of(EntityType.ZOMBIE, EntityType.HUSK, EntityType.DROWNED,
            EntityType.ZOMBIE_VILLAGER, EntityType.ZOMBIFIED_PIGLIN, EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.EVOKER,
            EntityType.ILLUSIONER, EntityType.RAVAGER, EntityType.VEX, EntityType.ZOGLIN);

    static boolean danger(Entity entity) {
        return DANGER.contains(entity.getEntityType());
    }

    @Override
    public EntityType type() {
        return EntityType.VILLAGER;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public Disposition disposition() {
        return Disposition.PASSIVE;
    }

    @Override
    public boolean leavesWhenFar(Creature creature) {
        return false;
    }

    protected double stroll() {
        return 0.5;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        brain.add(0, new StayAfloat(creature));
        brain.add(1, new Panic(creature, 1.0));
        brain.add(1, new AvoidNearby(creature, Villager::danger, target -> true, 8f, 0.5, 0.6));
        brain.add(5, Wander.onLand(creature, stroll()));
        brain.add(6, WatchNearby.players(creature, 8f));
        brain.add(7, new LookAround(creature));
    }

    @Override
    public void prepare(Creature creature, RandomGenerator random) {
        if (creature.getEntityMeta() instanceof VillagerMeta meta) {
            meta.setVillagerData(new VillagerMeta.VillagerData(Hometown.at(creature.getInstance(), creature.getPosition()),
                    VillagerProfession.NONE, VillagerMeta.Level.NOVICE));
        }
    }
}
