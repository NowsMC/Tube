package space.nows.tube.server.mob.loot;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import space.nows.tube.server.event.DeathDrops;
import space.nows.tube.server.gameplay.loot.LootTables;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.KillCredit;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class MobLoot {

    private static final int MONSTER_EXPERIENCE = 5;

    private MobLoot() {
    }

    public static DeathDrops.Drops of(Creature creature) {
        Player killer = KillCredit.killer(creature);
        boolean byPlayer = killer != null;
        LootTables.Context context = LootTables.Context.entity(byPlayer ? killer.getItemInMainHand() : ItemStack.AIR, byPlayer,
                creature.getFireTicks() > 0, ThreadLocalRandom.current());
        context = SheepLoot.describe(creature, context);
        List<ItemStack> items = LootTables.forEntity(creature.getEntityType().key().asString(), context);
        return new DeathDrops.Drops(items, byPlayer ? experience(creature) : 0);
    }

    private static int experience(Creature creature) {
        return creature.kind().disposition().hostile() ? MONSTER_EXPERIENCE : 1 + ThreadLocalRandom.current().nextInt(3);
    }
}
