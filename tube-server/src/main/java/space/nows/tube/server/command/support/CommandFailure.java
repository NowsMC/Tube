package space.nows.tube.server.command.support;

public final class CommandFailure extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CommandFailure(String message) {
        super(message, null, false, false);
    }
}
