package space.nows.tube.server.command.builtin.entity.spread;

@FunctionalInterface
public interface SpreadFilter {

    boolean accepts(SpreadPoint point);
}
