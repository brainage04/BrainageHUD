"""Drive the isolated Minecraft client on display :182 for the captures-ui shots.

Every step is verified instead of assumed:
  * commands are proven by their own feedback line appearing in the client log,
  * the chat capture is proven by the screenshot itself (chat panel + suggestion
    text rows are detected in the pixels before the frame is accepted).

Subcommands:
  locate                     print the Minecraft window id / geometry
  key <xdotool-key>          press one key
  combo <mod> <key>          press a modified key (e.g. combo ctrl v)
  cmd <text>                 send one command, verified through the client log
  shot <tag>                 press F2 and copy the new screenshot into renders/frames
  capture <tag> <input> [--pre CMD]... [--raw]
                             clear chat, type <input> in the chat, screenshot it and
                             require a detected suggestion list in the image
  chat <text> [tag]          open the chat, paste <text>, screenshot, then Escape
  click <x> <y>              move the pointer and click
  move <x> <y>               move the pointer without clicking
  esc                        press Escape once
  tab [n]                    press Tab n times (native completion cycling)
"""

from __future__ import annotations

import json
import os
import re
import shutil
import subprocess
import sys
import time
from pathlib import Path

from PIL import Image

ART = Path(__file__).resolve().parent.parent
DISPLAY = os.environ.get("CAPTURE_DISPLAY", ":182")
GAMEDIR = Path(os.environ.get("CAPTURE_GAMEDIR", str(ART / "runtime")))
SHOTS = GAMEDIR / "screenshots"
FRAMES = ART / "renders" / "frames"
LOG = ART / "logs" / "latest.log"
EVIDENCE = ART / "evidence"

WIN_PATTERN = re.compile(r".*Minecraft.*")


def run(cmd: list[str], **kw) -> str:
    env = {**os.environ, "DISPLAY": DISPLAY}
    return subprocess.run(cmd, check=True, capture_output=True, text=True, env=env, **kw).stdout


def window_id() -> str:
    ids = [line.strip() for line in run(["xdotool", "search", "--name", "Minecraft"]).splitlines() if line.strip()]
    if not ids:
        raise SystemExit(f"no Minecraft window on {DISPLAY}")
    return ids[-1]


def focus() -> str:
    win = window_id()
    run(["xdotool", "windowfocus", "--sync", win])
    return win


def press(key: str) -> None:
    run(["xdotool", "key", "--clearmodifiers", key])


def set_clipboard(text: str) -> None:
    subprocess.run(
        ["xclip", "-selection", "clipboard", "-silent"],
        input=text, text=True, check=True,
        env={**os.environ, "DISPLAY": DISPLAY},
        stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, timeout=10,
    )


def log_mark() -> int:
    return LOG.stat().st_size if LOG.exists() else 0


def log_since(mark: int) -> str:
    with LOG.open("r", errors="replace") as handle:
        handle.seek(mark)
        return handle.read()


def wait_log(mark: int, pattern: str, timeout: float = 8.0) -> str | None:
    """Wait for `pattern` in the log appended after `mark`; return the match."""
    deadline = time.time() + timeout
    seen = ""
    while time.time() < deadline:
        seen = log_since(mark)
        hit = re.search(pattern, seen)
        if hit:
            return hit.group(0)
        time.sleep(0.4)
    return None


def newest_shot() -> Path | None:
    files = sorted(SHOTS.glob("*.png"), key=lambda p: p.stat().st_mtime)
    return files[-1] if files else None


