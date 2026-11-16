package org.bukkit.configuration.file;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationOptions;
import org.bukkit.configuration.MemoryConfiguration;

public class FileConfigurationOptions extends ConfigurationOptions {

    private String header;
    private boolean copyHeader = true;
    private String footer;
    private boolean parseComments = true;

    protected FileConfigurationOptions(MemoryConfiguration configuration) {
        super(configuration);
    }

    @Override
    public FileConfigurationOptions copyDefaults(boolean value) {
        super.copyDefaults(value);
        return this;
    }

    @Override
    public FileConfigurationOptions pathSeparator(char value) {
        super.pathSeparator(value);
        return this;
    }

    public String header() {
        return header;
    }

    public FileConfigurationOptions header(String value) {
        this.header = value;
        return this;
    }

    public java.util.List<String> getHeader() {
        return header == null ? java.util.List.of() : header.lines().toList();
    }

    public FileConfigurationOptions setHeader(java.util.List<String> value) {
        this.header = value == null || value.isEmpty() ? null : String.join("\n", value);
        return this;
    }

    public java.util.List<String> getFooter() {
        return footer == null ? java.util.List.of() : footer.lines().toList();
    }

    public FileConfigurationOptions setFooter(java.util.List<String> value) {
        this.footer = value == null || value.isEmpty() ? null : String.join("\n", value);
        return this;
    }

    public boolean parseComments() {
        return parseComments;
    }

    public FileConfigurationOptions parseComments(boolean value) {
        this.parseComments = value;
        return this;
    }

    public boolean copyHeader() {
        return copyHeader;
    }

    public FileConfigurationOptions copyHeader(boolean value) {
        this.copyHeader = value;
        return this;
    }

    @Override
    public Configuration configuration() {
        return super.configuration();
    }
}
