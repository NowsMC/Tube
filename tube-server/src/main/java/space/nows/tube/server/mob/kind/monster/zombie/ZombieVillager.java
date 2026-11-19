package space.nows.tube.server.mob.kind.monster.zombie;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.VillagerProfession;
import net.minestom.server.entity.metadata.monster.zombie.ZombieVillagerMeta;
import net.minestom.server.entity.metadata.villager.VillagerMeta;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.kind.villager.Hometown;

import java.util.List;
import java.util.random.RandomGenerator;

public final class ZombieVillager extends Zombie {

    @Override
    public EntityType type() {
        return EntityType.ZOMBIE_VILLAGER;
    }

    @Override
    public void prepare(Creature creature, RandomGenerator random) {
        if (creature.getEntityMeta() instanceof ZombieVillagerMeta meta) {
            List<VillagerProfession> professions = List.copyOf(VillagerProfession.values());
            VillagerProfession profession = professions.get(random.nextInt(professions.size()));
            meta.setVillagerData(new VillagerMeta.VillagerData(Hometown.at(creature.getInstance(), creature.getPosition()), profession,
                    VillagerMeta.Level.NOVICE));
        }
        super.prepare(creature, random);
    }
}
