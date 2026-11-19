package space.nows.tube.server.mob.kind.monster.illager;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.monster.raider.SpellcasterIllagerMeta;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import space.nows.tube.server.mob.Creature;

final class MirrorSpell extends Spell {

    private static final int INVISIBLE_TICKS = 1200;

    MirrorSpell(Creature caster, Spellbook book) {
        super(caster, book);
    }

    @Override
    public boolean canStart() {
        return !caster.hasEffect(PotionEffect.INVISIBILITY) && super.canStart();
    }

    @Override
    protected int castingTime() {
        return 20;
    }

    @Override
    protected int interval() {
        return 340;
    }

    @Override
    protected SpellcasterIllagerMeta.Spell look() {
        return SpellcasterIllagerMeta.Spell.DISAPPEAR;
    }

    @Override
    protected String prepareSound() {
        return "prepare_mirror";
    }

    @Override
    protected void cast(LivingEntity target) {
        caster.addEffect(new Potion(PotionEffect.INVISIBILITY, 0, INVISIBLE_TICKS));
    }
}
