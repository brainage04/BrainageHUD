"""Capture a no-chat frame and a chat frame back to back and diff them.

Used to locate the chat panel, the typed input line and the suggestion list in
pixels (the capture machine has no OCR), so the crop boxes are measured rather
than guessed.
"""

from __future__ import annotations

import subprocess
import sys
import time
from pathlib import Path

from PIL import Image

ART = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ART / "scripts"))
import drive_ui as D  # noqa: E402


def main() -> None:
    text = sys.argv[1] if len(sys.argv) > 1 else "/tp"
    tag = sys.argv[2] if len(sys.argv) > 2 else "probe"
    D.focus()
    D.press("Escape")
    time.sleep(1.0)
    a = D.shot(f"{tag}-nochat", settle=1.0)
    D.open_chat(text)
    b = D.shot(f"{tag}-chat")
    D.press("Escape")
    time.sleep(0.8)
    pa = Image.open(a).convert("RGB").load()
    pb = Image.open(b).convert("RGB").load()
    w, h = Image.open(a).size
    rows = {}
    for y in range(0, h):
        xs = [x for x in range(0, 1600) if max(abs(pa[x, y][i] - pb[x, y][i]) for i in range(3)) > 12]
        if xs:
            rows[y] = (len(xs), min(xs), max(xs))
    print(f"typed={text!r}  changed rows below y=1000 (x<1600):")
    bands, cur = [], None
    for y in sorted(rows):
        if cur and y - cur[1] <= 3:
            cur[1] = y
        else:
            if cur:
                bands.append(cur)
            cur = [y, y]
    if cur:
        bands.append(cur)
    for y0, y1 in bands:
        if y1 < 1000:
            continue
        xs = [rows[y][1] for y in range(y0, y1 + 1, 2)] + [rows[y][2] for y in range(y0, y1 + 1, 2)]
        print(f"  band y={y0}..{y1} (h={y1-y0+1}) x=[{min(xs)},{max(xs)}]")


if __name__ == "__main__":
    main()
