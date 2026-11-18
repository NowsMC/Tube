package space.nows.tube.bootstrap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record Library(String group, String artifact, String version, String classifier) {

    String path() {
        String file = artifact + "-" + version + (classifier.isEmpty() ? "" : "-" + classifier) + ".jar";
        return group.replace('.', '/') + "/" + artifact + "/" + version + "/" + file;
    }

    public String coordinates() {
        return group + ":" + artifact + ":" + version + (classifier.isEmpty() ? "" : ":" + classifier);
    }

    public static Library of(String coordinates) {
        String[] parts = coordinates.strip().split(":");
        if (parts.length < 3 || parts.length > 4 || Arrays.stream(parts).anyMatch(String::isBlank)) {
            throw new IllegalArgumentException("library must be group:artifact:version, got " + coordinates);
        }
        return new Library(parts[0], parts[1], parts[2], parts.length == 4 ? parts[3] : "");
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
