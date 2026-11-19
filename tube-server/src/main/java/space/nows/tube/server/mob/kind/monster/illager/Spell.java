package space.nows.tube.server.mob.kind.monster.illager;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.monster.raider.SpellcasterIllagerMeta;
import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.sound.MobVoice;

abstract class Spell extends Behavior {

    private static final int WARMUP = 20;

    protected final Creature caster;
    private final Spellbook book;
    private int warmup;
    private long nextAt;

    Spell(Creature caster, Spellbook book) {
        this.caster = caster;
        this.book = book;
    }

    protected abstract int castingTime();

    protected abstract int interval();

    protected abstract SpellcasterIllagerMeta.Spell look();

    protected abstract String prepareSound();

    protected abstract void cast(LivingEntity target);

    protected int warmupTime() {
        return WARMUP;
    }

    protected final boolean ready() {
        return !book.casting() && caster.getAliveTicks() >= nextAt;
    }

    protected final boolean warmingUp() {
        return warmup > 0;
    }

    @Override
    public boolean canStart() {
        return caster.target() != null && ready();
    }

    @Override
    public boolean canContinue() {
        LivingEntity target = caster.target();
        return target != null && warmup > 0;
    }

    @Override
    public void start() {
        warmup = ticks(warmupTime());
        book.begin(look(), castingTime());
        nextAt = caster.getAliveTicks() + interval();
        MobVoice.play(caster, prepareSound());
    }

    @Override
    public void tick() {
        if (--warmup == 0) {
            cast(caster.target());
            MobVoice.play(caster, "cast_spell");
        }
    }
}
