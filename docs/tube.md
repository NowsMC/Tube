# Working on Tube

Tube is a Minecraft server built on Minestom with its own API and systems. Bukkit plugins run through a translator. There is no NMS and no CraftBukkit, and there will not be.

## Where a change goes

| Module | Put here |
| --- | --- |
| `tube-api` | The public API that plugins use. |
| `tube-server` | Tube's own server systems: world generation, gameplay, mobs and server management. Minestom only runs the protocol and chunks underneath, and no other module touches it. |
| `tube-network` | Netty sockets, RCON and Query. |
| `tube-bootstrap` | The entry point of every server jar. It downloads the server's runtime libraries, then starts the server. |
| `tube-bukkit-api` | Tube's own `org.bukkit` classes. |
| `tube-bukkit` | The translator from Bukkit calls to the Tube API. |
| `versions/<minecraft version>` | One server jar per Minecraft version. |

- A new feature is designed in Tube's terms first: API in `tube-api`, implementation in `tube-server`.
- Bukkit support only translates. Add to `tube-bukkit-api` what plugins actually call, and implement it in `tube-bukkit` on top of the Tube API. If the Tube API cannot do it yet, extend the Tube API first.
- `tools/bukkit` measures which Bukkit classes and methods plugins need.
- Normal world generation is in `tube-server` under `world/vanilla`. It reads Mojang's world generation data, downloaded at run time.
- Mobs are in `tube-server` under `mob`: `Creature` is the entity, each mob type is one `CreatureKind` in `mob/kind`, and goals, movement, senses and traits each have their own package.
- The console and the log format are in `tube-server` under `console`, configured by `tube.yml`.
- Plugins add commands through `PluginContext.commands()`. Argument reading, positions, target selectors and registry names for completion are in the API (`CommandInput`, `Positions`, `Server.targets()`, `Server.registries()`), so a plugin command parses like a built-in one.
- Plugins shade the libraries they need into their own jar. Tube does not download libraries for plugins.

## Versions

`MAJOR.YYMM.PATCH`, for example `0.2611.0`.

- A bug fix raises PATCH.
- A release with new features takes the current year and month as YYMM and resets PATCH. A month has at most one feature release.
- A breaking change raises MAJOR. While MAJOR is 0, anything may still break.

The version is the `revision` property in the root `pom.xml`. Each release has its entries in `CHANGELOG.md`.
