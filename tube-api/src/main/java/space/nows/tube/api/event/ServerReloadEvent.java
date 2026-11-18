package space.nows.tube.api.event;

public record ServerReloadEvent(boolean pluginsReloaded) implements Event {
}
