package space.nows.tube.server.mob.kind.monster.illager;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.monster.raider.SpellcasterIllagerMeta;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureKinds;
import space.nows.tube.server.mob.kind.monster.vex.Vex;

final class SummonSpell extends Spell {

    private static final double COUNT_RANGE = 16;
    private static final int SUMMONED = 3;

    SummonSpell(Creature caster, Spellbook book) {
        super(caster, book);
    }

    @Override
    public boolean canStart() {
        if (!super.canStart()) {
            return false;
        }
        int vexes = 0;
        for (Entity entity : caster.getInstance().getNearbyEntities(caster.getPosition(), COUNT_RANGE)) {
            if (entity.getEntityType() == EntityType.VEX) {
                vexes++;
            }
        }
        return caster.random().nextInt(8) + 1 > vexes;
    }

    @Override
    protected int castingTime() {
        return 100;
    }

    @Override
    protected int interval() {
        return 340;
    }

    @Override
    protected SpellcasterIllagerMeta.Spell look() {
        return SpellcasterIllagerMeta.Spell.SUMMON_VEX;
    }

    @Override
    protected String prepareSound() {
        return "prepare_summon";
    }

    @Override
    protected void cast(LivingEntity target) {
        Pos at = caster.getPosition();
        for (int i = 0; i < SUMMONED; i++) {
            Point home = new Vec(at.blockX() - 2 + caster.random().nextInt(5), at.blockY() + 1, at.blockZ() - 2 + caster.random().nextInt(5));
            Creature vex = new Creature(CreatureKinds.of(EntityType.VEX).orElseThrow());
            vex.brain().remember(Vex.OWNER, caster);
            vex.brain().remember(Vex.HOME, home);
            vex.brain().remember(Vex.LIFE, 20 * (30 + caster.random().nextInt(90)));
            CreatureKinds.spawn(vex, caster.getInstance(), new Pos(home.x() + 0.5, home.y(), home.z() + 0.5));
        }
    }
}
