package space.nows.tube.api.scoreboard;

import net.kyori.adventure.text.Component;

public record SidebarLine(String id, Component text, int score) {
}
