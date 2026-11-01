# Marker Colors

A Hytale server plugin that lets each player choose the color of other players' compass and map
markers. Work in progress.

> Built against Hytale Early Access **0.7.5** (`ServerVersion >=0.7.5 <0.8.0`); 1.0.0 was built for 0.7.2.
> Changes per version: [CHANGELOG.md](CHANGELOG.md).

## Usage

Right-click another player's marker on the world map and pick a color (the same palette as placeable
map markers), or "Default". The color shows on the map and on the compass. Or use the command:

```
/markercolor <player> <color>   # color: white, purple, orange, yellow, green, blue, or #RRGGBB
/markercolor <player> reset
```

Colors are personal: only you see the colors you choose. They are saved in `marker_colors.json` in the
plugin's data folder and survive server restarts.

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
