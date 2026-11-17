package space.nows.tube.server.player;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.tag.Tag;

public record RespawnPoint(String world, Pos position) {

    public static final Tag<RespawnPoint> TAG = Tag.Transient("tube:respawn_point");
}
