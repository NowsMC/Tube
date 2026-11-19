package space.nows.tube.api.event.server;

import space.nows.tube.api.event.Event;

public record ServerReloadEvent(boolean pluginsReloaded) implements Event {
}
