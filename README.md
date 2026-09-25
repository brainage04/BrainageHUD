# BrainageHUD

## Overview
BrainageHUD is a client-side Fabric and NeoForge mod that adds useful heads-up display (HUD) elements to Minecraft, along with waypoints and enchantment tools.

## HUD elements
- **Position HUD**: block position, position within the chunk, the C and E counters, the direction you face (including +/- X/Z), rotation, light levels and biome. Rotation can include the unwrapped "true" yaw, or show only while you hold an axe, a hoe or a Hypixel SkyBlock farming tool.
- **Network HUD**: ping and TPS, each of which can be turned off, optionally coloured from green (good) to red (bad). TPS is measured from the game time the server reports each second.
- **Performance HUD**: FPS, RAM usage, CPU usage, GPU usage and GPU frame time.
- **Date/Time HUD**: date, time (12 or 24 hour) and timezone.
- **Motion HUD**: your speed on each axis and your horizontal speed in blocks per second, optionally also in blocks per tick. It measures how far you actually moved, so it stays accurate when you are blocked or riding something.
- **Entity HUD**: how many entities are loaded, grouped into creatures, water creatures, ambient mobs, monsters and others.
- **Projectile HUD**: how many arrows, snowballs, eggs, ender pearls and wind charges you carry, optionally per slot.
- **Food HUD**: how much of each food you carry, optionally per slot.
- **Reach HUD**: the name, distance and coordinates of the block or entity you are looking at.
- **Toggle Sprint HUD**: whether you are walking, sneaking or sprinting, and whether that is vanilla or toggled. It can also show the game's toggle setting and the key state, for debugging.
- **Armour Info HUD**: durability, names and icons of your armour and the items in your hands.
- **Keystrokes HUD**: WASD, Space and the mouse buttons as they are pressed, and your clicks per second.
- **Enchant Info HUD**: the held item's enchantments and the enchantments it could still get (see [Enchant Info](#enchant-info)).
- **Status Effect HUD**: your active status effects with their icons, levels and how long each has left.
- **Fishing HUD**: while your bobber is out, whether it is in open water (which decides whether treasure can be caught) and the chance of catching a fish, treasure or junk.

Motion, Entity and Fishing are off by default; turn them on in the config editor. Every element can be configured to show as little or as much as you like.

HUD elements take the vanilla HUD into account: elements anchored to the top right move down when the vanilla status effect icons are showing, so the two don't overlap. The vanilla icons can be turned off with Show Vanilla Status Effects in HudRendererLib's config editor; top-right elements then stay where they are.

## Waypoints
Waypoints are saved per world (singleplayer save or server address) in `config/brainagehud/waypoints.json`. Each visible waypoint in your current dimension is drawn in the world, in its own colour:

- A beacon beam through the whole height of the dimension, which widens with distance so it can be found from far away.
- A spinning gem hovering above the spot, with a pulse spreading over the ground below it when you are near.
- A label with the name and distance, readable through walls. It keeps its size on screen however far away the waypoint is. Beyond 16 blocks, only the waypoint you look towards shows its name; the others show their distance.

Every dimension also has a built-in "World Centre" waypoint at 0, 63, 0, white by default. It is listed first on the Manage Waypoints screen, where it can be hidden or shown but not edited or deleted. Its colour is World Centre Colour in the config.

Each part can be turned off, and the label resized, under Waypoint Config in the config editor.

Waypoint names can be up to 64 characters and can't contain formatting codes, quotes or control characters.

## Enchant Info
The Enchant Info HUD shows the held item's name, its enchantments (with the maximum level after any enchantment below it), then a "Missing:" list of the enchantments it could still get. Enchantments that cannot be combined share one line, e.g. `Fortune III / Silk Touch`. Blacklisted enchantments are never listed as missing; by default the blacklist holds the curses and the enchantments that are usually a worse choice (such as Blast Protection, Smite and Knockback). Each part can be turned off under Enchant Info HUD in the config editor.

