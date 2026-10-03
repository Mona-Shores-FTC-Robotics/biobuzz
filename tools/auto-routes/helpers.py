from autogen import *


def waits(r, label, cond, total_s):
    """One wait for cond, at most total_s. (Until 1 Oct 2026 this chained 1 s waits so the endgame guard
    could step in between them; the guard now cuts a wait mid-way, so one card does.) Returns a list, so
    callers can splat it."""
    return [r.wait(label, when=[cond], ms=round(total_s * 1000))]

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
    """Get clear of the FLOWER without turning, then one path that turns in its first half (still
    clear of the FLOWER) and drives in square; collect. Leaves the robot at `name` (against it)."""
    h = r.points[name][2]
    cur = r.points[r.at][2]
    out = []
    if abs((cur - h + 180) % 360 - 180) > 20:
        here, there = r.points[r.at], r.points[name + "_TURN"]
        across = abs(r.points[name][2] % 180) < 45  # approach along x: move in x first
        bend = (there[0], here[1]) if across else (here[0], there[1])
        # Turn on the way to _TURN, once half way and clear of the start (a robot turns at most 300 deg/s, so a
        # turn squeezed into the short last leg would still be going when it reached the FLOWER).
        out += [r.go(name + "_TURN", ctrl=[bend], turn_from=cur, turn_after=0.5, turn_by=1.0), r.go(name, heading=h)]
    else:
        out += [r.go(name + "_IN", heading=h), r.go(name, heading=h)]
    # A FLOWER gives one POLLEN per 0.5 s (RobotDesign.flowerPullS): 4 take 2 s once there.
    return out + [r.wait(label, when=["IntakeFull"], ms=max(ms, 2300))]


def leave_flower(r, name, to):
    """From against the FLOWER to `to` (a spot beside it, like a robot's home): back straight off to
    the line of `to`, turning only once clear, then drive square into `to`."""
    h, t, dest = r.points[name][2], r.points[name + "_TURN"], r.points[to]
    # Turn at x <= 57.5: a turning robot's corners reach 12.7 in, and the centre line is at 70.75.
    r.pt(name + "_BACK_" + to, min(dest[0], 57.5), t[1], dest[2])
    # The last leg reaches `to`'s x early: drifting across beside the FLOWER would brush it.
    return [r.go(name + "_BACK_" + to, turn_from=h, turn_after=0.5, turn_by=1.0),
            r.go(to, ctrl=[(dest[0], t[1] + 2)], heading=dest[2])]
