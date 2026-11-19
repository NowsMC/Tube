package space.nows.tube.server.mob.kind.boss;

import net.kyori.adventure.bossbar.BossBar;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.EntityTags;
import space.nows.tube.server.mob.brain.idle.FloatAround;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.brain.target.Retaliate;
import space.nows.tube.server.mob.kind.monster.Monster;
import space.nows.tube.server.mob.move.FloatControl;

import java.util.Set;

public final class Wither extends Monster {

    private static final CreatureStats STATS = CreatureStats.monster(300, 0.6).withArmor(4).withFollowRange(40);
    private static final Set<EntityType> UNDEAD = EntityTags.of("minecraft:undead");
    private static final double FLYING_SPEED = 0.1;

    @Override
    public EntityType type() {
        return EntityType.WITHER;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public boolean fireproof() {
        return true;
    }

    @Override
    public boolean leavesWhenFar(Creature creature) {
        return false;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        creature.getAttribute(Attribute.FLYING_SPEED).setBaseValue(FLYING_SPEED);
        FloatControl control = new FloatControl(creature);
        creature.addTrait(control);
        creature.addTrait(new Banner(creature, "entity.minecraft.wither", BossBar.Color.PURPLE));
        brain.add(1, new SkullFire(creature, control));
        brain.add(5, new FloatAround(creature, control));
        brain.add(1, new Retaliate(creature));
        brain.add(2, HuntNearest.of(creature, entity -> entity instanceof LivingEntity && !UNDEAD.contains(entity.getEntityType()),
                10, false, target -> true));
    }
}
