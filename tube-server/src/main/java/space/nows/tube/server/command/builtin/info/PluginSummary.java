package space.nows.tube.server.command.builtin.info;

import space.nows.tube.api.plugin.PluginDescriptor;

import java.util.ArrayList;
import java.util.List;

final class PluginSummary {

    private PluginSummary() {
    }

    static List<String> lines(PluginDescriptor plugin) {
        List<String> lines = new ArrayList<>();
        lines.add(plugin.name() + " " + plugin.version());
        if (present(plugin.description())) {
            lines.add(plugin.description());
        }
        if (!plugin.authors().isEmpty()) {
            lines.add("Authors: " + String.join(", ", plugin.authors()));
        }
        if (present(plugin.website())) {
            lines.add("Website: " + plugin.website());
        }
        if (!plugin.depends().isEmpty()) {
            lines.add("Depends on: " + String.join(", ", plugin.depends()));
        }
        if (!plugin.softDepends().isEmpty()) {
            lines.add("Uses when present: " + String.join(", ", plugin.softDepends()));
        }
        if (!plugin.commands().isEmpty()) {
            lines.add("Declares " + plugin.commands().size() + (plugin.commands().size() == 1 ? " command" : " commands"));
        }
        return lines;
    }

    private static boolean present(String text) {
        return text != null && !text.isBlank();
    }
}
