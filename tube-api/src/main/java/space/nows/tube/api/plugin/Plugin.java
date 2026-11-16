package space.nows.tube.api.plugin;

public interface Plugin {

    void onEnable(PluginContext context) throws Exception;

    default void onDisable() {
    }
}
