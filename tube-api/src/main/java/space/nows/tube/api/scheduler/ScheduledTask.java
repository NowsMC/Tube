package space.nows.tube.api.scheduler;

public interface ScheduledTask {

    void cancel();

    boolean isActive();
}
