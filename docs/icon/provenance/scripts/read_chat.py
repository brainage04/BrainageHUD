"""Read the chat text rows of a captured frame using the client's own font.

Prints, for every text band found in the lower part of the frame: the band's y
range, the measured ink box, and which candidate strings (typed input, ghost
completion, suggestion entries) match that ink width.
"""

from __future__ import annotations

import statistics
import sys
from pathlib import Path

from PIL import Image

ART = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ART / "tools"))
from mcfont import Font  # noqa: E402

SCALE = 4


def bands(frame: Path, y0: int = 1800, y1: int = 2160, x1: int = 2400, offset: int = 42) -> list[dict]:
    im = Image.open(frame).convert("RGB")
    px = im.load()
    w, h = im.size
    y1 = min(y1, h)
    rows = {}
    for y in range(y0, y1):
        vals = [min(px[x, y]) for x in range(0, min(x1, w))]
        med = statistics.median(vals)
        cols = [x for x, v in enumerate(vals) if v > med + offset]
        if cols:
            rows[y] = (min(cols), max(cols), len(cols))
    out, cur = [], None
    for y in sorted(rows):
        if cur and y - cur["y1"] <= 6:
            cur["y1"] = y
            cur["x0"] = min(cur["x0"], rows[y][0])
            cur["x1"] = max(cur["x1"], rows[y][1])
        else:
            if cur:
                out.append(cur)
            cur = {"y0": y, "y1": y, "x0": rows[y][0], "x1": rows[y][1]}
    if cur:
        out.append(cur)
    return [b for b in out if b["y1"] - b["y0"] >= 6]


def main() -> None:
    frame = Path(sys.argv[1])
    candidates = sys.argv[2].split(",") if len(sys.argv) > 2 else []
    font = Font()
    for band in bands(frame):
        measured = band["x1"] - band["x0"] + 1
        line = f"  y={band['y0']}..{band['y1']} ink x=[{band['x0']},{band['x1']}] width={measured}"
        if candidates:
            hits = font.match(measured, candidates, SCALE, tol=6)
            line += "  matches: " + (", ".join(f"{c!r}({w})" for c, w, _ in hits) if hits else "none")
        print(line)


if __name__ == "__main__":
    main()
