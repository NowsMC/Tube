# Tube

Tube is not a fork of any server. It assembles existing libraries into a launcher for modern Minecraft servers, with Minestom as the engine and its own plugin API on top. One server jar is built per Minecraft version.

Supported Minecraft versions: 26.1.2, 26.2

## Bukkit

The Bukkit support in Tube is not the real Bukkit. The `org.bukkit` package is Tube's own implementation: calls that a plugin makes to the Bukkit API are translated into calls to the Tube API, and Tube's own API and management do the actual work. No Bukkit backend is used. Behavior is not guaranteed to match Bukkit, and many parts of the Bukkit API are not implemented.

## License

Apache License 2.0. See `LICENSE` and `NOTICE`.
