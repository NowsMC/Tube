package space.nows.tube.server.command.builtin;

public final class Permissions {

    public static final String ADMIN = "tube.admin";
    public static final String OP = "tube.op";
    public static final String KICK = "tube.kick";
    public static final String BAN = "tube.ban";
    public static final String WHITELIST = "tube.whitelist";

    private Permissions() {
    }

    public static String command(String name) {
        return "tube.command." + name;
    }
}