def shot(tag: str, settle: float = 0.0) -> Path:
    if settle:
        time.sleep(settle)
    before = {p.name for p in SHOTS.glob("*.png")}
    press("F2")
    for _ in range(60):
        fresh = sorted({p.name for p in SHOTS.glob("*.png")} - before)
        if fresh:
            src = SHOTS / fresh[-1]
            last = -1
            for _ in range(40):
                now = src.stat().st_size
                if now > 1024 and now == last:
                    break
                last = now
                time.sleep(0.5)
            time.sleep(0.5)
            FRAMES.mkdir(parents=True, exist_ok=True)
            target = FRAMES / f"{tag}.png"
            for _ in range(5):
                shutil.copy2(src, target)
                if target.stat().st_size == src.stat().st_size and target.stat().st_size > 1024:
                    break
                time.sleep(0.5)
            return target
        time.sleep(0.4)
    raise SystemExit("screenshot never appeared")


def pause_menu_open(frame: Path) -> bool:
    """True when the pause menu overlay (flat dark grey backdrop) is on screen."""
    im = Image.open(frame).convert("RGB")
    px = im.load()
    w, h = im.size
    hits = 0
    for y in range(0, h, 7):
        for x in range(0, w, 7):
            p = px[x, y]
            if max(p) - min(p) <= 4 and 40 <= p[0] <= 60:
                hits += 1
    return hits > 8000            # pause menu covers ~20% of a 4K frame


def snap(target: Path) -> Path:
    """Press F2 and copy the game's newest screenshot to `target`."""
    before = {p.name for p in SHOTS.glob("*.png")}
    press("F2")
    for _ in range(60):
        fresh = sorted({p.name for p in SHOTS.glob("*.png")} - before)
        if fresh:
            src = SHOTS / fresh[-1]
            last = -1
            for _ in range(40):
                now = src.stat().st_size
                if now > 1024 and now == last:
                    break
                last = now
                time.sleep(0.5)
            target.parent.mkdir(parents=True, exist_ok=True)
            for _ in range(5):
                shutil.copy2(src, target)
                if target.stat().st_size == src.stat().st_size and target.stat().st_size > 1024:
                    return target
                time.sleep(0.5)
            return target
        time.sleep(0.4)
    raise SystemExit("screenshot never appeared")


def ensure_ingame(tries: int = 3) -> Path:
    """Make sure no screen owns the keyboard, judged from the pixels, not assumed."""
    target = FRAMES / "_state/state.png"
    for _ in range(tries):
        snap(target)
        if not pause_menu_open(target):
            return target
        print("pause menu detected - closing with Escape")
        press("Escape")
        time.sleep(1.5)
    return target


def open_chat(text: str) -> None:
    """Press t, paste `text`; no submission."""
    focus()
    set_clipboard(text)
    press("t")
    time.sleep(1.2)
    press("ctrl+v")
    time.sleep(1.6)


