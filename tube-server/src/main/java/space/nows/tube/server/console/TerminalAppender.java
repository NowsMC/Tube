package space.nows.tube.server.console;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

final class TerminalAppender extends AppenderBase<ILoggingEvent> {

    private final LogLine line;
    private final TubeConsole console;

    TerminalAppender(LogLine line, TubeConsole console) {
        this.line = line;
        this.console = console;
    }

    @Override
    protected void append(ILoggingEvent event) {
        console.print(line.format(event));
    }
}
