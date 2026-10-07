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
# puts an 18 in robot's face 2.2 in from it; qual_right.fit moves the FLOWER points so the face stops at
# baselines_v.FLOWER_FACE_V, which seated() sets to 4.59.
SEAT_FACE_IN = 4.59


class seated:
    """Within it, the V's FLOWER points put the face SEAT_FACE_IN from the FLOWER's centre (baselines_v.FLOWER_FACE_V,
    which qual_right.fit applies; shifting helpers' points as well would move the robot out twice)."""

    def __enter__(self):
        self.saved = baselines_v.FLOWER_FACE_V
        baselines_v.FLOWER_FACE_V = SEAT_FACE_IN
        return self

    def __exit__(self, *a):
        baselines_v.FLOWER_FACE_V = self.saved


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
# On the transfer's 0.5 s feed (RobotDesign.transferFeedS, claude/simulator) the 4th POLLEN is ready about 0.5 s after it
# leaves the FLOWER, so 1.3 s leaves with it still aboard and TIP 2 never comes: -leave1800 and -leave2300 wait for it.
LEAVE.update({f"{n}-leave{ms}": (b, w, ms) for n, (b, w) in list(ROUTES.items()) for ms in (1800, 2300)})
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


FLOWER_FIRST = {f"qual-right-v-flower-first-l{ms}": ms for ms in (1800, 2000, 2200, 2600, 3000, 3400)}

if __name__ == "__main__":
    for name, ms in FLOWER_FIRST.items():
        r = build_flower_first(name, 200, ms)
        r.folder = autogen.EXPERIMENTS
        r.write()
        print(name)


# Mentor review of flower first (7 Oct 2026): "shoot the four that you have, then stop shooting until all four are
# collected, do the turning, get oriented, then shoot the last four ... the extra half a second is worth it to be
# perfectly lined up"; and the turn away from the FLOWER "might be running into where the balls will drop". So: seated,
# the 4 preloads one by one (each makes room for a FLOWER POLLEN), hold until the FLOWER's 4 are in, back out and turn
# to N_FIRE, then fire them there, stopped: TIP 2 can only start once they're away, so the robot is already standing
# where the baseline waits for its spill.
def right_flower_first_hold(name, robot="baseline", n_fire=None, _tail=True, **kw):
    r = _right(name, robot=robot, n_fire=n_fire, **kw)
    r.cards, r.lines, r.path_ends = [], [], {}
    r.at = "START"
    r.add(r.action("SpinUp"), *_flower(r, "FAR_FLOWER", "The far FLOWER", 1500)[:-1],
          # The preloads one by one only once the left CELL is up: LaunchOne waits until it has fired, and the CELL
          # that is up before TIP 1 is out of the seat's range, so a missed TIP 1 held the robot there all AUTO. Without
          # TIP 1 it goes on full (the next wait passes at once) and fires them from N_FIRE.
          r.wait("TIP 1 (the partner), seated at the FLOWER", when=["LeftCellUp"], ms=FLOWER_TIP1_MS[0],
                 yes=[r.action("LaunchOne") for _ in range(4)], yes_label="TIP 1: fire the preloads",
                 no_label="No TIP 1: on to N_FIRE with them"),
          r.wait("The far FLOWER's 4, collected", when=["IntakeFull"], ms=FLOWER_HOLD_MS[0]))
    r.at = "FAR_FLOWER"
    r.add(r.go("N_FIRE", turn_after=0.3, turn_by=1.0))
    r.at = "N_FIRE"
    r.add(r.wait("Fire the far FLOWER's 4, lined up (TIP 2)", when=["Tip"], ms=2500, alongside="LaunchAll"))
    if _tail:
        r.add(*qual_right.tail(r, **kw))
    return r


FLOWER_HOLD_MS = [3000]
# How long to sit at the FLOWER for the partner's TIP 1 (it settles about 3 s after the robot seats); past this the
# robot leaves for N_FIRE with its preloads and fires them there.
FLOWER_TIP1_MS = [5000]  # at most: 4 pulls at 0.5 s after the first shot, then the transfer's feed


def build_flower_first_hold(name, wait=200):
    qual_right.right = right_flower_first_hold
    restore = retime.with_wait("qual-right-v", wait)
    try:
        with seated():
            return baselines_v.build_for_v(baselines_v.BASELINES["qual-right-v"], name)
    finally:
        restore()
        qual_right.right = _right


