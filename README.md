# BrainageHUD

BrainageHUD is a client-side Fabric and NeoForge mod for Minecraft 26.3 that adds useful heads-up display (HUD) elements, along with waypoints and enchantment tools.

Motion and Entity are off by default; turn them on in the config editor. Every element can be configured to show as little or as much as you like.

## Features

### HUD elements

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

### Waypoints

Waypoints are saved per world (singleplayer save or server address, where `play.example.net` and `play.example.net:25565` are one server) in `config/brainagehud/waypoints.json`. Each visible waypoint in your current dimension is drawn in the world, in its own colour:

- A beacon beam through the whole height of the dimension, which widens with distance so it can be found from far away.
- A spinning gem hovering above the spot, with a pulse spreading over the ground below it when you are near.
- A label with the name and distance, readable through walls. It keeps its size on screen however far away the waypoint is. Beyond 16 blocks, only the waypoint you look towards shows its name; the others show their distance.

Every dimension also has a built-in "World Centre" waypoint at 0, 63, 0, white by default. It is listed first on the Manage Waypoints screen, where it can be hidden or shown but not edited or deleted. Its colour is World Centre Colour in the config.

Each part can be turned off, and the label resized, under Waypoint Config in the config editor. Waypoint names can be up to 64 characters and can't contain formatting codes, quotes or control characters.

### Enchant Info

The Enchant Info HUD shows the held item's name, its enchantments, then a "Missing:" list of the enchantments it could still get. Compact mode is on by default: all of the item's enchantments share one line with their levels (`Sharpness III, Unbreaking III`), and the missing enchantments that can all be added share another (`Missing: Fire Aspect, Looting, Mending`), without maximum levels, so a plain sword takes three lines instead of eight. Enchantments that cannot be combined are a choice of one: each such group gets a line of its own after the missing list, e.g. `Sharpness / Smite / Bane of Arthropods`. So `,` always separates enchantments you can all add, and `/` always separates the alternatives of one choice. With Compact Mode off (Enchant Info HUD > Compact Mode, or the Toggle Enchant Info Compact key, `K`) the full format is used: one line per enchantment, with the maximum level after any enchantment below it, and one line per missing choice. Lines wrap at the element's Max Width (see Configuration), 300 GUI pixels by default, only at those separators and never inside a name, and a wrapped line keeps its `,` or `/` at the end, so a line ending in `/` continues the same choice. At its default place the panel then ends before the Date/Time HUD in a 1920x1080 window at GUI scale 3. Blacklisted enchantments are never listed as missing; by default the blacklist holds the curses and the enchantments that are usually a worse choice (such as Blast Protection, Smite and Knockback). Each part can be turned off under Enchant Info HUD in the config editor, and the key switches compact mode without opening it.

Tooltip lines of enchantments at their maximum level are shown in bold (Enchant Info > Highlight Max Level Enchants).

On screens narrower than about 640 GUI pixels (Auto GUI scale at 1080p, or Force Unicode Font), the panel can still reach the clock: lower its Max Width in the HUD editor or config.

## Getting started

