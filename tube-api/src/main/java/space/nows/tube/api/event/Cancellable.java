package space.nows.tube.api.event;

public interface Cancellable extends Event {

    boolean isCancelled();

    void setCancelled(boolean cancelled);
}