if __name__ == "__main__":
    r = build_flower_first_hold("qual-right-v-flower-first-hold")
    r.folder = autogen.EXPERIMENTS
    r.write()
    print(r.name if hasattr(r, "name") else "qual-right-v-flower-first-hold")


# Mentor review of the hold route's logs (7 Oct 2026): "The park should go further into the park zone; it just barely
# parks" and "there is so much time before parking that we really should wait for TIP 3, fill up, then park". TIP 3
# comes at about 21.7 s and the robot was parked by 22.1 s. So from S_FIRE once the GARDEN's shots are away: wait for
# TIP 3, give its spill a moment to land, take what the webcam sees of it (the spill scatters: half of it lands on our
# side, near the GARDEN and up the west side), then drive into PARK. The endgame guard still cuts to PARK in time.
# PARK deeper: the robot's centre at y 95 (its frame y 87.4-102.6, about 8 in inside the zone, y 94.3-117.9), x 10.5.
# Both robots can't be fully inside (the zone is 23.6 in long, the two robots 33), so the partner parks at the zone's
# far end, y 116 (partner-preloads-right-high), 1.6 in clear of our V's tips.
DEEP_PARK = (10.5, 95, 90)
TIP3_MS, LAND_MS, COLLECT_MS = 2500, 800, 3000


def tip3_collect_park(r, tag="", **kw):
    r.at = "S_FIRE"
    r.pt("PARK", *DEEP_PARK)
    # The webcam looks from S_COLLECT facing west, away from the HIVE: from S_FIRE facing north it chased a piece up
    # beside the HIVE frame's west foot bar (x 46, y 51-90) and stalled there. The park path runs up the west side
    # (x 24), clear of the bar, from S_COLLECT.
    r.pt("S_COLLECT", 50, 24, 180)
    out = [r.wait(f"TIP 3{tag}", when=["Tip"], ms=TIP3_MS), r.go("S_COLLECT", turn_by=0.8)]
    r.at = "S_COLLECT"
    out += [r.wait(f"TIP 3's spill lands{tag}", when=["IntakeFull"], ms=LAND_MS),
            r.wait(f"Fill up from TIP 3's spill{tag}", when=["IntakeFull"], ms=COLLECT_MS, alongside="CollectSeen")]
    return out + [r.go("PARK", ctrl=[(24, 30), (24, 70)], heading=90, turn_by=0.5, park=True)]


def partner_right_high(name="partner-preloads-right-high"):
    """partners.partner_right, parked at the LOADING ZONE's far end (y 116), leaving room for us to park deep."""
    import autogen as ag
    from helpers import fire as hfire
    r = ag.Route(name, (59, 9.5, 90), speed=40)
    r.pt("PARK_P", 10.5, 116, 90)
    r.add(r.action("SpinUp"), r.action("IntakeOff"), hfire(r, "Fire the preloads", "Empty", ms=4500),
          r.go("PARK_P", ctrl=[(26, 20), (26, 100)], park=True))
    return r


def build_flower_first_tip3(name, wait=200):
    # baselines_v.shoots_right ends the route with park_from_garden (its third_load): swap that for ours.
    park = baselines_v.park_from_garden
    baselines_v.park_from_garden = tip3_collect_park
    try:
        return build_flower_first_hold(name, wait)
    finally:
        baselines_v.park_from_garden = park


if __name__ == "__main__":
    for r in (build_flower_first_tip3("qual-right-v-flower-first-tip3"), partner_right_high()):
        r.folder = autogen.EXPERIMENTS
        r.write()
        print(r.name)


