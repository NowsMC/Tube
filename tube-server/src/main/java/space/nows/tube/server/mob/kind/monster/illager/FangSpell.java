package space.nows.tube.server.mob.kind.monster.illager;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.monster.raider.SpellcasterIllagerMeta;
import net.minestom.server.instance.Instance;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.projectile.Fangs;

final class FangSpell extends Spell {

    private static final double CLOSE = 9;

    FangSpell(Creature caster, Spellbook book) {
        super(caster, book);
    }

    @Override
    protected int castingTime() {
        return 40;
    }

    @Override
    protected int interval() {
        return 100;
    }

    @Override
    protected SpellcasterIllagerMeta.Spell look() {
        return SpellcasterIllagerMeta.Spell.ATTACK;
    }

    @Override
    protected String prepareSound() {
        return "prepare_attack";
    }

    @Override
    protected void cast(LivingEntity target) {
        if (target == null) {
            return;
        }
        Pos at = caster.getPosition();
        double minY = Math.min(target.getPosition().y(), at.y());
        double maxY = Math.max(target.getPosition().y(), at.y()) + 1;
        double toward = Math.atan2(target.getPosition().z() - at.z(), target.getPosition().x() - at.x());
        if (at.distanceSquared(target.getPosition()) < CLOSE) {
            for (int i = 0; i < 5; i++) {
                double angle = toward + i * Math.PI * 0.4;
                place(at.x() + Math.cos(angle) * 1.5, at.z() + Math.sin(angle) * 1.5, minY, maxY, angle, 0);
            }
            for (int i = 0; i < 8; i++) {
                double angle = toward + i * Math.PI * 2 / 8 + Math.PI * 2 / 5;
                place(at.x() + Math.cos(angle) * 2.5, at.z() + Math.sin(angle) * 2.5, minY, maxY, angle, 3);
            }
        } else {
            for (int i = 0; i < 16; i++) {
                double reach = 1.25 * (i + 1);
                place(at.x() + Math.cos(toward) * reach, at.z() + Math.sin(toward) * reach, minY, maxY, toward, i);
            }
        }
    }

    private void place(double x, double z, double minY, double maxY, double angle, int delay) {
        Instance instance = caster.getInstance();
        int y = (int) Math.floor(maxY);
        while (y >= Math.floor(minY) - 1) {
            if (instance.isChunkLoaded(new Pos(x, y, z)) && instance.getBlock((int) Math.floor(x), y - 1, (int) Math.floor(z)).solid()) {
                Fangs fangs = new Fangs(caster, delay);
                fangs.setInstance(instance, new Pos(x, y, z, (float) Math.toDegrees(angle) - 90f, 0));
                return;
            }
            y--;
        }
    }
}
