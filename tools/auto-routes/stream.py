"""Shoot while extracting (the user, 6 Oct 2026 evening): the turret fires from the FLOWER while the extractor feeds
roller -> lane -> J -> turret, so the robot no longer drives back to a firing spot with the FLOWER's 4.

Each baseline (baselines_v) with every far-FLOWER visit turned into a stream: drive in as before, StreamOn, stay until
TIP 2 starts (each POLLEN fired as it comes out, at the CELL the turret aims for), StreamOff, then on as before (to
N_FIRE, where the route's tail starts). The robot faces the FLOWER, so this needs the turret ("rigid V, turret
transfer"): the far FLOWER is about 45 in from the left CELL, inside the simulator's 60 in range.

    qual-right-v-stream, qual-stages-angled-v-stream, qual-stages-wall-v-stream     (optionally -x<ms>: retime.py's waits)

    python3 stream.py      writes them into experiments/
"""
import autogen
import baselines_v
import helpers
import qual_right
import retime

_flower, _fire = qual_right.flower, qual_right.fire
# The FLOWER seat (CAD chat, 7 Oct 2026, c04f0cb on claude/robotics-meeting-notes-lq2y55): the FLOWER's centre 4.59 in
# ahead of the robot's face, where the extractor's block stalls on the FLOWER's uprights. helpers.FLOWER_PICKUP_IN
# (11.2 from an 18 in robot's centre) puts the face 2.2 in from it; the routes here move their FLOWER points out by the
# difference, so the face stops at 4.59 on any body (qual_right.fit keeps the face where helpers puts it).
SEAT_FACE_IN = 4.59


class seated:
    """Within it, helpers' FLOWER points put the face SEAT_FACE_IN from the FLOWER's centre."""

    def __enter__(self):
        self.saved = helpers.FLOWER_PICKUP_IN, helpers.FLOWER_APPROACH_IN
        d = SEAT_FACE_IN - (helpers.FLOWER_PICKUP_IN - 9)
        helpers.FLOWER_PICKUP_IN += d
        helpers.FLOWER_APPROACH_IN += d
        return self

    def __exit__(self, *a):
        helpers.FLOWER_PICKUP_IN, helpers.FLOWER_APPROACH_IN = self.saved
SEAT_MS = 400  # the robot settles into the FLOWER after the path ends; the intake pulls meanwhile
STREAM_MS = [4500]  # at most this long at the FLOWER, or until the TIP (set per route)


def flower(r, name, label, ms=1500):
    cards = _flower(r, name, label, ms)
    if name != "FAR_FLOWER":
        return cards
    # flower(): the paths in, then the wait for IntakeFull. Here: stream instead of filling up.
    # Seated first: StreamOn fires even while the robot moves (the shot carries its motion), and the robot is still
    # settling into the FLOWER for about 0.4 s after the path ends, so the first shot went wide in every run.
    return cards[:-1] + [r.wait("Seated at the FLOWER", when=["IntakeFull"], ms=SEAT_MS), r.action("StreamOn"),
                         r.wait("The far FLOWER, fired as it comes out (TIP 2)", when=["Tip"], ms=STREAM_MS[0]),
                         r.action("StreamOff")]


def fire(r, label, until, ms=2000):
    if "far FLOWER" in label:  # already fired from the FLOWER: the drive back to N_FIRE is the next card
        return r.action("StreamOff")
    return _fire(r, label, until, ms)


def build(base, name, wait=None, stream_ms=4500):
    STREAM_MS[0] = stream_ms
    qual_right.flower, qual_right.fire = flower, fire
    restore = retime.with_wait(base, wait) if wait is not None else (lambda: None)
    try:
        with seated():
            return baselines_v.build_for_v(baselines_v.BASELINES[base], name)
    finally:
        restore()
        qual_right.flower, qual_right.fire = _flower, _fire


# The retimed waits for the 0.25 s shots (retime.py): ShootsRight 200 ms, the wall partner 700 ms, the angled
# partner's unchanged.
ROUTES = {"qual-right-v-stream": ("qual-right-v", None), "qual-right-v-stream-x200": ("qual-right-v", 200),
          "qual-stages-angled-v-stream": ("qual-stages-angled-v", None),
          "qual-stages-wall-v-stream": ("qual-stages-wall-v", None), "qual-stages-wall-v-stream-x700": ("qual-stages-wall-v", 700)}
# Leave the FLOWER once its 4 are away (4 pulls at RobotDesign.flowerPullS, 0.5 s, the last shot just after), not when
# TIP 2 starts: waiting for the TIP put the robot back at N_FIRE about 2 s after it started, with the spill scattered
# (2 of 4 picked up against 3.9). Gone 1.3 s after seated (the 4th shot away), it is back as the TIP starts.
LEAVE = {f"{n}-leave": (b, w, 1300) for n, (b, w) in list(ROUTES.items())}
ROUTES = {n: (b, w, 4500) for n, (b, w) in ROUTES.items()}
ROUTES.update(LEAVE)

if __name__ == "__main__":
    for name, (base, wait, ms) in ROUTES.items():
        r = build(base, name, wait, ms)
        r.folder = autogen.EXPERIMENTS
        r.write()
        print(name)


# The user's plan (7 Oct 2026): "path to the FLOWER immediately and shoot all 8 from there". ShootsRight: from the start
# straight to the far FLOWER, seated there while the partner makes TIP 1; once the left CELL rises, StreamOn fires the
# 4 preloads from the seat, and each one that leaves makes room in the lane for a FLOWER POLLEN, fired in turn: all 8
# for TIP 2 without moving. Leave once they're away, for TIP 2's spill, then the baseline's tail.
_right = qual_right.right
FLOWER_FIRST_LEAVE_MS = [2600]  # after StreamOn: 4 preloads at 0.25 s overlapping 4 pulls at 0.5 s, and the last shot


def right_flower_first(name, robot="baseline", n_fire=None, **kw):
    r = _right(name, robot=robot, n_fire=n_fire, **kw)  # for its points and fitted spots; the cards are redrawn below
    r.cards, r.lines, r.path_ends = [], [], {}
    r.at = "START"
    r.add(r.action("SpinUp"), *_flower(r, "FAR_FLOWER", "The far FLOWER", 1500)[:-1],
          r.wait("TIP 1 (the partner), seated at the FLOWER", when=["LeftCellUp"], ms=9000), r.action("StreamOn"),
          r.wait("The preloads and the far FLOWER, all 8 from the seat (TIP 2)", when=["Tip"], ms=FLOWER_FIRST_LEAVE_MS[0]),
          r.action("StreamOff"))
    r.at = "FAR_FLOWER"
    r.add(r.go("N_FIRE", turn_after=0.3, turn_by=1.0))
    r.at = "N_FIRE"
    r.add(*qual_right.tail(r, **kw))
    return r


def build_flower_first(name, wait=200, leave_ms=2600):
    FLOWER_FIRST_LEAVE_MS[0] = leave_ms
    qual_right.right = right_flower_first
    restore = retime.with_wait("qual-right-v", wait)
    try:
        with seated():
            return baselines_v.build_for_v(baselines_v.BASELINES["qual-right-v"], name)
    finally:
        restore()
        qual_right.right = _right


FLOWER_FIRST = {f"qual-right-v-flower-first-l{ms}": ms for ms in (1800, 2000, 2200, 2600)}

if __name__ == "__main__":
    for name, ms in FLOWER_FIRST.items():
        r = build_flower_first(name, 200, ms)
        r.folder = autogen.EXPERIMENTS
        r.write()
        print(name)
