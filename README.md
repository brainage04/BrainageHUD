# About
BrainageHUD is a client-side mod for the Fabric and NeoForge modloaders that adds useful heads-up display (HUD) elements to Minecraft.

Here is a comprehensive list of the HUD elements added by BrainageHUD:
- Date Time - Displays date, 12/24 hr time, timezone
- Network - Displays ping, TPS, optionally coloured from green (good) to red (bad)
- Performance - Displays FPS, RAM/GPU/CPU usage
- Position - Displays block/chunk positions, C/E counters, cardinal directions (including +/- X/Z facing information), rotation (optionally including the unwrapped "true" yaw, or only while holding an axe, a hoe or a Hypixel SkyBlock farming tool), light levels, biome
- Motion - Displays your velocity on each axis and your horizontal speed, in blocks per second (and optionally per tick)
- Entities - Displays how many entities are loaded, grouped into creatures, water creatures, ambient mobs, monsters and others
- Projectiles - Displays how many arrows, snowballs, eggs, ender pearls and wind charges you carry, optionally per slot
- Food - Displays how much of each food you carry, optionally per slot
- Reach - Displays name/distance/coordinates of targeted block/entity
- Toggle Sprint - Displays the current movement state of the player (walking/sneaking/sprinting, vanilla/toggled), optionally with the toggle setting and key state for debugging
- Armour Info - Displays information (durability, name, item icons) about armour and main/off hand
- Keystrokes - Displays information (pressed/released, clicks per second) about important keys (WASD, space, mouse buttons)
- Enchant Info - Displays the held item's enchantments, their maximum levels, and the enchantments it could still get
- Fishing - While your bobber is out, displays whether it is in open water, which decides whether treasure can be caught

Motion, Entities, Projectiles and Food are off by default; turn them on in the config editor.

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

# Waypoints
Waypoints are saved per world (singleplayer save or server address) in `config/brainagehud/waypoints.json`. Each visible waypoint in your current dimension is drawn in the world, in its own colour:

- A beacon beam through the whole height of the dimension, from the bottom of the world to the top, which widens with distance so it can be found from far away.
- A spinning gem hovering above the spot, with a pulse spreading over the ground below it when you are near.
- A label with the name and distance, readable through walls. It keeps its size on screen however far away the waypoint is. Beyond 16 blocks, only the waypoint you look towards shows its name; the others show their distance.

Every dimension also has a built-in "World Centre" waypoint at 0, 63, 0, white by default. It is listed first on the Manage Waypoints screen, where it can be hidden or shown but not edited or deleted; its colour is World Centre Colour in the config.

Each part can be turned off, and the label resized, under Waypoint Config in the BrainageHUD config.

- Create Waypoint (default B) creates a waypoint where you stand, named "Waypoint 1", "Waypoint 2", and so on.
- Manage Waypoints (default U) opens a screen to add, edit, hide/show and delete them.
- `/waypoints add <name> [<x> <y> <z>]`, `/waypoints remove <name>`, `/waypoints list`, and `/waypoints` to open the screen. Quote names that contain spaces. (Vanilla's `/waypoint` is a server command for the locator bar, so this one is plural.)

Both keys are in the BrainageHUD category of the controls screen.

# Other keys
- Inventory Stats (unbound by default) lists every occupied inventory slot in chat with its item, count and item data (damage, enchantments, custom name and so on).

# Other commands
- `/fullbright <amount>` - Sets the ambient light from -1 to 1; 0 turns it off.
- `/getenchantinfo <enchantment>` - Shows an enchantment's maximum level, incompatible enchantments and applicable items. Accepts an ID or a (partial) name.
- `/getenchants [<item>]` - Lists the enchantments the held (or given) item can get.
- `/blacklistedenchants add|remove <enchantment>` and `/blacklistedenchants query` - Manage the enchantments Enchant Info never lists as missing.

# Dependencies
This mod requires [HudRendererLib](https://github.com/brainage04/HudRendererLib/releases) and [Cloth Config](https://modrinth.com/mod/cloth-config/versions) on both loaders. Fabric installations also require [Fabric API](https://modrinth.com/mod/fabric-api/versions).

[Mod Menu](https://modrinth.com/mod/modmenu/versions) is also recommended.

## Migrating from the Fabric-only release

Install exactly one matching BrainageHUD JAR: Fabric or NeoForge. Remove the old BrainageHUD JAR before switching loaders; do not place both variants in the same client `mods` directory. BrainageHUD remains client-only, so install it and its loader-specific dependencies on the client rather than a dedicated server. Fabric requires Fabric API, HudRendererLib (Fabric), and Cloth Config (Fabric); NeoForge requires HudRendererLib (NeoForge) and Cloth Config (NeoForge). The stable mod ID remains `brainagehud`, so the existing `config/brainagehud.json` configuration path is preserved. Running the root `./gradlew build` emits both loader artifacts under `build/libs`.
