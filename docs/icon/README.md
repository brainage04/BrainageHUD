# BrainageHUD icon

## What this is

The mod's icon: `icon.png` — 1024x1024 RGBA PNG, 13 053 bytes,
sha256 `4ba8579cccc53adcbe21f77ba4ce9f7d4b49ada60a88cb44a261dee18bdc276e`.

It is a real in-game screenshot: the 600x600 capture crop `provenance/capture-crop.png`
(GUI scale 4), reduced to its native 150x150 GUI pixels, enlarged 6x with NEAREST to 900x900
and centred on a transparent 1024x1024 canvas (62 px margin). No interpolation, no
compositing, no shader pack and no resource pack. The mod ships 128x128 LANCZOS reductions of
`icon.png` at `common/src/main/resources/assets/brainagehud/icon.png` and
`fabric/src/gametest/resources/assets/brainagehud/icon.png`.

## How it was made

**Method: real Minecraft capture** (not a Blender render, not generated pixel art).

Client and scene:

| | |
|---|---|
| Client | Minecraft 26.2, Fabric Loader 0.19.3, Fabric API 0.156.0+26.2 |
| Java | OpenJDK 25.0.4.1+1, `-Xmx4G`, `-Dfabric.addMods`, `--version 26.2`, `--username IconCapture` |
| Mods loaded | hudrendererlib 1.0.7, brainagehud 1.0.3, brainagelib 1.0.1, cloth-config-fabric 26.2.155, simpletpa 1.2.0, simplehomes 1.0.0, spawncommands 1.0.0 |
| Renderer | software OpenGL, Mesa llvmpipe (LLVM 21.1.8) on a dedicated Xvfb display `:182`, 3840x2160x24; the primary desktop was never used |
| Audio | own PulseAudio null sink `round3_ui_mcscreens` (`soundDevice=Round3UIMCScreens`) |
| Shader pack | **none** (vanilla rendering) |
| Resource pack | **none** |
| World | `UI Void` — flat generator with `layers=[]`, biome `minecraft:the_void`, `generate_structures=0`, seed `20260910`: pure void, no terrain |
| Camera | `/tp @s 0.5 -60 0.5 0 0` → x=0.5 y=-60 z=0.5, yaw 0°, pitch 0° |
| Scene state | `/gamemode spectator`, `/time set noon`, `/weather clear`, `/tick freeze` (all four verified through the client's own chat feedback) |
| Screenshot conditions | GUI scale 4, renderDistance 2, `renderClouds=false`, no cloud layer in any frame, HUD visible (F1 not pressed); the frame is the game's own native F2 PNG, no post-processing |

The subject is HudRendererLib 1.0.7's **HUD Element Editor**, opened with the mod's own
keybind (`keypad add`). The pointer was moved to (160, 90) and left-clicked, which selects
and highlights the BrainageHUD **Position** HUD element (TOP_LEFT, x=5 y=5) — the
translucent white selection overlay is drawn over the element rectangle and the other HUD
elements stay visible.

The capture crop `provenance/capture-crop.png` (600x600 RGB, 11 318 bytes, sha256
`789acd60d5aaf0f890051e51c1cea9be9d9a792fdd34a7144290a1b40319f591`; until 2026-10-02 it was
shipped as `icon.png` itself, moved here unchanged) is the exact integer crop
`(0, 0, 600, 600)` of that F2 frame. The selection rectangle measured in the frame is
`(20, 20)-(471, 395)`, so the crop keeps it whole with margins left 20, top 20, right 129,
bottom 205 px.

**Native-scale enlargement (2026-10-02).** The icon rule is square, a power of two, 512 or
1024 px; the owner asked for BrainageHUD to be scaled up to 1024. The crop is pixel art: 27
colours on GUI scale 4's 4x4 grid (aligned with the crop). 21 196 of its 22 500 blocks are a
single colour; the other 1 304 lie on faint diagonal seams of the sky gradient, where the
colours differ by at most 1 level per channel. `native_scale.py` takes each block's majority
colour (72 blocks have no strict majority and go to the colour that dominates the surrounding
3x3 blocks) to recover the native 150x150 image, enlarges it by the largest integer factor that
fits 1024 (6x, 900x900) with NEAREST, and centres it on a transparent 1024x1024 canvas. A
LANCZOS resize was rejected because it blurs every GUI pixel edge.

## Provenance files

| Path | What it is |
|---|---|
| `manifest.json` | The round-3 delivery record for this icon: label, method, source, measured geometry, notes |
| `capture-crop.png` | **The 600x600 capture crop** (formerly `icon.png`, byte-identical) — the only surviving copy of this crop |
| `native_scale.py` | **The script that writes `icon.png`** from `capture-crop.png` (native 150x150 recovery, 6x NEAREST, centred on 1024x1024) and the two shipped 128x128 copies |
| `evidence/crop-report.json` | The measured highlight rectangle and the crop box used by `scripts/make_crops.py` |
| `evidence/scene.json` | Full scene provenance: display, audio sink, client, mod list, options, world generation, every verified scene command, the BrainageHUD shot description |
| `evidence/SHA256SUMS.txt` | Checksums of the seven captures-ui deliverables, including this capture crop |
| `evidence/client-log-chat-captures.log` | The client log of the capture session (scene commands, world name, saved screenshots) |
| `scripts/make_crops.py` | **The script that produced the capture crop** (function `brainagehud_crop()`) |
| `scripts/drive_ui.py` | The verified X11 driver used for every key press, command and screenshot |
| `scripts/capture_final.py` | The capture pass driver (scene verification, F2 capture, font-metric read-back) |
| `scripts/collect_autocomplete.py` | Input-line ink measurement helper |
| `scripts/read_chat.py`, `scripts/probe_chat.py` | Chat-panel detection helpers |
| `tools/mcfont.py` | The client's own bitmap-font advance table, used to identify rendered command text |
| `tools/nbt.py` | Minimal NBT read/write used to build the void world |
| `tools/setup.py` | Builds the capture runtime: options, HUD configs, void world, argfile, launcher |
| `launch-ui.sh`, `client-ui.args` | The exact launcher and Java argfile of the session |
| `blockers.json`, `cleanup.json` | Known limitations of the session, and the resource-teardown record |

Excluded on purpose: the game directory (`runtime/`, ~18 MB), session logs, `__pycache__`,
and the per-mod chat-autocomplete evidence files belonging to SimpleTPA, SimpleHomes and
SpawnCommands (they live in those mods' own `docs/icon/provenance/`).

## How to regenerate

The session ran with working directory `<round3>/captures-ui`. Restore `provenance/` to
that name, then:

```sh
cd captures-ui
python3 tools/setup.py                                             # writes runtime/, client-ui.args, launch-ui.sh
Xvfb :182 -screen 0 3840x2160x24 -nolisten tcp &                   # the session's own display
pactl load-module module-null-sink sink_name=round3_ui_mcscreens \
      sink_properties=device.description=Round3UIMCScreens
sh launch-ui.sh                                                    # Fabric MC 26.2 client, mods from client-ui.args
python3 scripts/drive_ui.py locate                                 # confirms the Minecraft window
# open the HUD Element Editor with the "keypad add" keybind, then:
python3 scripts/drive_ui.py move 160 90
python3 scripts/drive_ui.py click 160 90                           # selects + highlights the Position element
python3 scripts/drive_ui.py shot hud-editor-selected                # F2 -> renders/frames/hud-editor-selected.png
python3 scripts/make_crops.py                                      # writes renders/brainagehud-position-selected-600.png
```

`renders/brainagehud-position-selected-600.png` is `capture-crop.png`. Then write `icon.png` and
the shipped copies (Pillow 12.3.0, from `docs/icon/provenance`):

```sh
python3 native_scale.py    # prints: native (150, 150), 72 tied blocks, x6 -> (900, 900) centred on 1024x1024
```

Prerequisites not shipped here: the Minecraft 26.2 client assets and Fabric Loader, the
mod jars named in `client-ui.args`, and the world save (the void world is rebuilt by
`tools/setup.py` from a game-written save; see Notes).

## Notes

* **The source frame is not preserved.** `scripts/make_crops.py` crops
  `renders/frames/hud-editor-selected.png`, and that directory is empty in the round-3
  tree — the full-resolution F2 frames of the captures-ui session were deleted before the
  later capture pass. The capture crop (`capture-crop.png`, shipped as the icon until
  2026-10-02) is intact and is the only surviving copy of that crop; re-running the recipe
  above is the way to obtain the frame again.
* **This is a recapture, not a crop of an earlier closeup.** The previously approved
  closeup was 590x480, so a 600x600 crop of it was geometrically impossible without
  inventing pixels. The same scene was therefore recaptured: same editor, same element,
  same GUI scale 4 (the measured selection rectangle is 451x375 px in both sessions). The
  only visible difference is the backdrop — this round's void world instead of the earlier
  dungeon world.
* `tools/setup.py` builds the void world from `captures-fortnite/runtime/saves/Fortnite
  Void Corner` with `level.dat` rewritten by `tools/nbt.py` and `world_gen_settings.dat`
  copied byte-identical; that save is not part of this provenance.
* The client never renders a chat suggestion *list* in 26.2 (it renders an inline
  completion in the input line); that is why the BrainageHUD icon, not a chat shot, is the
  BrainageHUD icon. See `blockers.json`.

## Working-tree note

The round-3 working tree that produced this icon was cleaned up after integration. Every file needed to regenerate the icon was copied into `provenance/`; the copies live under `provenance/from-round3/` when they came from the working tree. Any remaining `round3/...` mention records where something came from, not a path that still exists.
