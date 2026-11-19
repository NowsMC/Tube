package space.nows.tube.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.color.TeamColor;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import space.nows.tube.api.scoreboard.team.CollisionRule;
import space.nows.tube.api.scoreboard.team.NameTagVisibility;
import space.nows.tube.api.scoreboard.team.Team;

import java.util.Locale;
import java.util.Set;

final class MinestomTeam implements Team {

    private final net.minestom.server.scoreboard.Team team;
    private NamedTextColor color = NamedTextColor.WHITE;

    MinestomTeam(net.minestom.server.scoreboard.Team team) {
        this.team = team;
    }

    net.minestom.server.scoreboard.Team handle() {
        return team;
    }

    @Override
    public String name() {
        return team.getTeamName();
    }

    @Override
    public Component displayName() {
        return team.getTeamDisplayName();
    }

    @Override
    public void setDisplayName(Component displayName) {
        team.updateTeamDisplayName(displayName == null ? Component.empty() : displayName);
    }

    @Override
    public Component prefix() {
        return team.getPrefix();
    }

    @Override
    public void setPrefix(Component prefix) {
        team.updatePrefix(prefix == null ? Component.empty() : prefix);
    }

    @Override
    public Component suffix() {
        return team.getSuffix();
    }

    @Override
    public void setSuffix(Component suffix) {
        team.updateSuffix(suffix == null ? Component.empty() : suffix);
    }

    @Override
    public NamedTextColor color() {
        return color;
    }

    @Override
    public void setColor(NamedTextColor color) {
        this.color = color == null ? NamedTextColor.WHITE : color;
        team.updateTeamColor(TeamColor.valueOf(NamedTextColor.NAMES.key(this.color).toUpperCase(Locale.ROOT)));
    }

    @Override
    public NameTagVisibility nameTagVisibility() {
        return NameTagVisibility.valueOf(team.getNameTagVisibility().name());
    }

    @Override
    public void setNameTagVisibility(NameTagVisibility visibility) {
        team.updateNameTagVisibility(TeamsPacket.NameTagVisibility.valueOf(visibility.name()));
    }

    @Override
    public CollisionRule collisionRule() {
        return CollisionRule.valueOf(team.getCollisionRule().name());
    }

    @Override
    public void setCollisionRule(CollisionRule rule) {
        team.updateCollisionRule(TeamsPacket.CollisionRule.valueOf(rule.name()));
    }

    @Override
    public boolean friendlyFire() {
        return team.isAllowFriendlyFire();
    }

    @Override
    public void setFriendlyFire(boolean allowed) {
        team.updateAllowFriendlyFire(allowed);
    }

    @Override
    public boolean seeInvisibleMembers() {
        return team.isSeeInvisiblePlayers();
    }

    @Override
    public void setSeeInvisibleMembers(boolean see) {
        team.updateSeeInvisiblePlayers(see);
    }

    @Override
    public Set<String> members() {
        return Set.copyOf(team.getMembers());
    }

    @Override
    public void addMember(String entry) {
        team.addMember(entry);
    }

    @Override
    public void removeMember(String entry) {
        team.removeMember(entry);
    }
}
