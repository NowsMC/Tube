package space.nows.tube.server.mob.kind.monster.illager;

import net.minestom.server.color.DyeColor;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.monster.raider.SpellcasterIllagerMeta;
import space.nows.tube.api.world.GameRule;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.world.rule.RuleBook;

import java.util.ArrayList;
import java.util.List;

final class WololoSpell extends Spell {

    private static final double RANGE = 16;
    private static final double HEIGHT = 4;

    private Creature sheep;

    WololoSpell(Creature caster, Spellbook book) {
        super(caster, book);
    }

    @Override
    public boolean canStart() {
        if (caster.target() != null || !RuleBook.enabled(caster.getInstance(), GameRule.MOB_GRIEFING)) {
            return false;
        }
        if (!ready()) {
            return false;
        }
        List<Creature> blue = new ArrayList<>();
        for (Entity entity : caster.getInstance().getNearbyEntities(caster.getPosition(), RANGE)) {
            if (entity instanceof Creature candidate && candidate.getEntityType() == EntityType.SHEEP && !candidate.isDead()
                    && Math.abs(candidate.getPosition().y() - caster.getPosition().y()) <= HEIGHT
                    && candidate.get(DataComponents.SHEEP_COLOR) == DyeColor.BLUE) {
                blue.add(candidate);
            }
        }
        if (blue.isEmpty()) {
            return false;
        }
        sheep = blue.get(caster.random().nextInt(blue.size()));
        return true;
    }

    @Override
    public boolean canContinue() {
        return sheep != null && warmingUp();
    }

    @Override
    public void stop() {
        sheep = null;
    }

    @Override
    protected int warmupTime() {
        return 40;
    }

    @Override
    protected int castingTime() {
        return 60;
    }

    @Override
    protected int interval() {
        return 140;
    }

    @Override
    protected SpellcasterIllagerMeta.Spell look() {
        return SpellcasterIllagerMeta.Spell.WOLOLO;
    }

    @Override
    protected String prepareSound() {
        return "prepare_wololo";
    }

    @Override
    public void tick() {
        if (sheep != null) {
            caster.gaze().lookAt(sheep);
        }
        super.tick();
    }

    @Override
    protected void cast(LivingEntity target) {
        if (sheep != null && !sheep.isDead() && sheep.get(DataComponents.SHEEP_COLOR) == DyeColor.BLUE) {
            sheep.set(DataComponents.SHEEP_COLOR, DyeColor.RED);
        }
    }
}
