package space.nows.tube.server.console;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.EncoderBase;

import java.nio.charset.StandardCharsets;

final class LogLineEncoder extends EncoderBase<ILoggingEvent> {

    private static final byte[] NOTHING = new byte[0];

    private final LogLine line;

    LogLineEncoder(LogLine line) {
        this.line = line;
    }

    @Override
    public byte[] headerBytes() {
        return NOTHING;
    }

    @Override
    public byte[] encode(ILoggingEvent event) {
        return (line.format(event) + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] footerBytes() {
        return NOTHING;
    }
}
