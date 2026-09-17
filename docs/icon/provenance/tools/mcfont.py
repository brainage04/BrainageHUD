"""Minecraft's own bitmap font as a measuring tool for screenshots.

The capture machine has no OCR and no vision model, so the only honest way to
read text in a captured frame is to rebuild the glyph advance table from the
client's own font sheet (assets/minecraft/textures/font/ascii.png inside the
client jar) and compare measured ink extents with the prediction for a candidate
string.

`width(text, scale)` returns the ink width in screenshot pixels for a string
rendered at the given GUI scale; `match(...)` picks the candidate whose predicted
width is closest to a measurement.
"""

from __future__ import annotations

import sys
from pathlib import Path

from PIL import Image

FONT_PNG = Path("/tmp/mcfont/assets/minecraft/textures/font/ascii.png")
CELL = 8
COLS = 16
ASCII_ROWS = [
    None, None,
    "".join(chr(c) for c in range(0x20, 0x30)),
    "".join(chr(c) for c in range(0x30, 0x40)),
    "".join(chr(c) for c in range(0x40, 0x50)),
    "".join(chr(c) for c in range(0x50, 0x60)),
    "".join(chr(c) for c in range(0x60, 0x70)),
    "".join(chr(c) for c in range(0x70, 0x80)),
]
SPACE_ADVANCE = 4


class Font:
    def __init__(self, path: Path = FONT_PNG):
        sheet = Image.open(path).convert("RGBA")
        self.glyphs: dict[str, tuple[int, int, tuple[tuple[bool, ...], ...]]] = {}
        for row, chars in enumerate(ASCII_ROWS):
            if chars is None:
                continue
            for col, char in enumerate(chars):
                cell = sheet.crop((col * CELL, row * CELL, (col + 1) * CELL, (row + 1) * CELL))
                px = cell.load()
                columns = [x for x in range(CELL) if any(px[x, y][3] > 0 for y in range(CELL))]
                if not columns:
                    continue
                left, right = min(columns), max(columns)
                rows = [y for y in range(CELL) if any(px[x, y][3] > 0 for x in range(CELL))]
                top, bottom = min(rows), max(rows)
                bitmap = tuple(
                    tuple(px[x, y][3] > 0 for x in range(left, right + 1)) for y in range(top, bottom + 1)
                )
                self.glyphs[char] = (left, top, bitmap)

    def advance(self, char: str) -> int:
        if char == " ":
            return SPACE_ADVANCE
        glyph = self.glyphs.get(char)
        if glyph is None:
            return SPACE_ADVANCE
        return len(glyph[2][0]) + 1

    def width(self, text: str, scale: int = 4) -> int:
        """Ink width in screenshot pixels (sum of advances minus the trailing gap)."""
        total = sum(self.advance(c) for c in text)
        return max(0, total - 1) * scale

    def ink_columns(self, text: str) -> list[tuple[str, int, int]]:
        """[(char, x0, x1)] ink spans in GUI pixels for the rendered string."""
        out, x = [], 0
        for char in text:
            glyph = self.glyphs.get(char)
            if glyph is not None:
                width = len(glyph[2][0])
                out.append((char, x, x + width - 1))
                x += width + 1
            else:
                x += self.advance(char)
        return out

    def match(self, measured: int, candidates: list[str], scale: int = 4, tol: int = 4) -> list[tuple[str, int, int]]:
        scored = [(c, self.width(c, scale), abs(self.width(c, scale) - measured)) for c in candidates]
        scored.sort(key=lambda t: t[2])
        return [s for s in scored if s[2] <= tol]


def measure_ink(frame: Path, y0: int, y1: int, x0: int = 0, x1: int = 1400, offset: int = 45) -> tuple[int, int, int]:
    """Ink extent (min x, max x, pixel count) of a text row band.

    `offset` is added to the row median so the dim chat text stands out from the
    panel background without needing absolute thresholds.
    """
    import statistics

    im = Image.open(frame).convert("RGB")
    px = im.load()
    cols: list[int] = []
    for y in range(y0, y1):
        vals = [min(px[x, y]) for x in range(x0, x1)]
        med = statistics.median(vals)
        cols += [x for x, v in enumerate(vals, start=x0) if v > med + offset]
    if not cols:
        return (0, 0, 0)
    return (min(cols), max(cols), len(cols))


if __name__ == "__main__":
    font = Font()
    text = sys.argv[1] if len(sys.argv) > 1 else "/tp"
    scale = int(sys.argv[2]) if len(sys.argv) > 2 else 4
    print(f"{text!r} scale {scale}: ink width {font.width(text, scale)} px")