- Minecraft 26.3; Java 25 or later.
- Fabric Loader 0.19.5 or later with [Fabric API](https://modrinth.com/mod/fabric-api), or NeoForge 26.3.0.99 or later.
- [HudRendererLib](https://modrinth.com/mod/hudrendererlib) 1.1.1 or later and [Cloth Config](https://modrinth.com/mod/cloth-config) 26.3.159 or later, for your loader.
- Recommended on Fabric: [Mod Menu](https://modrinth.com/mod/modmenu), for a config button in the mods list.

Install the BrainageHUD JAR for your loader (Fabric or NeoForge) and its requirements into the client's `mods` folder. BrainageHUD is client-only and isn't needed on servers.

## Commands

- `/waypoints add <name> [<x> <y> <z>]`, `/waypoints remove <name>` and `/waypoints list` – add, remove and list waypoints; `/waypoints` opens the Manage Waypoints screen. Quote names that contain spaces. (Vanilla's `/waypoint` is a server command for the locator bar, so this one is plural.)
- `/getenchants [<item>]` – lists every enchantment the held item (or the given item) can get, grouping enchantments that conflict with each other.
- `/getenchantinfo <enchantment>` – shows an enchantment's ID, maximum level, conflicts and the items it applies to. It accepts an ID (`minecraft:fortune` or `fortune`) or a name, ignoring case; a partial name lists every match.
- `/blacklistedenchants add <enchantment>`, `/blacklistedenchants remove <enchantment>` and `/blacklistedenchants query` – edit and show the Enchant Info blacklist, which is also editable in the config editor.
- `/fullbright <amount>` – brightens every light level from 0 to 1 (1 lights caves and night-time terrain fully) or darkens it from 0 to -1 (-1 is black); 0 turns it off.
- `/brainagehudconfig` – opens the config editor.
- `Numpad Minus` (Open Config) – opens the BrainageHUD config editor.
- `Numpad Plus` (Open Element Editor) – opens the HUD element editor.
- `B` (Create Waypoint) – creates a waypoint where you stand, named "Waypoint 1", "Waypoint 2", and so on.
- `U` (Manage Waypoints) – opens a screen to add, edit, hide/show and delete waypoints.
- `K` (Toggle Enchant Info Compact) – switches the Enchant Info HUD between compact and full lines, and says in chat which one is on.
- `Inventory Stats` (Unbound) – lists every occupied inventory slot in chat with its item, count and item data (damage, enchantments, custom name and so on).

The element editor key and HudRendererLib's own config key (Numpad Enter) are in the HudRendererLib category of the controls screen; the rest are in the BrainageHUD category.

## Configuration

The config editor holds every element's settings. Each element can override the global text colour, text shadows, backdrop opacity, padding and maximum width in its Style Overrides.

Max Width is off by default for every element except the Enchant Info HUD, which wraps at 300 GUI pixels unless its own Max Width or HudRendererLib's global Max Element Width is set. Turned on (Style Overrides > Max Width > Enabled, then the width in GUI pixels, where 0 means no limit), it wraps the element's lines at that width: between words, or for the Enchant Info HUD between enchantments (its item name between words), never inside a word or name. A single word or name wider than the limit stays whole on its own line. The element keeps its alignment: a right-anchored element still ends at its right edge, with every line right-aligned. With Show Icons on, the Status Effect HUD wraps each effect's name and time beside its icon.

The global settings are in HudRendererLib's config editor (Numpad Enter). Screen Margin keeps elements from being moved off-screen or too close to its edge.

In the element editor, drag an element with the mouse or nudge it with the arrow keys (Shift ×10, Ctrl ×5), and press Space to cycle its alignment. A selected element that can wrap shows an orange bar on its free edge (the right edge, or the left one for right-anchored elements): drag it to set the element's Max Width, or press `[` and `]` to narrow or widen it by one pixel (Shift ×10, Ctrl ×5), and Backspace to reset it to the global Max Element Width or, without one, the element's default. Save & Close keeps the changes; Undo & Close or Escape reverts them.

While the Status Effect HUD is on, the game's own status effect icons in the top right are hidden (Hide Vanilla Status Effects, on by default in its settings). Whenever those icons are showing, elements anchored to the top right move down so the two don't overlap. Show Vanilla Status Effects in HudRendererLib's config editor turns the vanilla icons off everywhere.

## Compatibility

The Enchant Info HUD and the enchant commands replace the separate GetEnchantInfo mod; do not install both together.

Sister mod for Minecraft 1.8.9 Forge: [Toggle Sprint](https://github.com/brainage04/ToggleSprint).

The ping measurement is based on [Better Ping Display](https://github.com/vladmarica/better-ping-display-fabric) by vladmarica. Licensed under the MIT licence.

Source and issues: [github.com/brainage04/BrainageHUD](https://github.com/brainage04/BrainageHUD).

## Documentation

- [GetEnchantInfo port details](docs/getenchantinfo-migration.md) – command, HUD and config differences.
- [Release procedure](docs/RELEASE.md) – maintainer publishing instructions.
- [Development](docs/DEVELOPMENT.md) – build, test and headless recording commands.
- [Modrinth description](docs/MODRINTH.md) – project page text.
- [Icon](docs/icon/README.md) – artwork source and credits.
