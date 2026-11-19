package space.nows.tube.server.mob.kind.monster.illager;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.monster.raider.SpellcasterIllagerMeta;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import space.nows.tube.server.mob.Creature;

final class BlindnessSpell extends Spell {

    private static final int BLIND_TICKS = 400;

    private LivingEntity blinded;

    BlindnessSpell(Creature caster, Spellbook book) {
        super(caster, book);
    }

    @Override
    public boolean canStart() {
        LivingEntity target = caster.target();
        return super.canStart() && target != blinded;
    }

    @Override
    protected int castingTime() {
        return 20;
    }

    @Override
    protected int interval() {
        return 180;
    }

    @Override
    protected SpellcasterIllagerMeta.Spell look() {
        return SpellcasterIllagerMeta.Spell.BLINDNESS;
    }

    @Override
    protected String prepareSound() {
        return "prepare_blindness";
    }

    @Override
    protected void cast(LivingEntity target) {
        if (target != null) {
            target.addEffect(new Potion(PotionEffect.BLINDNESS, 0, BLIND_TICKS));
            blinded = target;
        }
    }
}
