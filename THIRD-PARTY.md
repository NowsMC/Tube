# Third-party software

Tube uses the following software. The CLI jar bundles picocli. The server jar bundles nothing third party: it downloads the other libraries from Maven repositories into `libraries/` on first start. Each library keeps its own license.

## Bundled in the CLI jar

| Library | Version | License | Project |
| --- | --- | --- | --- |
| picocli | 4.7.7 | Apache-2.0 | https://picocli.info |

## Downloaded at runtime

| Library | Version | License | Project |
| --- | --- | --- | --- |
| Minestom | 2026.10.07-26.2 | Apache-2.0 | https://github.com/minestom/minestom |
| Minestom data | 26.2-rv3 | Apache-2.0 | https://github.com/minestom/MinestomDataGenerator |
| Adventure (api, key, nbt, minimessage, text serializers, slf4j logger) | 5.2.0 | MIT | https://github.com/PaperMC/adventure |
| Kyori ansi | 1.1.1 | MIT | https://github.com/KyoriPowered/ansi |
| Kyori option | 1.1.0 | MIT | https://github.com/KyoriPowered/option |
| Netty (buffer, codec-base, common, resolver, transport, epoll) | 4.2.19.Final | Apache-2.0 | https://netty.io |
| fastutil | 8.5.19 | Apache-2.0 | https://fastutil.di.unimi.it |
| Flare, flare-fastutil | 2.0.1 | MIT | https://github.com/vectrix-space/flare |
| JCTools | 4.0.7 | Apache-2.0 | https://github.com/JCTools/JCTools |
| JNoise (core, generators) | 4.1.0 | MIT | https://github.com/Articdive/JNoise |
| Gson | 2.15.0 | Apache-2.0 | https://github.com/google/gson |
| SnakeYAML | 2.7 | Apache-2.0 | https://codeberg.org/snakeyaml/snakeyaml |
| jtoml (api, internals) | 1.8.3 | Apache-2.0 | https://github.com/WasabiThumb/jtoml |
| SLF4J API | 2.0.20 | MIT | https://www.slf4j.org |
| Logback (classic, core) | 1.6.5 | EPL-2.0 or LGPL-2.1 | https://logback.qos.ch |
| JetBrains annotations | 26.1.0 | Apache-2.0 | https://github.com/JetBrains/java-annotations |
| JSpecify | 1.0.0 | Apache-2.0 | https://jspecify.dev |
| Google auto-service annotations | 1.1.1 | Apache-2.0 | https://github.com/google/auto |
| Error Prone annotations | 2.50.0 | Apache-2.0 | https://github.com/google/error-prone |

## Downloaded for Bukkit plugins

| Library | Version | License | Project |
| --- | --- | --- | --- |
| Guava, failureaccess, listenablefuture | 33.6.0-jre | Apache-2.0 | https://github.com/google/guava |
| J2ObjC annotations | 3.1 | Apache-2.0 | https://github.com/google/j2objc |
| Log4j API, Log4j to SLF4J | 2.26.1 | Apache-2.0 | https://logging.apache.org/log4j/2.x |
| SQLite JDBC | 3.51.2.0 | Apache-2.0 | https://github.com/xerial/sqlite-jdbc |

## Minecraft data

Tube contains no Mojang code or data. On first start it downloads the vanilla server jar from Mojang's official servers, keeps only the recipe files in `cache/`, and deletes the jar. That data stays under Mojang's terms: https://www.minecraft.net/eula
