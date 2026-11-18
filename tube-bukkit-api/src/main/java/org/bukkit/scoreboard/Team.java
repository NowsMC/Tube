package org.bukkit.scoreboard;

import java.util.Set;

public interface Team {

    enum Option {
        NAME_TAG_VISIBILITY,
        DEATH_MESSAGE_VISIBILITY,
        COLLISION_RULE
    }

    enum OptionStatus {
        ALWAYS,
        NEVER,
        FOR_OTHER_TEAMS,
        FOR_OWN_TEAM
    }

    String getName();

    String getDisplayName();

    void setDisplayName(String displayName);

    String getPrefix();

    void setPrefix(String prefix);

    String getSuffix();

    void setSuffix(String suffix);

    org.bukkit.ChatColor getColor();

    void setColor(org.bukkit.ChatColor color);

    boolean allowFriendlyFire();

    void setAllowFriendlyFire(boolean enabled);

    boolean canSeeFriendlyInvisibles();

    void setCanSeeFriendlyInvisibles(boolean enabled);

    OptionStatus getOption(Option option);

    void setOption(Option option, OptionStatus status);

    Set<String> getEntries();

    int getSize();

    void addEntry(String entry);

    boolean removeEntry(String entry);

    boolean hasEntry(String entry);

    Scoreboard getScoreboard();

    void unregister();
}
