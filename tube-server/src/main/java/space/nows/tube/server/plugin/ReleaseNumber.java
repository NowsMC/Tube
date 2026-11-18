package space.nows.tube.server.plugin;

import java.util.ArrayList;
import java.util.List;

final class ReleaseNumber {

    private ReleaseNumber() {
    }

    static boolean atLeast(String actual, String required) {
        List<Long> have = parts(actual);
        List<Long> need = parts(required);
        for (int i = 0; i < Math.max(have.size(), need.size()); i++) {
            long a = i < have.size() ? have.get(i) : 0;
            long b = i < need.size() ? need.get(i) : 0;
            if (a != b) {
                return a > b;
            }
        }
        return true;
    }

    private static List<Long> parts(String version) {
        List<Long> parts = new ArrayList<>();
        int end = version.indexOf('-');
        for (String part : (end < 0 ? version : version.substring(0, end)).split("\\.")) {
            try {
                parts.add(Long.parseLong(part.strip()));
            } catch (NumberFormatException e) {
                break;
            }
        }
        return parts;
    }
}
