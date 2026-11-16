package org.bukkit.inventory.meta;

public interface BookMeta extends org.bukkit.inventory.meta.WritableBookMeta {

    boolean hasTitle();

    java.lang.String getTitle();

    boolean setTitle(java.lang.String p0);

    boolean hasAuthor();

    java.lang.String getAuthor();

    void setAuthor(java.lang.String p0);

    public enum Generation {
        ORIGINAL,
        COPY_OF_ORIGINAL,
        COPY_OF_COPY,
        TATTERED
    }
}
