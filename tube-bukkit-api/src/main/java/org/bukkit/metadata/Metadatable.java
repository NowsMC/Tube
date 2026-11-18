package org.bukkit.metadata;

public interface Metadatable {

    void setMetadata(java.lang.String p0, org.bukkit.metadata.MetadataValue p1);

    java.util.List<org.bukkit.metadata.MetadataValue> getMetadata(java.lang.String p0);

    boolean hasMetadata(java.lang.String p0);

    void removeMetadata(java.lang.String p0, org.bukkit.plugin.Plugin p1);
}
