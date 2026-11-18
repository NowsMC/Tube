package space.nows.tube.server.console;

import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

final class LoggedStream extends OutputStream {

    private final Logger logger;
    private final Level level;
    private final ByteArrayOutputStream pending = new ByteArrayOutputStream();

    LoggedStream(Logger logger, Level level) {
        this.logger = logger;
        this.level = level;
    }

    @Override
    public synchronized void write(int b) {
        if (b == '\n') {
            flushLine();
        } else if (b != '\r') {
            pending.write(b);
        }
    }

    @Override
    public synchronized void flush() {
        if (pending.size() > 0) {
            flushLine();
        }
    }

    private void flushLine() {
        String line = pending.toString(StandardCharsets.UTF_8);
        pending.reset();
        logger.atLevel(level).log(line);
    }
}
