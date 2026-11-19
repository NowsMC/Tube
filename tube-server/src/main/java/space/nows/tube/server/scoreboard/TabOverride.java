package space.nows.tube.server.scoreboard;

import net.kyori.adventure.text.Component;

record TabOverride(Component displayName, Integer order, Boolean listed, Integer latency) {

    static final TabOverride NONE = new TabOverride(null, null, null, null);

    TabOverride withDisplayName(Component displayName) {
        return new TabOverride(displayName, order, listed, latency);
    }

    TabOverride withOrder(int order) {
        return new TabOverride(displayName, order, listed, latency);
    }

    TabOverride withListed(boolean listed) {
        return new TabOverride(displayName, order, listed, latency);
    }

    TabOverride withLatency(int latency) {
        return new TabOverride(displayName, order, listed, latency);
    }
}
