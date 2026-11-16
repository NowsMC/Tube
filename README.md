# Tube

Minecraft server with its own plugin API, built on Minestom. One server jar is built per Minecraft version.

Supported Minecraft versions: 26.1.2, 26.2

## Build

    ./mvnw verify

Server jars are written to `versions/<version>/target/tube-<version>.jar`. Run one from an empty directory with Java 25:

    java -jar tube-26.2.jar

First start creates `tube.yml`, `plugins/` and `worlds/`. Type `help` in the console for commands.

## Plugins

A plugin is a jar containing `tube-plugin.yml` with `name`, `version`, `main` and an optional `depends` list, and a `main` class implementing `space.nows.tube.api.Plugin`. An optional `config.yml` in the jar is copied to the plugin's data folder on first run.

## License

Apache License 2.0. See `LICENSE` and `NOTICE`.
