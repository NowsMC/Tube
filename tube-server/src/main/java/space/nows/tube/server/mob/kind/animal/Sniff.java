package space.nows.tube.server.mob.kind.animal;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import space.nows.tube.api.entity.mob.brain.Behavior;
import space.nows.tube.api.entity.mob.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.sound.MobVoice;

import java.util.Set;

final class Sniff extends Behavior {

    private static final int CHANCE = 400;
    private static final int DIG_TICKS = 120;
    private static final int COOLDOWN = 9600;
    private static final Set<Block> DIGGABLE = Set.of(Block.DIRT, Block.GRASS_BLOCK, Block.PODZOL, Block.COARSE_DIRT, Block.ROOTED_DIRT,
            Block.MOSS_BLOCK, Block.MUD, Block.MUDDY_MANGROVE_ROOTS);

    private final Creature sniffer;
    private long readyAt;
    private int digging;

    Sniff(Creature sniffer) {
        super(Control.MOVE, Control.LOOK);
        this.sniffer = sniffer;
    }

    @Override
    public boolean canStart() {
        return !sniffer.isBaby() && sniffer.getAliveTicks() >= readyAt && sniffer.isOnGround()
                && DIGGABLE.contains(sniffer.blockAt(sniffer.getPosition().add(0, -1, 0)).defaultState())
                && sniffer.random().nextInt(halved(CHANCE)) == 0;
    }

    @Override
    public boolean canContinue() {
        return digging < DIG_TICKS;
    }

    @Override
    public void start() {
        digging = 0;
        sniffer.navigation().stop();
        MobVoice.play(sniffer, "digging");
    }

    @Override
    public boolean everyTick() {
        return true;
    }

    @Override
    public void tick() {
        if (++digging < DIG_TICKS) {
            return;
        }
        Point at = sniffer.getPosition().add(sniffer.getPosition().direction().mul(2));
        ItemEntity seed = new ItemEntity(ItemStack.of(sniffer.random().nextBoolean() ? Material.TORCHFLOWER_SEEDS : Material.PITCHER_POD));
        seed.setPickupDelay(java.time.Duration.ofMillis(500));
        seed.setInstance(sniffer.getInstance(), at.add(0, 0.5, 0));
        readyAt = sniffer.getAliveTicks() + COOLDOWN;
        MobVoice.play(sniffer, "drop_seed");
    }
}
