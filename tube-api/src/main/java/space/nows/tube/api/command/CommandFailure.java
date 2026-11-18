package space.nows.tube.api.command;

public class CommandFailure extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CommandFailure(String message) {
        super(message, null, false, false);
    }
}
