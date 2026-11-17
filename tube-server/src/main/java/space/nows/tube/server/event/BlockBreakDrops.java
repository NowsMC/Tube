package space.nows.tube.server.event;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.tag.Tag;

public final class BlockBreakDrops {

    public static final Tag<BlockVec> SUPPRESSED_AT = Tag.Transient("tube:break_without_drops");

    private BlockBreakDrops() {
    }
}
