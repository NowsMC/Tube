package space.nows.tube.bootstrap;

import java.util.ArrayList;
import java.util.List;

record Library(String group, String artifact, String version, String classifier) {

    private static final java.util.regex.Pattern TIMESTAMPED = java.util.regex.Pattern.compile("(.+)-\\d{8}\\.\\d{6}-\\d+");

    String path() {
        String file = artifact + "-" + version + (classifier.isEmpty() ? "" : "-" + classifier) + ".jar";
        return group.replace('.', '/') + "/" + artifact + "/" + directoryVersion() + "/" + file;
    }

    String key() {
        return group + ":" + artifact;
    }

    private String directoryVersion() {
        var matcher = TIMESTAMPED.matcher(version);
        return matcher.matches() ? matcher.group(1) + "-SNAPSHOT" : version;
    }

    String coordinates() {
        return group + ":" + artifact + ":" + version + (classifier.isEmpty() ? "" : ":" + classifier);
    }

    static List<Library> parse(String text) {
        List<Library> libraries = new ArrayList<>();
        for (String raw : text.split("\\R")) {
            String line = raw.strip();
            int note = line.indexOf(" -- ");
            if (note >= 0) {
                line = line.substring(0, note);
            }
            String[] parts = line.split(":");
            if (parts.length < 5 || !parts[2].equals("jar")) {
                continue;
            }
            if (parts.length == 5) {
                libraries.add(new Library(parts[0], parts[1], parts[3], ""));
            } else {
                libraries.add(new Library(parts[0], parts[1], parts[4], parts[3]));
            }
        }
        return List.copyOf(libraries);
    }
}