def analyse_chat(path: Path) -> dict:
    """Detect the chat panel and its text rows in a frame (bottom-left region)."""
    im = Image.open(path).convert("RGB")
    w, h = im.size
    px = im.load()
    rows = []
    for y in range(h // 2, h):
        n = sum(1 for x in range(0, int(w * 0.62), 3) if min(px[x, y]) >= 140)
        rows.append((y, n))
    bands, cur = [], None
    for y, n in rows:
        if n >= 4:
            cur = [y, y] if cur is None else [cur[0], y]
        else:
            if cur and cur[1] - cur[0] >= 3:
                bands.append(tuple(cur))
            cur = None
    if cur and cur[1] - cur[0] >= 3:
        bands.append(tuple(cur))
    # horizontal extent of the topmost and bottommost band
    def extent(band):
        xs = [x for y in range(band[0], band[1] + 1) for x in range(0, int(w * 0.62)) if min(px[x, y]) >= 140]
        return (min(xs), max(xs)) if xs else None
    info = {
        "size": [w, h],
        "bands": [list(b) for b in bands],
        "bandCount": len(bands),
        "extents": [extent(b) for b in bands],
    }
    return info


def send_command(text: str, expect: str, tries: int = 4, pause: float = 1.6) -> bool:
    for attempt in range(tries):
        focus()
        mark = log_mark()
        set_clipboard(text)
        press("t")
        time.sleep(1.0)
        press("ctrl+v")
        time.sleep(0.7)
        press("Return")
        hit = wait_log(mark, expect, timeout=6.0)
        if hit:
            time.sleep(pause)
            return True
        # the keystrokes landed in some other screen: toggle it with Escape and retry
        press("Escape")
        time.sleep(1.0)
    return False


def capture_chat(tag: str, text: str, pre: list[tuple[str, str]], expect_bands: int = 2) -> dict:
    for command, pattern in pre:
        if not send_command(command, pattern):
            raise SystemExit(f"pre-command failed: {command}")
    focus()
    press("F3+d")            # clear the chat history (vanilla debug clear-chat)
    time.sleep(1.0)
    result = None
    for attempt in range(1, 4):
        open_chat(text)
        mark = log_mark()
        frame = shot(tag)
        with LOG.open("r", errors="replace") as handle:
            handle.seek(mark)
            saved = re.search(r"Saved screenshot as (\S+)", handle.read())
        info = analyse_chat(frame)
        if info["bandCount"] >= expect_bands:
            result = {"frame": str(frame), "attempt": attempt, "chat": info,
                      "typed": text, "gameScreenshot": saved.group(1) if saved else None}
            break
        print(f"attempt {attempt}: no suggestion list detected ({info['bandCount']} bands) - retrying")
        press("Escape")
        time.sleep(1.2)
        press("F3+d")
        time.sleep(0.8)
    if result is None:
        raise SystemExit(f"could not capture a chat suggestion list for {text!r}")
    press("Escape")          # close the chat without submitting
    time.sleep(0.8)
    EVIDENCE.mkdir(parents=True, exist_ok=True)
    (EVIDENCE / f"{tag}.json").write_text(json.dumps(result, indent=2) + "\n")
    return result


def main() -> None:
    if len(sys.argv) < 2:
        raise SystemExit(__doc__)
    action, args = sys.argv[1], sys.argv[2:]
    if action == "locate":
        win = focus()
        print(f"window {win} on {DISPLAY}")
        print(run(["xdotool", "getwindowgeometry", win]).strip())
    elif action == "key":
        focus(); press(args[0])
    elif action == "combo":
        focus(); run(["xdotool", "key", "--clearmodifiers", f"{args[0]}+{args[1]}"])
    elif action == "cmd":
        ok = send_command(args[0], args[1] if len(args) > 1 else re.escape(args[0]))
        print("command verified" if ok else "COMMAND NOT VERIFIED")
        if not ok:
            raise SystemExit(1)
    elif action == "shot":
        focus(); print(shot(args[0], settle=float(args[1]) if len(args) > 1 else 0.0))
    elif action == "capture":
        tag, text = args[0], args[1]
        pre: list[tuple[str, str]] = []
        rest = args[2:]
        i = 0
        while i < len(rest):
            if rest[i] == "--pre":
                pre.append((rest[i + 1], rest[i + 2])); i += 3
            else:
                raise SystemExit(f"unexpected argument {rest[i]}")
        info = capture_chat(tag, text, pre)
        print(json.dumps(info, indent=2))
    elif action == "chat":
        focus()
        open_chat(args[0])
        if len(args) > 1:
            shot(args[1])
        press("Escape")
        time.sleep(0.6)
    elif action == "tab":
        focus()
        for _ in range(int(args[0]) if args else 1):
            press("Tab"); time.sleep(0.7)
    elif action == "move":
        run(["xdotool", "mousemove", "--sync", args[0], args[1]]); time.sleep(0.2)
    elif action == "click":
        run(["xdotool", "mousemove", "--sync", args[0], args[1]]); time.sleep(0.3)
        run(["xdotool", "click", "1"]); time.sleep(0.6)
    elif action == "esc":
        focus(); press("Escape"); time.sleep(0.6)
    elif action == "last":
        print(newest_shot())
    else:
        raise SystemExit(__doc__)
    print("ok:", action, *args)


if __name__ == "__main__":
    main()
