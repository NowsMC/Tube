package space.nows.tube.server.mob.kind.monster.illager;

import net.minestom.server.entity.metadata.monster.raider.SpellcasterIllagerMeta;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.trait.Trait;

final class Spellbook implements Trait {

    private final Creature caster;
    private int castingLeft;

    Spellbook(Creature caster) {
        this.caster = caster;
    }

    boolean casting() {
        return castingLeft > 0;
    }

    void begin(SpellcasterIllagerMeta.Spell spell, int ticks) {
        castingLeft = ticks;
        show(spell);
    }

    @Override
    public void tick() {
        if (castingLeft > 0 && --castingLeft == 0) {
            show(SpellcasterIllagerMeta.Spell.NONE);
        }
    }

    private void show(SpellcasterIllagerMeta.Spell spell) {
        if (caster.getEntityMeta() instanceof SpellcasterIllagerMeta meta) {
            meta.setSpell(spell);
        }
    }
}
