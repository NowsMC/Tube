package space.nows.tube.server;

public interface Restarter {

    boolean available();

    void restart();
}
