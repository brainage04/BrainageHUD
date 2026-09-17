"""Build the captures-ui runtime: options, configs, void world, argfile, launcher.

Everything is derived from artefacts that are already proven in this round
(captures-fortnite/client-fortnite-4k.args reached the title screen the same way,
captures-fortnite/runtime/saves/Fortnite Void Corner is the game-written pure-void
world, captures/runtime/config holds the HUD element configs used by the existing
BrainageHUD shots).  Only the values that must differ are changed.
"""

from __future__ import annotations

import shlex
import shutil
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import nbt

ROUND = Path("/home/thomas/01_TM/Coding/Websites/brainage04.github.io/.local-icon-variants/round3")
ART = ROUND / "captures-ui"
FORTNITE = ROUND / "captures-fortnite"
DUNGEON = ROUND / "captures-dungeon"
CAPTURES = ROUND / "captures"

MODS = [
    "/home/thomas/.gradle/caches/modules-2/files-2.1/net.fabricmc.fabric-api/fabric-api/"
    "0.156.0+26.2/d96e0d9ef8ea3604fac4ca7495d7c6148f3ac816/fabric-api-0.156.0+26.2.jar",
    str(Path.home() / "01_TM/Coding/Minecraft/HudRendererLib/build/libs/hudrendererlib-1.0.7.jar"),
    str(Path.home() / "01_TM/Coding/Minecraft/BrainageHUD/build/libs/brainagehud-1.0.3.jar"),
    str(Path.home() / "01_TM/Coding/Minecraft/SimpleTPA/build/libs/simpletpa-1.2.0.jar"),
    str(Path.home() / "01_TM/Coding/Minecraft/SimpleHomes/build/libs/simplehomes-1.0.0.jar"),
    str(Path.home() / "01_TM/Coding/Minecraft/SpawnCommands/build/libs/spawncommands-1.0.0.jar"),
    "/home/thomas/.gradle/caches/modules-2/files-2.1/me.shedaniel.cloth/cloth-config-fabric/"
    "26.2.155/babcf16dbd15e09d326e21ffe85ab3f7d843ef9e/cloth-config-fabric-26.2.155.jar",
    str(Path.home() / "01_TM/Coding/Minecraft/BrainageLib/build/libs/brainagelib-1.0.1.jar"),
]

OPTION_PATCHES = {
    "guiScale": "guiScale:4",
    "renderClouds": 'renderClouds:"false"',
    "cloudRange": "cloudRange:64",
    "soundDevice": 'soundDevice:"Round3UIMCScreens"',
    "pauseOnLostFocus": "pauseOnLostFocus:false",
    "autoSuggestions": "autoSuggestions:true",
    "chatVisibility": "chatVisibility:0",
    "chatOpacity": "chatOpacity:1.0",
    "textBackgroundOpacity": "textBackgroundOpacity:0.5",
    "backgroundForChatOnly": "backgroundForChatOnly:true",
    "chatScale": "chatScale:1.0",
    "chatWidth": "chatWidth:1.0",
    "chatHeightFocused": "chatHeightFocused:1.0",
    "chatHeightUnfocused": "chatHeightUnfocused:0.4375",
    "narrator": "narrator:0",
    "soundCategory_master": "soundCategory_master:0.0",
    "saveChatDrafts": "saveChatDrafts:false",
    "fullscreen": "fullscreen:false",
    "overrideWidth": "overrideWidth:0",
    "overrideHeight": "overrideHeight:0",
    "tutorialStep": "tutorialStep:none",
    "skipMultiplayerWarning": "skipMultiplayerWarning:true",
}


def build_options() -> None:
    src = (FORTNITE / "runtime/options.txt").read_text().splitlines()
    out, seen = [], set()
    for line in src:
        key = line.split(":", 1)[0]
        if key in OPTION_PATCHES:
            out.append(OPTION_PATCHES[key])
            seen.add(key)
        else:
            out.append(line)
    missing = [k for k in OPTION_PATCHES if k not in seen]
    if missing:
        raise SystemExit(f"options.txt patch keys not present: {missing}")
    (ART / "runtime/options.txt").write_text("\n".join(out) + "\n")


def build_configs() -> None:
    for name in ("brainagehud.json", "hudrendererlib.json"):
        shutil.copy2(CAPTURES / "runtime/config" / name, ART / "runtime/config" / name)


def build_world() -> None:
    src = FORTNITE / "runtime/saves/Fortnite Void Corner"
    dst = ART / "runtime/saves/UI Void"
    (dst / "data/minecraft").mkdir(parents=True, exist_ok=True)
    shutil.copy2(src / "data/minecraft/world_gen_settings.dat", dst / "data/minecraft/world_gen_settings.dat")

    name, root = nbt.read_file(src / "level.dat")
    data = root.value["Data"]
    packs = nbt.get(root, "Data", "DataPacks", "Enabled")
    packs.value = [item for item in packs.value if item.value in ("vanilla", "fabric-convention-tags-v2")]
    data.value["LevelName"].value = "UI Void"
    data.value["LastPlayed"].value = 0
    nbt.write_file(dst / "level.dat", name, root)


def build_args() -> None:
    tokens = shlex.split((FORTNITE / "client-fortnite-4k.args").read_text())
    for i, token in enumerate(tokens):
        if token.startswith("-Dfabric.addMods="):
            tokens[i] = "-Dfabric.addMods=" + ":".join(MODS)
        elif token == "--gameDir":
            tokens[i + 1] = str(ART / "runtime")
    if "--quickPlaySingleplayer" not in tokens:
        tokens += ["--quickPlaySingleplayer", "UI Void"]
    text = "".join(f"{shlex.quote(t)}\n" for t in tokens)
    (ART / "client-ui.args").write_text(text)


def build_launcher() -> None:
    fortnite_launch = (FORTNITE / "launch-fortnite.sh").read_text().splitlines()
    libpath = next(line for line in fortnite_launch if line.startswith("export LD_LIBRARY_PATH="))
    drivers = next(line for line in fortnite_launch if line.startswith("export LIBGL_DRIVERS_PATH="))
    script = f"""#!/bin/sh
# captures-ui launcher: Fabric MC 26.2 client on isolated display :182 with its own
# PulseAudio null sink (round3_ui_mcscreens).  Argfile client-ui.args is
# captures-fortnite/client-fortnite-4k.args with --gameDir repointed at captures-ui/runtime
# and the mod set swapped for SimpleTPA + SimpleHomes + SpawnCommands + BrainageHUD.
{libpath}
{drivers}
export DISPLAY=:182
export ALSOFT_CONF="{ART}/runtime/alsoft.conf"
exec /nix/store/b7hzgfqk2jfcbncp0vpabf73j0riqgq4-openjdk-25.0.4.1+1/bin/java "@{ART}/client-ui.args" "$@"
"""
    path = ART / "launch-ui.sh"
    path.write_text(script)
    path.chmod(0o755)


def main() -> None:
    for sub in ("runtime/config", "runtime/saves", "runtime/logs", "runtime/screenshots",
                "renders", "evidence", "scripts", "tools"):
        (ART / sub).mkdir(parents=True, exist_ok=True)
    (ART / "runtime/alsoft.conf").write_text(
        "[general]\ndrivers=pulse\n[pulse]\ndefault-sink=round3_ui_mcscreens\n"
    )
    build_options()
    build_configs()
    build_world()
    build_args()
    build_launcher()
    print("built", ART)


if __name__ == "__main__":
    main()
