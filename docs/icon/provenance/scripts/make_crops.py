"""Crop the deliverable images out of the verified capture frames.

Chat shots: the native autocomplete (MC 26.2 renders it inline in the input line)
plus a small margin, as a tight crop and as a square crop.
BrainageHUD: 600x600 square around the selected Position HUD element's highlight
rectangle, measured in the frame rather than guessed.
"""

from __future__ import annotations

import json
import statistics
import sys
from pathlib import Path

from PIL import Image

ART = Path(__file__).resolve().parent.parent
FRAMES = ART / "renders" / "frames"
OUT = ART / "renders"

CHAT_FRAMES = {
    "simpletpa": "simpletpa-tp-full.png",
    "simplehomes": "simplehomes-home-full.png",
    "spawncommands": "spawncommands-spawn-full.png",
}


def ink_box(frame: Path, y0: int = 2090, y1: int = 2160, x0: int = 12, x1: int = 3400, offset: int = 40):
    im = Image.open(frame).convert("RGB")
    px = im.load()
    w, h = im.size
    y1 = min(y1, h)
    cols, rows = [], []
    for y in range(y0, y1):
        vals = [min(px[x, y]) for x in range(x0, min(x1, w))]
        med = statistics.median(vals)
        hit = [x for x, v in enumerate(vals, start=x0) if v > med + offset]
        if hit:
            rows.append(y)
            cols += hit
    if not cols:
        return None
    return {"x0": min(cols), "x1": max(cols), "y0": min(rows), "y1": max(rows),
            "width": max(cols) - min(cols) + 1, "height": max(rows) - min(rows) + 1}


def chat_crops() -> dict:
    report = {}
    for name, frame_name in CHAT_FRAMES.items():
        frame = FRAMES / frame_name
        im = Image.open(frame).convert("RGB")
        w, h = im.size
        ink = ink_box(frame)
        if ink is None:
            raise SystemExit(f"no input line text found in {frame}")
        tight = (0, max(0, ink["y0"] - 26), min(w, ink["x1"] + 40), h)
        side = max(360, min(720, 2 * ink["width"]))
        square = (0, h - side, min(w, side), h)
        out = {}
        for label, box in (("tight", tight), ("square", square)):
            target = OUT / f"{name}-autocomplete-{label}.png"
            im.crop(box).save(target)
            out[label] = {"path": str(target.relative_to(ART)), "box": list(box),
                          "size": [box[2] - box[0], box[3] - box[1]],
                          "margins": {"left": ink["x0"] - box[0], "right": box[2] - ink["x1"],
                                      "top": ink["y0"] - box[1], "bottom": box[3] - ink["y1"]}}
        out["ink"] = ink
        out["frame"] = str(frame.relative_to(ART))
        report[name] = out
        print(name, json.dumps(out["tight"]), json.dumps(out["square"]))
    return report


def brainagehud_crop() -> dict:
    frame = FRAMES / "hud-editor-selected.png"
    im = Image.open(frame).convert("RGB")
    px = im.load()
    pts = [(x, y) for y in range(0, 1400, 1) for x in range(0, 1800, 1)
           if px[x, y][2] >= 215 and 170 <= px[x, y][0] <= 190 and 185 <= px[x, y][1] <= 200]
    xs = [p[0] for p in pts]
    ys = [p[1] for p in pts]
    rect = {"x0": min(xs), "y0": min(ys), "x1": max(xs), "y1": max(ys)}
    box = (0, 0, 600, 600)
    assert rect["x1"] < box[2] and rect["y1"] < box[3], "600x600 crop would cut the highlight"
    target = OUT / "brainagehud-position-selected-600.png"
    im.crop(box).save(target)
    return {"path": str(target.relative_to(ART)), "box": list(box), "size": [600, 600],
            "highlightRect": rect, "frame": str(frame.relative_to(ART)),
            "margins": {"left": rect["x0"] - box[0], "top": rect["y0"] - box[1],
                        "right": box[2] - rect["x1"], "bottom": box[3] - rect["y1"]}}


def main() -> None:
    report = {"chat": chat_crops(), "brainagehud": brainagehud_crop()}
    (ART / "evidence" / "crop-report.json").write_text(json.dumps(report, indent=2) + "\n")
    print("brainagehud:", json.dumps(report["brainagehud"]))


if __name__ == "__main__":
    main()
