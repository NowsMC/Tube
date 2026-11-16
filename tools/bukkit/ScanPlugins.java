import com.google.gson.GsonBuilder;

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.MemberRefEntry;
import java.lang.classfile.constantpool.PoolEntry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

// Lists the org.bukkit classes and members each plugin jar references.
// usage: java -cp <gson> ScanPlugins.java <plugin jar folder> <output scan.json>
public class ScanPlugins {

    static final Map<String, String> PREFIXES = new LinkedHashMap<>();

    static {
        PREFIXES.put("bukkit", "org/bukkit/");
        PREFIXES.put("craftbukkit", "org/bukkit/craftbukkit/");
        PREFIXES.put("spigot", "org/spigotmc/");
        PREFIXES.put("paper", "io/papermc/paper/");
        PREFIXES.put("paperold", "com/destroystokyo/paper/");
        PREFIXES.put("folia", "io/papermc/paper/threadedregions/");
        PREFIXES.put("nms", "net/minecraft/");
        PREFIXES.put("netty", "io/netty/");
        PREFIXES.put("protocollib", "com/comphenix/protocol/");
        PREFIXES.put("adventure", "net/kyori/");
        PREFIXES.put("authlib", "com/mojang/");
    }

    static final List<String> DESCRIPTORS = List.of("plugin.yml", "paper-plugin.yml", "fabric.mod.json", "META-INF/mods.toml", "velocity-plugin.json", "bungee.yml");
    static final List<String> DESCRIPTOR_KEYS = List.of("name", "version", "main", "api-version", "folia-supported", "depend", "softdepend", "load", "bootstrapper", "loader");

    public static void main(String[] args) throws Exception {
        Map<String, Object> plugins = new TreeMap<>();
        Map<String, Set<String>> classUsers = new TreeMap<>();
        Map<String, Set<String>> memberUsers = new TreeMap<>();
        List<Path> jars;
        try (var files = Files.list(Path.of(args[0]))) {
            jars = files.filter(p -> p.getFileName().toString().endsWith(".jar")).sorted().toList();
        }
        for (Path path : jars) {
            String name = path.getFileName().toString();
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("size", Files.size(path));
            try (ZipFile jar = new ZipFile(path.toFile())) {
                Set<String> own = new HashSet<>();
                jar.stream().filter(e -> e.getName().endsWith(".class")).forEach(e -> own.add(e.getName().substring(0, e.getName().length() - 6)));
                for (String descriptor : DESCRIPTORS) {
                    var entry = jar.getEntry(descriptor);
                    if (entry == null) {
                        continue;
                    }
                    @SuppressWarnings("unchecked")
                    List<String> found = (List<String>) info.computeIfAbsent("descriptors", k -> new java.util.ArrayList<String>());
                    found.add(descriptor);
                    if (descriptor.endsWith(".yml")) {
                        String text = new String(jar.getInputStream(entry).readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                        Map<String, String> keys = new LinkedHashMap<>();
                        for (String key : DESCRIPTOR_KEYS) {
                            Matcher m = Pattern.compile("^" + Pattern.quote(key) + ":\\s*(.+)$", Pattern.MULTILINE).matcher(text);
                            keys.put(key, m.find() ? m.group(1).strip() : null);
                        }
                        info.put(descriptor, keys);
                    }
                }
                Map<String, Integer> counts = new TreeMap<>();
                Set<String> bukkitClasses = new TreeSet<>();
                Set<String> bukkitMembers = new TreeSet<>();
                for (var entry : jar.stream().filter(e -> e.getName().endsWith(".class")).toList()) {
                    ClassModel model;
                    try {
                        model = ClassFile.of().parse(jar.getInputStream(entry).readAllBytes());
                    } catch (IllegalArgumentException e) {
                        continue;
                    }
                    for (PoolEntry pool : model.constantPool()) {
                        if (pool instanceof ClassEntry cls) {
                            String internal = cls.asInternalName();
                            if (own.contains(internal)) {
                                continue;
                            }
                            PREFIXES.forEach((label, prefix) -> {
                                if (internal.startsWith(prefix)) {
                                    counts.merge(label, 1, Integer::sum);
                                }
                            });
                            if (internal.startsWith("org/bukkit/")) {
                                bukkitClasses.add(internal);
                            }
                        } else if (pool instanceof MemberRefEntry ref) {
                            String owner = ref.owner().asInternalName();
                            if (!own.contains(owner) && owner.startsWith("org/bukkit/")) {
                                bukkitMembers.add(owner + "#" + ref.name().stringValue() + ref.type().stringValue());
                            }
                        }
                    }
                }
                info.put("refs", counts);
                info.put("bukkit_class_count", bukkitClasses.size());
                info.put("bukkit_member_count", bukkitMembers.size());
                bukkitClasses.forEach(c -> classUsers.computeIfAbsent(c, k -> new TreeSet<>()).add(name));
                bukkitMembers.forEach(m -> memberUsers.computeIfAbsent(m, k -> new TreeSet<>()).add(name));
            } catch (Exception e) {
                info.put("error", String.valueOf(e));
            }
            plugins.put(name, info);
        }
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("plugins", plugins);
        summary.put("classes", classUsers);
        summary.put("members", memberUsers);
        Files.writeString(Path.of(args[1]), new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(summary));
        System.out.println(plugins.size() + " jars, " + classUsers.size() + " bukkit classes, " + memberUsers.size() + " bukkit members");
    }
}
