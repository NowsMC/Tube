package space.nows.tube.bukkit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

final class Slf4jLogHandler extends Handler {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube");
    private static final String UNSUPPORTED_LIBRARIES = "Could not initialize LibraryLoader";

    private final SimpleFormatter formatter = new SimpleFormatter();

    static void install() {
        java.util.logging.Logger root = java.util.logging.Logger.getLogger("");
        for (Handler existing : root.getHandlers()) {
            root.removeHandler(existing);
        }
        root.addHandler(new Slf4jLogHandler());
    }

    @Override
    public void publish(LogRecord record) {
        String message = formatter.formatMessage(record);
        if (message.startsWith(UNSUPPORTED_LIBRARIES)) {
            return;
        }
        int level = record.getLevel().intValue();
        if (level >= java.util.logging.Level.SEVERE.intValue()) {
            LOGGER.error(message, record.getThrown());
        } else if (level >= java.util.logging.Level.WARNING.intValue()) {
            LOGGER.warn(message, record.getThrown());
        } else if (level >= java.util.logging.Level.INFO.intValue()) {
            LOGGER.info(message, record.getThrown());
        } else {
            LOGGER.debug(message, record.getThrown());
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }
}
