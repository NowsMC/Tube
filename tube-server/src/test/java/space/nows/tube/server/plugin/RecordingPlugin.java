package space.nows.tube.server.plugin;

import space.nows.tube.api.Plugin;
import space.nows.tube.api.PluginContext;

import java.util.ArrayList;
import java.util.List;

public final class RecordingPlugin implements Plugin {

    static final List<String> EVENTS = new ArrayList<>();

    private String name;

    @Override
    public void onEnable(PluginContext context) {
        name = context.descriptor().name();
        context.commands().register("cmd-" + name, (sender, args) -> { });
        EVENTS.add("enable:" + name + ":" + context.config().getString("greeting", "none"));
    }

    @Override
    public void onDisable() {
        EVENTS.add("disable:" + name);
    }
}
