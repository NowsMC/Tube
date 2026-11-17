package space.nows.tube.server.command.support;

import java.util.List;
import java.util.Locale;

public final class CommandInput {

    private final List<String> words;
    private final String usage;
    private int next;

    public CommandInput(List<String> words, String usage) {
        this.words = words;
        this.usage = usage;
    }

    public boolean hasNext() {
        return next < words.size();
    }

    public int remaining() {
        return words.size() - next;
    }

    public String word() {
        if (!hasNext()) {
            throw usage();
        }
        return words.get(next++);
    }

    public String keyword() {
        return word().toLowerCase(Locale.ROOT);
    }

    public String wordOr(String fallback) {
        return hasNext() ? word() : fallback;
    }

    public String rest() {
        if (!hasNext()) {
            throw usage();
        }
        String text = String.join(" ", words.subList(next, words.size()));
        next = words.size();
        return text;
    }

    public int integer() {
        String text = word();
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new CommandFailure("Invalid number " + text);
        }
    }

    public int integerOr(int fallback) {
        return hasNext() ? integer() : fallback;
    }

    public double decimal() {
        String text = word();
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new CommandFailure("Invalid number " + text);
        }
    }

    public void end() {
        if (hasNext()) {
            throw usage();
        }
    }

    public CommandFailure usage() {
        return new CommandFailure("Usage: " + usage);
    }
}
