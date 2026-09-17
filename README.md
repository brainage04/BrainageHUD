# About
BrainageHUD is a client-side mod for the Fabric and NeoForge modloaders that adds useful heads-up display (HUD) elements to Minecraft.

Here is a comprehensive list of the HUD elements added by BrainageHUD:
- Date Time - Displays date, 12/24 hr time, timezone
- Network - Displays ping, TPS
- Performance - Displays FPS, RAM/GPU/CPU usage
- Position - Displays block/chunk positions, C/E counters, cardinal directions (including +/- X/Z facing information)
- Reach - Displays name/distance/coordinates of targeted block/entity
- Toggle Sprint - Displays the current movement state of the player (walking/sneaking/sprinting, vanilla/toggled)
- Armour Info - Displays information (durability, name, item icons) about armour and main/off hand
- Keystrokes - Displays information (pressed/released, clicks per second) about important keys (WASD, space, mouse buttons)
- (WIP) Fishing - Displays information about fishing bobber while in water

All of the HUD elements are customisable and can be configured to display as little or as much information as you would like!

This mod also has global and per-element:
- Text shadows
- Text colour
- Backdrop opacity
- Element padding

The BrainageHUD config editor can be opened with Numpad Minus.
This is where you can change all of the per-element rendering settings.

The element editor can be opened with Numpad Plus.
This is where you can move the elements around the screen with the keyboard/mouse and change their alignments.
The "Screen Margin" config option will prevent you from moving the elements off-screen or too close to the edge of the screen, helping to ensure a more consistent layout.

The HudRendererLib config editor can be opened with Numpad Enter.
This is where you can change all of the global rendering settings.

This mod has been built with the vanilla HUD in mind. For example:
- HUD elements with the "Top Right" alignment will be shifted down a bit when the player has potion effects, so that the potion effect display and HUD elements do not overlap.

# Dependencies
This mod requires [HudRendererLib](https://github.com/brainage04/HudRendererLib/releases) and [Cloth Config](https://modrinth.com/mod/cloth-config/versions) on both loaders. Fabric installations also require [Fabric API](https://modrinth.com/mod/fabric-api/versions).

[Mod Menu](https://modrinth.com/mod/modmenu/versions) is also recommended.

## Migrating from the Fabric-only release

Install exactly one matching BrainageHUD JAR: Fabric or NeoForge. Remove the old BrainageHUD JAR before switching loaders; do not place both variants in the same client `mods` directory. BrainageHUD remains client-only, so install it and its loader-specific dependencies on the client rather than a dedicated server. Fabric requires Fabric API, HudRendererLib (Fabric), and Cloth Config (Fabric); NeoForge requires HudRendererLib (NeoForge) and Cloth Config (NeoForge). The stable mod ID remains `brainagehud`, so the existing `config/brainagehud.json` configuration path is preserved. Running the root `./gradlew build` emits both loader artifacts under `build/libs`.

# Todo
refactor custom hud elements using proper HudRenderer utility methods like getPosX/Y

fishinghud: can treasure be caught, etc

reachhud: add option to only update reach distance on left click (for pvp)

performancehud: get GPU to work with Minecraft's existing GPU profiler

keystrokeshud: smooth color transitions for key press/release events

add waypoints (can get these from https://github.com/brainage04/NPCAddons)
