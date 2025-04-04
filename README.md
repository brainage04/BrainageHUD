# About
BrainageHUD is a client-side only mod for Fabric 1.21.4 that adds heads-up display (HUD) elements to Minecraft.

Here is a comprehensive list of the HUD elements added by BrainageHUD:
- ArmourInfoHUD - Displays information (durability, name, item icons) about armour and main/off hand
- KeystrokesHUD - Displays information (pressed/released, clicks per second) about important keys (WASD, space, mouse buttons)
- DateTimeHUD - Displays date, 12/24 hr time, timezone
- NetworkHUD - Displays ping, TPS
- PerformanceHUD - Displays FPS, RAM/GPU/CPU usage
- PositionHUD - Displays position, C/E counters, cardinal direction (including +/- X/Z facing information)
- ReachHUD - Displays how far away the targeted block/entity is and the name of said targeted block/entity
- ToggleSprintHUD - Displays the current movement state of the player (walking/sneaking/sprinting, vanilla/toggled)

All of the HUD elements are customisable and can be configured to display as little or as much information as you would like!

This mod also has global and per-element:
- Text shadows
- Text colour
- Backdrop opacity
- Element padding

The config editor can be opened with Numpad Enter.
This is where you can change all of the global and per-element settings.

The element editor can be opened with Numpad Plus.
This is where you can move the elements around the screen with the keyboard/mouse and change their alignments.
The "Screen Margin" config option will prevent you from moving the elements off-screen or too close to the edge of the screen, helping to ensure a more consistent layout.

This mod has been built with the vanilla HUD in mind. For example:
- HUD elements with the "Top Right" alignment will be shifted down a bit when the player has potion effects, so that the potion effect display and HUD elements do not overlap.

# Todo
refactor custom hud elements using proper HudRenderer utility methods like getPosX/Y
overhaul config with proper overrides
add waypoints
reachhud: add option to only update reachhud on left click (for pvp)
performancehud: get GPU to work with Minecraft's existing GPU profiler
keystrokeshud: smooth color transitions for key press/release events