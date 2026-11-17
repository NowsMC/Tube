# Tube

Tube is a Minecraft server for modern versions. It uses Minestom as the engine and builds its own server around it: its own plugin API, its own systems and its own server management. Tube is not a fork of Paper, Spigot or any other server and contains no Mojang server code.

> Tube uses Minestom instead of NMS on purpose. The goal is to try a different standard, not to build another Paper.

Tube plays like a normal survival server and keeps the files server admins already know, such as `server.properties`. It runs plugins written for Tube, and Bukkit plugins through a translator. On first start it downloads the libraries it needs and reads the recipes from Mojang's server jar.

Not implemented yet: the End, villages and other structures, redstone circuits, enchanting, brewing and advancements.

Supported Minecraft version: 26.2

## Bukkit

The Bukkit support in Tube is not the real Bukkit. The `org.bukkit` package is Tube's own implementation: calls that a plugin makes to the Bukkit API are translated into calls to the Tube API, and Tube's own API and management do the actual work. No Bukkit backend is used. Behavior is not guaranteed to match Bukkit, and many parts of the Bukkit API are not implemented. Plugins that need server internals (NMS, CraftBukkit) or APIs outside Bukkit do not work.

## License

Apache License 2.0. See `LICENSE` and `NOTICE`.
