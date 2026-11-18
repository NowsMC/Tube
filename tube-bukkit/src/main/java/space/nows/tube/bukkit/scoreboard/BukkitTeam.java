package space.nows.tube.bukkit.scoreboard;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import space.nows.tube.api.scoreboard.CollisionRule;
import space.nows.tube.api.scoreboard.NameTagVisibility;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

final class BukkitTeam implements Team {

    private final BukkitScoreboard scoreboard;
    private final String name;
    private final Set<String> entries = new LinkedHashSet<>();
    private final Map<Option, OptionStatus> options = new EnumMap<>(Option.class);
    private final space.nows.tube.api.scoreboard.Team tube;
    private String displayName;
    private String prefix = "";
    private String suffix = "";
    private ChatColor color = ChatColor.RESET;
    private boolean friendlyFire;
    private boolean seeInvisible = true;

    BukkitTeam(BukkitScoreboard scoreboard, String name) {
        this.scoreboard = scoreboard;
        this.name = name;
        this.displayName = name;
        for (Option option : Option.values()) {
            options.put(option, OptionStatus.ALWAYS);
        }
        this.tube = scoreboard.isMain() ? backing(scoreboard, name) : null;
    }

    private static space.nows.tube.api.scoreboard.Team backing(BukkitScoreboard scoreboard, String name) {
        var scoreboards = scoreboard.runtime().tube().scoreboards();
        return scoreboards.team(name).orElseGet(() -> scoreboards.createTeam(name));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        if (tube != null) {
            tube.setDisplayName(BukkitText.component(displayName));
        }
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public void setPrefix(String prefix) {
        this.prefix = prefix == null ? "" : prefix;
        if (tube != null) {
            tube.setPrefix(BukkitText.component(this.prefix));
        }
    }

    @Override
    public String getSuffix() {
        return suffix;
    }

    @Override
    public void setSuffix(String suffix) {
        this.suffix = suffix == null ? "" : suffix;
        if (tube != null) {
            tube.setSuffix(BukkitText.component(this.suffix));
        }
    }

    @Override
    public ChatColor getColor() {
        return color;
    }

    @Override
    public void setColor(ChatColor color) {
        if (color.isFormat()) {
            throw new IllegalArgumentException("Only color or reset is allowed");
        }
        this.color = color;
        if (tube != null) {
            tube.setColor(color == ChatColor.RESET ? NamedTextColor.WHITE : NamedTextColor.NAMES.value(color.name().toLowerCase(java.util.Locale.ROOT)));
        }
    }

    @Override
    public boolean allowFriendlyFire() {
        return friendlyFire;
    }

    @Override
    public void setAllowFriendlyFire(boolean enabled) {
        this.friendlyFire = enabled;
        if (tube != null) {
            tube.setFriendlyFire(enabled);
        }
    }

    @Override
    public boolean canSeeFriendlyInvisibles() {
        return seeInvisible;
    }

    @Override
    public void setCanSeeFriendlyInvisibles(boolean enabled) {
        this.seeInvisible = enabled;
        if (tube != null) {
            tube.setSeeInvisibleMembers(enabled);
        }
    }

    @Override
    public OptionStatus getOption(Option option) {
        return options.get(option);
    }

    @Override
    public void setOption(Option option, OptionStatus status) {
        options.put(option, status);
        if (tube == null) {
            return;
        }
        if (option == Option.NAME_TAG_VISIBILITY) {
            tube.setNameTagVisibility(switch (status) {
                case ALWAYS -> NameTagVisibility.ALWAYS;
                case NEVER -> NameTagVisibility.NEVER;
                case FOR_OTHER_TEAMS -> NameTagVisibility.HIDE_FOR_OWN_TEAM;
                case FOR_OWN_TEAM -> NameTagVisibility.HIDE_FOR_OTHER_TEAMS;
            });
        } else if (option == Option.COLLISION_RULE) {
            tube.setCollisionRule(switch (status) {
                case ALWAYS -> CollisionRule.ALWAYS;
                case NEVER -> CollisionRule.NEVER;
                case FOR_OTHER_TEAMS -> CollisionRule.PUSH_OTHER_TEAMS;
                case FOR_OWN_TEAM -> CollisionRule.PUSH_OWN_TEAM;
            });
        }
    }

    @Override
    public synchronized Set<String> getEntries() {
        return new LinkedHashSet<>(entries);
    }

    @Override
    public synchronized int getSize() {
        return entries.size();
    }

    @Override
    public synchronized void addEntry(String entry) {
        entries.add(entry);
        if (tube != null) {
            tube.addMember(entry);
        }
    }

    @Override
    public synchronized boolean removeEntry(String entry) {
        boolean removed = entries.remove(entry);
        if (removed && tube != null) {
            tube.removeMember(entry);
        }
        return removed;
    }

    @Override
    public synchronized boolean hasEntry(String entry) {
        return entries.contains(entry);
    }

    @Override
    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    @Override
    public void unregister() {
        scoreboard.forgetTeam(this);
        if (tube != null) {
            scoreboard.runtime().tube().scoreboards().removeTeam(tube);
        }
    }
}
