"""Collect the native chat autocomplete state for one command prefix.

For a typed prefix the client shows the inline completion (the gray suffix of the
first suggestion).  Pressing Tab then completes/cycles through the whole
suggestion list, one suggestion per press, so the reachable candidates can be read
back from the pixel width of the input line at every step (measured against the
client's own font metrics - this machine has no OCR).
"""

from __future__ import annotations

import json
import statistics
import sys
import time
from pathlib import Path

from PIL import Image

ART = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ART / "scripts"))
sys.path.insert(0, str(ART / "tools"))
import drive_ui as D  # noqa: E402
from mcfont import Font  # noqa: E402

SCALE = 4


def ink_box(frame: Path, y0: int = 2100, y1: int = 2156, x0: int = 12, x1: int = 3400, offset: int = 40):
    im = Image.open(frame).convert("RGB")
    px = im.load()
    cols = []
    for y in range(y0, y1):
        vals = [min(px[x, y]) for x in range(x0, x1)]
        med = statistics.median(vals)
        cols += [x for x, v in enumerate(vals, start=x0) if v > med + offset]
    if not cols:
        return None
    return [min(cols), max(cols), max(cols) - min(cols) + 1]


def main() -> None:
    tag, typed = sys.argv[1], sys.argv[2]
    candidates = sys.argv[3].split(",") if len(sys.argv) > 3 else []
    steps = int(sys.argv[4]) if len(sys.argv) > 4 else 6
    font = Font()

    D.ensure_ingame()
    D.press("F3+d")                      # empty chat history
    time.sleep(1.2)
    D.focus()
    D.press("t")
    time.sleep(1.5)
    D.run(["xdotool", "type", "--delay", "60", "--clearmodifiers", "--", typed])
    time.sleep(2.5)

    out = {"typed": typed, "steps": []}
    for step in range(steps):
        frame = D.shot(f"{tag}-step{step}")
        box = ink_box(frame)
        entry = {"step": step, "frame": str(frame.relative_to(ART)), "ink": box}
        if box and candidates:
            entry["matches"] = [[c, w] for c, w, _ in font.match(box[2], candidates, SCALE, tol=6)]
        out["steps"].append(entry)
        print(json.dumps(entry))
        D.press("Tab")
        time.sleep(1.6)
    D.press("Escape")
    time.sleep(0.6)
    (ART / "evidence" / f"{tag}-autocomplete.json").write_text(json.dumps(out, indent=2) + "\n")


if __name__ == "__main__":
    main()
