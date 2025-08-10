# About
BrainageHUD is a client-side mod for the Fabric modloader that adds useful heads-up display (HUD) elements to Minecraft.

Here is a comprehensive list of the HUD elements added by BrainageHUD:
- Date Time - Displays date, 12/24 hr time, timezone
- Network - Displays ping, TPS
- Performance - Displays FPS, RAM/GPU/CPU usage
- Position - Displays block/chunk positions, C/E counters, cardinal directions (including +/- X/Z facing information)
- Reach - Displays name/distance/coordinates of targeted block/entity
- Toggle Sprint - Displays the current movement state of the player (walking/sneaking/sprinting, vanilla/toggled)
- Armour Info - Displays information (durability, name, item icons) about armour and main/off hand
- Keystrokes - Displays information (pressed/released, clicks per second) about important keys (WASD, space, mouse buttons)

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

# Todo
figure out how to do away with elementId field

add fishinghud: can treasure be caught, etc

refactor custom hud elements using proper HudRenderer utility methods like getPosX/Y

add waypoints

reachhud: add option to only update reachhud on left click (for pvp)description

performancehud: get GPU to work with Minecraft's existing GPU profiler

keystrokeshud: smooth color transitions for key press/release events