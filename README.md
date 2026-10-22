# Marker Colors

A Hytale server plugin that lets each player choose the color of other players' compass and map
markers. Work in progress.

> Built against Hytale Early Access **0.7.2** (`ServerVersion >=0.7.2 <0.8.0`).

## Usage

Right-click another player's marker on the world map and pick a color (the same palette as placeable
map markers), or "Default". The color shows on the map and on the compass. Or use the command:

```
/markercolor <player> <color>   # color: white, pink, orange, yellow, green, blue, or #RRGGBB
/markercolor <player> reset
```

Colors are personal: only you see the colors you choose. They are not saved across server restarts yet.

## Development

Requirements: **Java 25** (JetBrains Runtime recommended). Gradle comes with the wrapper.

```bash
./gradlew setupHytaleDev   # download the server + assets (needs a Hytale account login)
./gradlew build            # compile, run unit tests, build build/libs/markercolors-<version>.jar
./gradlew runServer        # local dev server on port 5520; join it from the game
```

Project metadata (IDs, version, author, server version range) lives in `gradle.properties`; the
manifest is generated from it (`./gradlew updatePluginManifest`). Project notes and design decisions
are in [`CLAUDE.md`](CLAUDE.md).

## Credits

Built on the [HytaleModding plugin template](https://github.com/HytaleModding/plugin-template) and
AzureDoom's [Hytale Gradle Plugin](https://github.com/AzureDoom/Hytale-Gradle-Plugin).

## License

MIT (as declared in `gradle.properties`).
