package space.nows.tube.server.scoreboard;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TabLists {

    private final Map<UUID, MinestomTabList> lists = new ConcurrentHashMap<>();

    public MinestomTabList of(Player viewer) {
        return lists.compute(viewer.getUuid(), (id, list) -> {
            if (list == null) {
                return new MinestomTabList(viewer);
            }
            list.attach(viewer);
            return list;
        });
    }

    void joined(Player player) {
        UUID id = player.getUuid();
        MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
            MinestomTabList own = lists.get(id);
            if (own != null) {
                own.replay();
            }
            for (MinestomTabList list : lists.values()) {
                if (list != own) {
                    list.targetJoined(id);
                }
            }
        });
    }
}