# Mentor review of the TIP 3 route (7 Oct 2026, evening):
# 1. TIP 2: "slightly too close to the tip zone ... 1-3 inches further back but started moving toward the drop zone",
#    timed so no spilled piece touches the robot as it falls (G409) yet it still collects 4. N_FIRE moves back
#    NORTH_BACK_IN; the drive south through the spill starts TIP2_GO_MS after the TIP starts (settle=False, extra).
# 2. TIP 3: "our robot should be catching stuff but it's oriented the wrong way". TIP 3's spill comes to rest along
#    the south wall (20 runs: x 36-72, y 1-19), behind a robot firing from S_FIRE facing north. It now fires the
#    GARDEN facing south (the turret aims), then sweeps west along y 10 through the spill.
# 3. "plenty of time to pick that tip up and go shoot them on the opposite side then park ... the partner park closer
#    to their side": up the lane under the HIVE (x 50, between the foot bars at x 33 and 83) and west into the
#    LOADING ZONE's north end, PARK_N (14.5, 116) facing west, 50 in from the left CELL (up after TIP 3, at about
#    (58, 90)), inside the turret's 60 in, so it fires them from PARK. The partner parks at the zone's south end
#    (partner-preloads-right-south, (10.5, 96)), on its side, 2.4 in clear of us.
NORTH_BACK_IN, TIP2_GO_MS = [2.0], [1200]


def tip3_sweep_north(r, tag="", **kw):
    """From S_FIRE, facing the HIVE, once the GARDEN's shots are away: TIP 3's spill rolls south off the HIVE toward
    the wall, past S_FIRE, so the robot holds there facing it, intake running, and catches what rolls in (mentor:
    "catching stuff but it's oriented the wrong way": it used to turn west to look for the spill with the webcam).
    Then straight north up the lane under the HIVE (x 57.5, between the frame's feet at x 46 and 95), west into
    PARK_N, and fires what it caught from there."""
    r.at = "S_FIRE"
    x, y, h = r.points["S_FIRE"]
    r.pt("S_CATCH3", CATCH3_X[0], y, 90)
    r.pt("PARK", 14.5, 116, 180)
    out = []
    if abs(CATCH3_X[0] - x) > 0.1:
        out.append(r.go("S_CATCH3", heading=90))
        r.at = "S_CATCH3"
    # One path up the lane and round into PARK: two (a stop at the lane's end) left the endgame guard too little
    # time, and it cut the fire from PARK.
    # Straight up the lane to N_FIRE (a curve into PARK from S_FIRE bent west under the HIVE into the frame's west
    # foot), fire there standing still, lined up, about 26 in from the left CELL (up after TIP 3, about (58, 90)),
    # then the short hop west into PARK. The park path is the last card (the endgame guard never cuts the last card,
    # and a fire card after it was cut every time); short, so the guard leaves the fire its time.
    r.pt("N_UP", 57.5, 116, 90)
    if FIRE3_AT[0] == "south":
        # Fire from the catch spot itself, over the HIVE: the left CELL is within the turret's 60 in of y 32 on the
        # lane. Then up the lane and west into PARK, the last card.
        r.pt("S_CATCH3", CATCH3_X[0], 32, 90)
        out = [r.go("S_CATCH3", heading=90)]
        r.at = "S_CATCH3"
        out += [r.wait(f"TIP 3{tag}", when=["Tip"], ms=TIP3_MS),
                r.wait(f"Catch TIP 3's spill{tag}", when=["IntakeFull"], ms=CATCH3_MS[0]),
                r.wait(f"Fire TIP 3's spill at the left CELL{tag}", when=["Empty"], ms=1500, alongside="LaunchAll"),
                r.go("PARK", ctrl=[(57.5, 116)], turn_after=0.55, turn_by=0.9, park=True)]
        r.at = "PARK"
        return out
    out += [r.wait(f"TIP 3{tag}", when=["Tip"], ms=TIP3_MS),
            r.wait(f"Catch TIP 3's spill{tag}", when=["IntakeFull"], ms=CATCH3_MS[0])]
    if FIRE3_AT[0] == "north":
        out.append(r.go("N_UP", heading=90))
        r.at = "N_UP"
        out += [r.wait(f"Fire TIP 3's spill at the left CELL{tag}", when=["Empty"], ms=1500, alongside="LaunchAll"),
                r.go("PARK", turn_after=0.1, turn_by=0.6, park=True)]
    else:
        # One park path from the catch spot, so the endgame guard knows the whole drive: with a stop at N_UP it
        # judged only the hop from there, cut a late TIP 3's catch too late, and the robot ended short of the zone
        # (2 runs in 20). Control points stacked at the lane's top (x 58.5, an inch right of the lane) hold it on the
        # lane until its back is past the HIVE frame's west foot (x 45-47, to y 90): with two, it bent west early and
        # its rear corner caught the foot at y 96 in every run.
        out.append(r.go("PARK", ctrl=[(58.5, 100), (58.5, 116), (58.5, 116), (58.5, 116)], turn_after=0.8,
                        turn_by=0.98, park=True))
    r.at = "PARK"
    return out


