package space.nows.tube.server.command.support;

import java.util.List;

public final class Typing {

    private Typing() {
    }

    public static int position(List<String> args) {
        return Math.max(0, args.size() - 1);
    }

    public static String current(List<String> args) {
        return args.isEmpty() ? "" : args.getLast();
    }

    public static String word(List<String> args, int index) {
        return index < args.size() ? args.get(index) : "";
    }
}
