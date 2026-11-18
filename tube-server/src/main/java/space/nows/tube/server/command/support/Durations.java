package space.nows.tube.server.command.support;

import space.nows.tube.api.command.CommandFailure;

public final class Durations {

    private static final int TICKS_PER_SECOND = 20;
    private static final int TICKS_PER_DAY = 24000;

    private Durations() {
    }

    public static int ticks(String text) {
        try {
            if (text.endsWith("d")) {
                return Math.multiplyExact(Integer.parseInt(text.substring(0, text.length() - 1)), TICKS_PER_DAY);
            }
            if (text.endsWith("s")) {
                return Math.multiplyExact(Integer.parseInt(text.substring(0, text.length() - 1)), TICKS_PER_SECOND);
            }
            return Integer.parseInt(text.endsWith("t") ? text.substring(0, text.length() - 1) : text);
        } catch (NumberFormatException | ArithmeticException e) {
            throw new CommandFailure("Invalid duration " + text);
        }
    }
}