CATCH3_X, CATCH3_MS, FIRE3_AT = [57.5], [2000], ["north"]


def right_flower_first_north(name, robot="baseline", n_fire=None, **kw):
    if TIP2_GO_MS[0] is not None:  # None: the baseline's TIP 2 timing (until the CELL settles, then its extra)
        kw = dict(kw, settle=False, extra=TIP2_GO_MS[0])
    r = right_flower_first_hold(name, robot=robot, n_fire=n_fire, _tail=False, **kw)
    x, y, h = r.points["N_FIRE"]
    r.pt("N_FIRE", x, y + NORTH_BACK_IN[0], h)
    r.add(*qual_right.tail(r, **kw))
    return r


def partner_right_south(name="partner-preloads-right-south"):
    """partners.partner_right, parked at the LOADING ZONE's south end (y 96), its own side, leaving the north end to us."""
    import autogen as ag
    from helpers import fire as hfire
    r = ag.Route(name, (59, 9.5, 90), speed=40)
    r.pt("PARK_P", 10.5, 96, 90)
    r.add(r.action("SpinUp"), r.action("IntakeOff"), hfire(r, "Fire the preloads", "Empty", ms=4500),
          r.go("PARK_P", ctrl=[(26, 20), (26, 80)], park=True))
    return r


def build_flower_first_north(name, back=2.0, go_ms=1200, wait=200, catch_x=57.5, catch_ms=2000, fire_at="north"):
    NORTH_BACK_IN[0], TIP2_GO_MS[0], CATCH3_X[0], CATCH3_MS[0], FIRE3_AT[0] = back, go_ms, catch_x, catch_ms, fire_at
    park, right = baselines_v.park_from_garden, qual_right.right
    baselines_v.park_from_garden = tip3_sweep_north
    restore = retime.with_wait("qual-right-v", wait)
    qual_right.right = right_flower_first_north
    try:
        with seated():
            return baselines_v.build_for_v(baselines_v.BASELINES["qual-right-v"], name)
    finally:
        restore()
        baselines_v.park_from_garden, qual_right.right = park, right


NORTH = {f"qual-right-v-flower-first-north-b{int(b)}-g{g}": (b, g, 57.5, 2000) for b in (0.0, 2.0) for g in (600, 900, 1200, 1500, 1800)}
NORTH.update({f"qual-right-v-flower-first-north-c{int(x)}-{ms}": (2.0, 1200, x, ms) for x in (50, 57.5) for ms in (1500, 2500, 3500)})
SOUTH_FIRE = {f"qual-right-v-flower-first-south-g{g}-c{ms}": (2.0, g, 57.5, ms) for g in (1200, 1500) for ms in (1000, 1500, 2000)}
CARRY = {f"qual-right-v-flower-first-carry-g{g}-c{ms}": (2.0, g, 57.5, ms) for g in (1200, 1500) for ms in (1500, 2000)}
CARRY.update({f"qual-right-v-flower-first-carry-settle-b{int(b)}": (b, None, 57.5, 1500) for b in (0.0, 2.0)})

if __name__ == "__main__":
    for name, (b, g, cx, cms) in NORTH.items():
        r = build_flower_first_north(name, b, g, catch_x=cx, catch_ms=cms)
        r.folder = autogen.EXPERIMENTS
        r.write()
    for name, (b, g, cx, cms) in CARRY.items():
        r = build_flower_first_north(name, b, g, catch_x=cx, catch_ms=cms, fire_at="none")
        r.folder = autogen.EXPERIMENTS
        r.write()
    for name, (b, g, cx, cms) in SOUTH_FIRE.items():
        r = build_flower_first_north(name, b, g, catch_x=cx, catch_ms=cms, fire_at="south")
        r.folder = autogen.EXPERIMENTS
        r.write()
    r = partner_right_south()
    r.folder = autogen.EXPERIMENTS
    r.write()
    print(len(NORTH), "north variants and", r.name)
