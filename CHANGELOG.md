# Changelog

## Unreleased

- Requires Hytale **0.7.5** or newer (was 0.7.2).
- The mod's ID changed from `obut:markercolors` to `olvrbu:markercolors` (new author name).
  **When updating:** delete the old `markercolors-1.0.0.jar` from `mods/`, and rename the data folder
  `mods/obut_markercolors/` to `mods/olvrbu_markercolors/` so players keep their chosen colors
  (`marker_colors.json`).
- No gameplay changes.

## 1.0.0 (2026-10-22)

First release.

- Each player can color other players' markers on the world map and the compass. Only you see the
  colors you choose.
- Right-click another player's marker on the map and pick a color (the same palette as placeable map
  markers: white, purple, orange, yellow, green, blue) or "Default".
- Command for all players: `/markercolor <player> <color>` (palette name or `#RRGGBB`),
  `/markercolor <player> reset`.
- Chosen colors are saved in `marker_colors.json` in the plugin's data folder and survive restarts.
