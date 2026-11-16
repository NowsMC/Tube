package space.nows.tube.api;

public interface Plugin {

    void onEnable(PluginContext context) throws Exception;

    default void onDisable() {
    }
}
