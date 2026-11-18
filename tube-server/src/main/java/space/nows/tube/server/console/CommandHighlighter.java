package space.nows.tube.server.console;

import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import space.nows.tube.server.command.CommandService;

final class CommandHighlighter implements Highlighter {

    private static final AttributedStyle KNOWN = AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN);
    private static final AttributedStyle UNKNOWN = AttributedStyle.DEFAULT.foreground(AttributedStyle.RED);

    private final TubeConsole console;

    CommandHighlighter(TubeConsole console) {
        this.console = console;
    }

    @Override
    public AttributedString highlight(LineReader reader, String buffer) {
        CommandService commands = console.commands();
        int start = 0;
        while (start < buffer.length() && buffer.charAt(start) == ' ') {
            start++;
        }
        if (commands == null || start == buffer.length()) {
            return new AttributedString(buffer);
        }
        int end = buffer.indexOf(' ', start);
        if (end < 0) {
            end = buffer.length();
        }
        String label = buffer.substring(buffer.charAt(start) == '/' ? start + 1 : start, end);
        return new AttributedStringBuilder()
                .append(buffer.substring(0, start))
                .styled(commands.has(label) ? KNOWN : UNKNOWN, buffer.substring(start, end))
                .append(buffer.substring(end))
                .toAttributedString();
    }
}
