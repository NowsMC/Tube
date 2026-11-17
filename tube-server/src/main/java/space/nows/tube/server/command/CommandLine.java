package space.nows.tube.server.command;

import java.util.ArrayList;
import java.util.List;

record CommandLine(String label, List<String> arguments, int currentStart) {

    static CommandLine parse(String line) {
        String text = line.startsWith("/") ? line.substring(1) : line;
        int offset = line.length() - text.length();
        int labelEnd = text.indexOf(' ');
        if (labelEnd < 0) {
            return new CommandLine(text, List.of(), offset);
        }
        List<String> arguments = new ArrayList<>();
        int start = labelEnd + 1;
        int wordStart = start;
        for (int i = start; i <= text.length(); i++) {
            if (i == text.length() || text.charAt(i) == ' ') {
                arguments.add(text.substring(wordStart, i));
                wordStart = i + 1;
            }
        }
        int currentStart = offset + text.length() - arguments.getLast().length();
        return new CommandLine(text.substring(0, labelEnd), List.copyOf(arguments), currentStart);
    }

    String current() {
        return arguments.isEmpty() ? "" : arguments.getLast();
    }
}
