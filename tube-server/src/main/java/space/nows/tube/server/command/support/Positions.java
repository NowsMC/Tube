package space.nows.tube.server.command.support;

import space.nows.tube.api.command.Suggestions;
import space.nows.tube.api.world.Location;

import java.util.List;

public final class Positions {

    private Positions() {
    }

    public record BlockPosition(int x, int y, int z) {
    }

    public static Location read(CommandInput input, Location origin) {
        double x = coordinate(input.word(), origin.x());
        double y = coordinate(input.word(), origin.y());
        double z = coordinate(input.word(), origin.z());
        return new Location(origin.world(), x, y, z, origin.yaw(), origin.pitch());
    }

    public static double[] readColumn(CommandInput input, Location origin) {
        double x = coordinate(input.word(), origin.x());
        double z = coordinate(input.word(), origin.z());
        return new double[]{x, z};
    }

    public static BlockPosition readBlock(CommandInput input, Location origin) {
        Location location = read(input, origin);
        return new BlockPosition((int) Math.floor(location.x()), (int) Math.floor(location.y()), (int) Math.floor(location.z()));
    }

    public static boolean isCoordinate(String text) {
        return text.startsWith("~") || !text.isEmpty() && (Character.isDigit(text.charAt(0)) || text.charAt(0) == '-');
    }

    public static List<String> suggest(List<String> args, int start) {
        int index = Typing.position(args) - start;
        return index >= 0 && index < 3 ? Suggestions.matching(Typing.current(args), "~") : List.of();
    }

    private static double coordinate(String text, double current) {
        try {
            if (text.startsWith("~")) {
                return current + (text.length() == 1 ? 0 : Double.parseDouble(text.substring(1)));
            }
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new CommandFailure("Invalid coordinate " + text);
        }
    }
}
