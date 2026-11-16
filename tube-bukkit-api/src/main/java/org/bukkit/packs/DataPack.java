package org.bukkit.packs;

import org.bukkit.Keyed;

public interface DataPack extends Keyed {

    String getTitle();

    String getDescription();

    boolean isEnabled();

    Source getSource();

    enum Source {
        DEFAULT,
        BUILT_IN,
        FEATURE,
        WORLD,
        SERVER
    }
}
