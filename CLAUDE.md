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
- `MapMarker.compassImage` exists; the compass seems to be fed from the same markers. **Not yet confirmed in game** whether the tint applies on the compass, and how a tint looks on the pale-blue `Player.png` (vanilla user markers that get tinted are white). Fallback if tint doesn't work well: ship recolored copies of the icons in the asset pack and switch the image instead.
- Icons: `Common/UI/WorldMap/MapMarkers/` in the assets zip (`~/.gradle/caches/hytale-assets/release-0.7.2-Assets.zip`).

## Current implementation (prototype, 2026-10-22)

- `ColoredPlayersMarkerProvider`: copy of vanilla `OtherPlayersMarkerProvider` plus a `TintComponent` when the viewer chose a color. Registered under `"playerIcons"` on `AddWorldEvent` (the way built-in plugins add providers). **Re-sync with vanilla after game updates.**
- `MarkerColorStore`: viewer UUID → target UUID → 0xRRGGBB, in memory only (lost on restart).
- `/markercolor <player> <color|reset>`: color is a name (`MarkerColorParser.NAMED`) or `#RRGGBB`. Open to all players (`hytale:Adventurer`, the default group, like vanilla `/emote`). The target must be online.
- `MarkerColorParser`: game-independent, unit-tested.

## Open design questions (ask the owner)

- How players choose colors: command only, a custom UI page, the map's context menu (`ContextMenuItem` on markers)?
- Persistence: per player across restarts (where: plugin config file vs. player component)? Choosing colors for offline players?
- Color set: free hex colors or a fixed palette?
- Anything else for the compass/map (the project goal is broader than colors)?

## Features

Record design decisions per feature as the owner makes them; don't change decided behavior without asking.

## Code conventions

- One responsibility per class.
- Log through the plugin logger with a clear prefix; no `System.out`.
- Keep game-independent logic in plain Java classes and unit-test it without a server.
- Small, reviewable changes. Explain non-obvious Hytale-specific code with a short comment.
- Don't add third-party dependencies without asking.