Tooltip lines of enchantments at their maximum level are shown in bold (Enchant Info > Highlight Max Level Enchants).

## Commands
- `/waypoints add <name> [<x> <y> <z>]`, `/waypoints remove <name>` and `/waypoints list` add, remove and list waypoints; `/waypoints` opens the Manage Waypoints screen. Quote names that contain spaces. (Vanilla's `/waypoint` is a server command for the locator bar, so this one is plural.)
- `/getenchants [<item>]` lists every enchantment the held item (or the given item) can get, grouping enchantments that conflict with each other.
- `/getenchantinfo <enchantment>` shows an enchantment's ID, maximum level, conflicts and the items it applies to. It accepts an ID (`minecraft:fortune` or `fortune`) or a name, ignoring case; a partial name lists every match.
- `/blacklistedenchants add <enchantment>`, `/blacklistedenchants remove <enchantment>` and `/blacklistedenchants query` edit and show the Enchant Info blacklist, which is also editable in the config editor.
- `/fullbright <amount>` sets the ambient light from -1 to 1; 0 turns it off.
- `/brainagehudconfig` opens the config editor.

## Controls
| Key | Default | Action |
|---|---|---|
| Open Config | Numpad Minus | Opens the BrainageHUD config editor. |
| Open Element Editor | Numpad Plus | Opens the HUD element editor. |
| Create Waypoint | B | Creates a waypoint where you stand, named "Waypoint 1", "Waypoint 2", and so on. |
| Manage Waypoints | U | Opens a screen to add, edit, hide/show and delete waypoints. |
| Inventory Stats | Unbound | Lists every occupied inventory slot in chat with its item, count and item data (damage, enchantments, custom name and so on). |

The element editor key and HudRendererLib's own config key (Numpad Enter) are in the HudRendererLib category of the controls screen; the rest are in the BrainageHUD category.

## Configuration and element editor
The config editor holds every element's settings. Each element can override the global text colour, text shadows, backdrop opacity, padding and maximum width in its Style Overrides.

The global settings are in HudRendererLib's config editor (Numpad Enter). Screen Margin keeps elements from being moved off-screen or too close to its edge.

In the element editor, drag an element with the mouse or nudge it with the arrow keys (Shift ×10, Ctrl ×5), and press Space to cycle its alignment. Save & Close keeps the changes; Undo & Close or Escape reverts them.

## Installation
Install the BrainageHUD JAR for your loader (Fabric or NeoForge) and its requirements into the client's `mods` folder. BrainageHUD is client-only and isn't needed on servers.

## Requirements
- Minecraft 26.2.
- [HudRendererLib](https://modrinth.com/mod/hudrendererlib) and [Cloth Config](https://modrinth.com/mod/cloth-config), for your loader.
- Fabric only: [Fabric API](https://modrinth.com/mod/fabric-api).
- Recommended on Fabric: [Mod Menu](https://modrinth.com/mod/modmenu), for a config button in the mods list.

## Links and credits
- Source and issues: [github.com/brainage04/BrainageHUD](https://github.com/brainage04/BrainageHUD).
- Sister mod for Minecraft 1.8.9 Forge: [Toggle Sprint](https://github.com/brainage04/ToggleSprint).
- The ping measurement is based on [Better Ping Display](https://github.com/vladmarica/better-ping-display-fabric) by vladmarica.
- Licensed under the MIT licence.

## Upgrade notes
- **From the Fabric-only release:** install exactly one BrainageHUD JAR, for Fabric or NeoForge; remove the old JAR before switching loaders. The mod ID is still `brainagehud`, so `config/brainagehud.json` carries over.
- **Enchant Info:** the Enchant Info HUD and the enchant commands replace the separate GetEnchantInfo mod; see [docs/getenchantinfo-migration.md](docs/getenchantinfo-migration.md).
