package space.nows.tube.api.plugin;

import java.util.List;

public interface PluginConfig {

    boolean contains(String path);

    String getString(String path, String fallback);

    int getInt(String path, int fallback);

    double getDouble(String path, double fallback);

    boolean getBoolean(String path, boolean fallback);

    List<String> getStringList(String path);

    void set(String path, Object value);

    void save();

    void reload();
}
