package org.bukkit.configuration.file;

public class YamlConfigurationOptions extends FileConfigurationOptions {

    private int indent = 2;
    private int width = 80;

    protected YamlConfigurationOptions(YamlConfiguration configuration) {
        super(configuration);
    }

    @Override
    public YamlConfigurationOptions copyDefaults(boolean value) {
        super.copyDefaults(value);
        return this;
    }

    @Override
    public YamlConfigurationOptions pathSeparator(char value) {
        super.pathSeparator(value);
        return this;
    }

    @Override
    public YamlConfigurationOptions header(String value) {
        super.header(value);
        return this;
    }

    @Override
    public YamlConfigurationOptions copyHeader(boolean value) {
        super.copyHeader(value);
        return this;
    }

    @Override
    public YamlConfigurationOptions setHeader(java.util.List<String> value) {
        super.setHeader(value);
        return this;
    }

    @Override
    public YamlConfigurationOptions setFooter(java.util.List<String> value) {
        super.setFooter(value);
        return this;
    }

    @Override
    public YamlConfigurationOptions parseComments(boolean value) {
        super.parseComments(value);
        return this;
    }

    public int indent() {
        return indent;
    }

    public YamlConfigurationOptions indent(int value) {
        if (value < 2 || value > 9) {
            throw new IllegalArgumentException("Indent must be between 2 and 9 characters");
        }
        this.indent = value;
        return this;
    }

    public int width() {
        return width;
    }

    public YamlConfigurationOptions width(int value) {
        this.width = value;
        return this;
    }
}
