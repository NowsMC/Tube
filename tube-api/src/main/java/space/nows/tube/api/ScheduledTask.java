package space.nows.tube.api;

public interface ScheduledTask {

    void cancel();

    boolean isActive();
}
