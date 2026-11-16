package space.nows.tube.api.event;

import space.nows.tube.api.command.CommandSender;

import java.util.Objects;

public final class CommandDispatchEvent implements Cancellable {

    private final CommandSender sender;
    private String line;
    private boolean cancelled;

    public CommandDispatchEvent(CommandSender sender, String line) {
        this.sender = sender;
        this.line = Objects.requireNonNull(line, "line");
    }

    public CommandSender sender() {
        return sender;
    }

    public String line() {
        return line;
    }

    public void setLine(String line) {
        this.line = Objects.requireNonNull(line, "line");
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
