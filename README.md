# Tube

Tube is a Minecraft server for modern versions. It uses Minestom as the engine and builds its own server around it: its own plugin API, its own systems and its own server management. Tube is not a fork of Paper, Spigot or any other server and contains no Mojang server code.

What Tube does:

- Runs from a single jar per Minecraft version and downloads the libraries it needs on first start.
- Reads the files server admins already know: `server.properties`, `ops.json`, the whitelist and the ban lists.
- Loads plugins written for Tube, and Bukkit plugins through a translator.
- Provides its own systems for worlds, player data, permissions, commands, RCON and Query, resource packs, item drops and pickups, skins, and a login API that can let chosen players join an online-mode server offline.
- Watches the tick and the connection: a tick watchdog, a packet rate limit and an idle timeout.

New worlds are flat; Tube does not generate terrain yet.

Supported Minecraft versions: 26.1.2, 26.2

## Bukkit

The Bukkit support in Tube is not the real Bukkit. The `org.bukkit` package is Tube's own implementation: calls that a plugin makes to the Bukkit API are translated into calls to the Tube API, and Tube's own API and management do the actual work. No Bukkit backend is used. Behavior is not guaranteed to match Bukkit, and many parts of the Bukkit API are not implemented. Plugins that need server internals (NMS, CraftBukkit) or APIs outside Bukkit do not work.

## License

Apache License 2.0. See `LICENSE` and `NOTICE`.
