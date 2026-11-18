package space.nows.tube.server.player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerVisibility {

    private final Map<UUID, Set<UUID>> hidden = new ConcurrentHashMap<>();

    public boolean canSee(UUID viewer, UUID target) {
        Set<UUID> hiddenFromViewer = hidden.get(viewer);
        return hiddenFromViewer == null || !hiddenFromViewer.contains(target);
    }

    public void hide(UUID viewer, UUID target) {
        hidden.computeIfAbsent(viewer, key -> ConcurrentHashMap.newKeySet()).add(target);
    }

    public void show(UUID viewer, UUID target) {
        Set<UUID> hiddenFromViewer = hidden.get(viewer);
        if (hiddenFromViewer != null) {
            hiddenFromViewer.remove(target);
        }
    }

    public void forget(UUID player) {
        hidden.remove(player);
        hidden.values().forEach(targets -> targets.remove(player));
    }
}
