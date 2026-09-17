"""Final capture pass for the three chat-autocomplete shots.

Per command prefix:
  * verify the scene through the client log (noon / clear weather / frozen pose),
  * clear the chat history (F3+D),
  * open the chat, type the prefix, capture the frame the game itself saves (F2),
  * read the input line back with the client's own font metrics (no OCR available),
  * Tab-complete through the suggestion list, capturing each step.

Writes renders/frames/<tag>-full.png plus evidence/<tag>.json.
"""

from __future__ import annotations

import json
import sys
import time
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
sys.path.insert(0, str(Path(__file__).resolve().parent.parent / "tools"))
import drive_ui as D  # noqa: E402
from collect_autocomplete import ink_box  # noqa: E402
from mcfont import Font  # noqa: E402

ART = Path(__file__).resolve().parent.parent
FONT = Font()
SCALE = 4

JOBS = [
    {
        "tag": "simpletpa-tp",
        "typed": "/tp",
        "candidates": ["/tp", "/tp ", "/tpaccept", "/tpautoaccept", "/tpdeny",
                       "/tprequest", "/tpdeny @p", "/tpautoaccept ", "/tprequest @p"],
    },
    {
        "tag": "simplehomes-home",
        "typed": "/home ",
        "candidates": ["/home ", "/home base", "/home mine", "/home village", "/homeof",
                       "/sethome", "/sharehome"],
    },
    {
        "tag": "spawncommands-spawn",
        "typed": "/spawn",
        "candidates": ["/spawn", "/spawnof", "/spawnpoint", "/spawnshare", "/spawn ",
                       "/spawnof @p", "/spawnpoint "],
    },
]


def main() -> None:
    ok_commands = [
        ("/time set noon", r"noon|time to 6000"),
        ("/weather clear", r"weather to clear|Set the weather"),
        ("/tick freeze", r"game is frozen|Game is frozen"),
        ("/tp @s 0.5 -60 0.5 0 -90", r"Teleported"),
    ]
    results = []
    for job in JOBS:
        D.ensure_ingame()
        sent = []
        for command, pattern in ok_commands:
            sent.append({"command": command, "verified": bool(D.send_command(command, pattern))})
        D.focus()
        D.press("F3+d")                       # empty chat history
        time.sleep(1.2)
        D.focus()
        D.press("t")
        time.sleep(1.5)
        D.run(["xdotool", "type", "--delay", "60", "--clearmodifiers", "--", job["typed"]])
        time.sleep(2.5)
        frame = D.shot(f"{job['tag']}-full")
        box = ink_box(frame)
        entry = {
            "tag": job["tag"],
            "typed": job["typed"],
            "sceneCommands": sent,
            "frame": str(frame.relative_to(ART)),
            "inputLine": {
                "ink": box,
                "matches": [[c, w] for c, w, _ in FONT.match(box[2], job["candidates"], SCALE, tol=6)] if box else [],
            },
            "candidatesTried": job["candidates"],
            "tabSteps": [],
        }
        for step in range(1, 5):
            D.press("Tab")
            time.sleep(1.6)
            step_frame = D.shot(f"{job['tag']}-tab{step}")
            sbox = ink_box(step_frame)
            entry["tabSteps"].append({
                "step": step,
                "frame": str(step_frame.relative_to(ART)),
                "ink": sbox,
                "matches": [[c, w] for c, w, _ in FONT.match(sbox[2], job["candidates"], SCALE, tol=6)] if sbox else [],
            })
        D.press("Escape")
        time.sleep(0.8)
        (ART / "evidence" / f"{job['tag']}.json").write_text(json.dumps(entry, indent=2) + "\n")
        print(json.dumps(entry["inputLine"]), "->", job["tag"])
        results.append(entry)
    print("done:", [r["tag"] for r in results])


if __name__ == "__main__":
    main()
