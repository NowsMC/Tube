package space.nows.tube.api;

public interface CommandSender {

    String name();

    void sendMessage(String message);
}
