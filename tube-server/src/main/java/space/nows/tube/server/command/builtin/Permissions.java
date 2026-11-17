package space.nows.tube.server.command.builtin;

final class Permissions {

    static final String ADMIN = "tube.admin";
    static final String OP = "tube.op";
    static final String KICK = "tube.kick";
    static final String BAN = "tube.ban";
    static final String WHITELIST = "tube.whitelist";

    private Permissions() {
    }

    static String command(String name) {
        return "tube.command." + name;
    }
}
