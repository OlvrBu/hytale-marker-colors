# CLAUDE.md — Marker Colors (Hytale server plugin)

## What this project is

A Hytale (Early Access, 0.7.x) server plugin that extends the compass and world map. Name: "Marker Colors" (mod ID `markercolors`, package `io.github.obut.markercolors`, main class `MarkerColorsPlugin`). The name is a placeholder the owner may change.

**Goal:** each player chooses the color of other players' compass and map markers, for themselves only. Example: players A, B, C; A sees B in green and C in red, while B and C still see the defaults (or their own choices).

Created on 2026-10-22 from the HytaleModding plugin template (https://github.com/HytaleModding/plugin-template, commit `b7d12fd`), set up like the owner's other mods: Transmutation Chest (`../transmutation_chest`: block component, custom UI, commands, config files, unit tests) and Law Of Milk (`../law_of_milk`: custom interactions, asset overrides). Look there first for patterns already verified on this server version. Note: the dev server uses `src/main/resources` as the plugin's data folder.

## Working with the developer

- Background and preferences of the developer are in the workspace `../CLAUDE.md` (not part of this repo); follow them.
- You cannot run the game client. Anything that needs in-game verification: tell the developer exactly what to do in-game and what log output / behaviour to report back. Don't mark a milestone done until they confirm.
- Testing per-viewer colors properly needs at least two players on the dev server.

## Toolchain & commands

- Java 25 (JetBrains Runtime recommended). Gradle wrapper — never require a system Gradle.
- AzureDoom's `com.azuredoom.hytale-tools` Gradle plugin. Project metadata lives in `gradle.properties`; the manifest is generated from it.

```bash
./gradlew setupHytaleDev                       # fetch/sync server jar + assets for dev
./gradlew build                                # compile, test, package -> build/libs/
./gradlew runServer                            # local dev server; connect from game to localhost:5520
./gradlew runServer -Ddebug=true -Dhotswap=true
./gradlew updatePluginManifest                 # after changing manifest-related properties
./gradlew build --refresh-dependencies         # if resolution fails
```

- Decompiled server sources (after `setupHytaleDev`): `build/vineflower/hytale-server/`.
- Dev server login: the first `runServer` of this project needs `/auth login device` in the server console; the login is then stored encrypted in `run/`.
- Singleplayer mods folder: `UserData/Mods` in the Hytale data folder

Always run `./gradlew build` after changes and fix all compile errors and test failures before reporting back.

## API accuracy rules (important)

The Hytale API is young, changes between Early Access updates, and is not well represented in your training data. Community docs (hytalemodding.dev) are good but can lag behind the current server version.

- **Never invent class names, method signatures, event names, or JSON keys.** Verify against the actual server JAR resolved by Gradle (decompiled sources, `javap`), the game assets, or an up-to-date guide.
- If you can't verify something, say so explicitly and propose how to check it, rather than guessing.
- When a community guide and the server JAR disagree, the JAR wins.
- Useful references: https://hytalemodding.dev/en/docs, https://github.com/AzureDoom/Hytale-Gradle-Plugin

## How player markers work (server 0.7.2, from decompiled sources)

- Each world's `WorldMapManager` holds named `MarkerProvider`s (`getMarkerProviders()`, `addMarkerProvider(key, provider)`, a plain map, so adding under an existing key replaces it). Vanilla registers `OtherPlayersMarkerProvider` as `"playerIcons"` in the `WorldMapManager` constructor.
- `MarkerProvider.update(World, Player viewer, MarkersCollector)` is called **per viewing player**, so markers can differ per viewer. Vanilla builds one `MapMarker` per other player: id `"Player-<uuid>"`, image `"Player.png"`, name = username, plus `PlayerMarkerComponent` and `HeightDeltaIconComponent` (above/below icons `PlayerAbove.png`/`PlayerBelow.png` from the gameplay config), added with `addIgnoreViewDistance`.
- `MapMarker` has no color field, but the protocol has a `TintComponent(Color)` marker component (`protocol.Color`: 3 signed bytes RGB). Vanilla uses it for user-placed map markers (`UserMapMarker`).
- `MapMarkerTracker` resends a marker when its name, image, icon size, compass image or components change (components compared with `Arrays.equals`; `TintComponent`/`Color` implement `equals`), so a color change shows up on the next update.
- **Confirmed in game 2026-10-22:** the `TintComponent` colors the player marker on both the map and the compass, including the above/below icons. No recolored icons needed.
- A `MapMarker` can carry `ContextMenuItem(name, command)` entries. The server itself never uses them. **Confirmed in game 2026-10-22:** they show on right-click on the map; clicking runs the command as the viewer and shows its chat feedback. The command must have **no leading `/`** (the variant with a slash did not work for the owner). The map UI itself is client-side and not in the assets; context menu items are the only server-side hook into it found so far.
- The user-marker color palette is client-side too (not in assets or server code). A chosen color arrives as `CreateUserMarker.tintColor` and is saved as `ColorTint` on the marker (personal markers: in `universe/players/<uuid>.json`; shared: world resource `SharedUserMapMarkers`). Read on 2026-10-22 from markers the owner placed in all colors (6), ordered by placement position: white `#FFFFFF`, pink `#FF1399`, orange `#FF7512`, yellow `#ECE133`, green `#00F482`, blue `#1880FF`. **Names are ours, not the game's.**
- Icons: `Common/UI/WorldMap/MapMarkers/` in the assets zip (`~/.gradle/caches/hytale-assets/release-0.7.2-Assets.zip`).

## Current implementation (2026-10-22)

- `ColoredPlayersMarkerProvider`: copy of vanilla `OtherPlayersMarkerProvider` plus a `TintComponent` when the viewer chose a color. Registered under `"playerIcons"` on `AddWorldEvent` (the way built-in plugins add providers). **Re-sync with vanilla after game updates.**
- `MarkerColorStore`: viewer UUID → target UUID → 0xRRGGBB, in memory only (lost on restart).
- Right-click menu on every other-player marker on the map: "Color: White" … "Color: Blue" (the vanilla palette) and "Color: Default"; each runs `/markercolor <name> <color>`.
- `/markercolor <player> <color|reset>`: color is a palette name (`MarkerColorParser.NAMED`) or any `#RRGGBB`. Open to all players (`hytale:Adventurer`, the default group, like vanilla `/emote`). The target must be online.
- `MarkerColorParser`: game-independent, unit-tested.
- Test aid: `/markerdummy` (operator-only) places a fake player marker "Dummy" at your position (run again next to it to remove); `/markercolor Dummy <color>` colors it. Not saved.

## Decided

- **(2026-10-22)** Players choose colors from the map UI, using the same palette the vanilla placeable map markers offer. Built as context menu entries on player markers.

## Open design questions (ask the owner)

- Persistence: per player across restarts (where: plugin config file vs. player component)? Choosing colors for offline players?
- Anything else for the compass/map (the project goal is broader than colors)?

## Features

Record design decisions per feature as the owner makes them; don't change decided behavior without asking.

## Code conventions

- One responsibility per class.
- Log through the plugin logger with a clear prefix; no `System.out`.
- Keep game-independent logic in plain Java classes and unit-test it without a server.
- Small, reviewable changes. Explain non-obvious Hytale-specific code with a short comment.
- Don't add third-party dependencies without asking.
