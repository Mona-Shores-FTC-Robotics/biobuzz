from autogen import *


def waits(r, label, cond, total_s, piece=1.0):
    """Waits for cond in pieces of at most `piece` seconds, so the endgame guard can step in between."""
    out, left, k = [], total_s, 1
    while left > 0:
        ms = min(piece, left) * 1000
        out.append(r.wait(label if k == 1 else f"{label} ({k})", when=[cond], ms=ms))
        left -= piece
        k += 1
    return out

def fire(r, label, until, ms=2000):
    return r.wait(label, when=[until], ms=ms, alongside="LaunchAll")
