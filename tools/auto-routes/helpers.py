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

# The red FLOWERs (field CAD), and how far from one a robot's centre stands to take its bottom POLLEN:
# the front of an 18 in robot against a ~2 in tube, inside the intake's reach (FieldSim.inIntake).
FAR_FLOWER_AT, WALL_FLOWER_AT = (47.36, 138.79), (2.71, 47.36)
FLOWER_PICKUP_IN, FLOWER_APPROACH_IN = 11.2, 17.0


def flower_points(r, name, at, heading):
    """Points `name` (intake against the FLOWER), `name`_IN (straight back from it) and `name`_TURN
    (further back, where an 18 in robot can turn without its corners, 12.7 in out, reaching the FLOWER)."""
    import math
    c, s = math.cos(math.radians(heading)), math.sin(math.radians(heading))
    for suffix, d in (("", FLOWER_PICKUP_IN), ("_IN", FLOWER_APPROACH_IN), ("_TURN", FLOWER_APPROACH_IN + 2.5)):
        r.pt(name + suffix, round(at[0] - d * c, 2), round(at[1] - d * s, 2), heading)
    return r


def flower(r, name, label, ms=1500):
    """Slide clear without turning, turn where there is room, drive straight in, collect, back straight
    out. A robot that turns beside a FLOWER sweeps its corners through it. Leaves it at `name`_IN."""
    h = r.points[name][2]
    cur = r.points[r.at][2]
    out = []
    if abs((cur - h + 180) % 360 - 180) > 20:
        # Away from the FLOWER first (along the approach line), then across: a diagonal slide from a
        # start beside it would cut the corner through it.
        here, there = r.points[r.at], r.points[name + "_IN"]
        across = abs(r.points[name][2] % 180) < 45  # approach along x: move in x first
        bend = (there[0], here[1]) if across else (here[0], there[1])
        out += [r.go(name + "_IN", ctrl=[bend], heading=cur), r.go(name + "_TURN", turn_from=cur)]
    else:
        out += [r.go(name + "_IN", heading=h)]
    return out + [r.go(name, heading=h), r.wait(label, when=["IntakeFull"], ms=ms), r.go(name + "_IN", heading=h)]


def leave_flower(r, name, to):
    """From `name`_IN back to `to` (a spot beside the FLOWER, like a robot's home): back off to
    `name`_TURN, turn on the way across to the line of `to`, then drive straight in to it."""
    h, t, dest = r.points[name][2], r.points[name + "_TURN"], r.points[to]
    r.pt(name + "_BACK_" + to, dest[0], t[1], dest[2])
    return [r.go(name + "_TURN", heading=h), r.go(name + "_BACK_" + to, turn_from=h), r.go(to, heading=dest[2])]
