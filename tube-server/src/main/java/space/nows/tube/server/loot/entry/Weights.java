package space.nows.tube.server.loot.entry;

final class Weights {

    private Weights() {
    }

    static int of(int weight, int quality, int luck) {
        return Math.max(0, weight + quality * luck);
    }
}
