"""The ramp hook: drive a thin ramp into a FLOWER's retrieval opening and let its POLLEN roll out to the intake.

    python3 tools/ramp-hook/ramp.py            # prints the tables in doc/ramp-hook.md (about 2 minutes, no build)

The idea (meeting, 6 Oct 2026): another team's video shows a thin ramp, rotated down and driven into a FLOWER,
emptying it very fast. Put that ramp on the outside edge of the small right hook (design 14), so one arm does two
jobs: the spill hook while waiting for a TIP, and a FLOWER emptier that feeds the intake.

Not FieldSim: FieldSim takes a FLOWER's POLLEN one at a time (RobotDesign.flowerPullS, 0.5 s each) and has no
ring, window or ramp. This is a 2-D slice through the FLOWER's centre, perpendicular to the wall, x inches from
the wall, z up. The robot comes in from +x.

- FLOWER, Competition Manual §9.7, Fig 9-12: centre 2.71 in from the wall (AdvantageScope's field CAD); a
  retrieval opening 3.55 in tall above a bottom ring 0.43 in tall; the 4 staged POLLEN with their centres at 1.4,
  4.3, 7.2 and 10.1 in (CAD). NOT published, so swept or guessed: the tube's inside radius (1.6 in, 0.2 in round
  a POLLEN), whether the bottom ring really is a lip the bottom POLLEN has to climb (RING), and its thickness.
- POLLEN: 2.8 in (§9.8), 0.055 lb, a hollow shell (I = 2/3 m r^2). Impacts with restitution E and Coulomb
  friction MU that changes the spin; neither is measured, so both are swept. Ball-ball contacts are frictionless.
- The ramp: a thin, straight tongue on the hook's crossbeam (narrower than the opening), sloping down toward the
  robot at SLOPE. It rests on the bottom ring's outer top corner, so inside the tube it rises toward its tip,
  and outside it runs down to the hook's floor plate (0.06 in polycarbonate) on the tiles. The robot drives it in at DRIVE in/s until the tip is TIP_DEPTH past the opening's field edge, then
  stops. The intake is 8 in behind the tip (the small hook's arm): a ball is "fed" once its centre reaches it.

What it answers: how long until all 4 POLLEN are out of the tube and at the intake, and how many stay in, against
FieldSim's 4 x 0.5 s. What it cannot: the 3-D wedge in plan view (the tongue must fit the opening), drag along
the tube wall, a POLLEN that isn't round. The video and a cardboard tongue answer those.
"""
import math
import multiprocessing

G = 386.1
R = 1.4                      # POLLEN radius, §9.8
K_SHELL = 2.0 / 3.0
CX = 2.71                    # FLOWER centre from the wall
TUBE_R = 1.6                 # inside radius: a guess, 0.2 in round a POLLEN
BACK, FRONT = CX - TUBE_R, CX + TUBE_R
WINDOW_TOP = 0.43 + 3.55     # bottom ring + retrieval opening, §9.7
RING_T = 0.25                # the ring's thickness: a guess
STAGED = (1.4, 4.3, 7.2, 10.1)
PLATE_T = 0.06
INTAKE_BEHIND_TIP = 8.0      # the small hook's 8 in arm
DT = 5e-5
REST = 4.0                   # in/s: slower impacts than this don't bounce (keeps resting contacts quiet)


def flower_segments(ring):
    """The FLOWER in the slice: back wall, base, ring (if any), front wall above the window."""
    segs = [((BACK, 0.0), (BACK, 20.0)), ((BACK, 0.0), (FRONT, 0.0)), ((FRONT, WINDOW_TOP), (FRONT, 20.0))]
    if ring > 0:
        segs += [((FRONT, 0.0), (FRONT, ring)), ((FRONT, ring), (FRONT + RING_T, ring)),
                 ((FRONT + RING_T, ring), (FRONT + RING_T, 0.0))]
    segs.append(((FRONT + RING_T, 0.0), (60.0, 0.0)))  # the tiles
    return segs


def tip_height(ring, slope, tip_depth):
    """A straight tongue resting on the ring's outer top corner rises toward its tip: how high the tip is."""
    return ring + PLATE_T + (tip_depth + RING_T) * math.tan(slope)


def ramp_profile(ring, slope, tip_depth):
    """The tongue and floor plate in the robot's frame, x from the tip toward the robot: a list of points."""
    tip_z = tip_height(ring, slope, tip_depth)
    run = (tip_z - PLATE_T) / math.tan(slope)
    return [(0.0, tip_z - 0.05), (0.0, tip_z), (run, PLATE_T), (INTAKE_BEHIND_TIP + 2.0, PLATE_T)]


def contact(p, v, w, seg, seg_v, e, mu, spin=True):
    """One ball against one segment moving at seg_v. Returns the new position, velocity and spin."""
    (ax, az), (bx, bz) = seg
    ex, ez = bx - ax, bz - az
    L2 = ex * ex + ez * ez
    t = max(0.0, min(1.0, ((p[0] - ax) * ex + (p[1] - az) * ez) / L2))
    cx, cz = ax + t * ex, az + t * ez
    dx, dz = p[0] - cx, p[1] - cz
    d = math.hypot(dx, dz)
    if d >= R or d < 1e-9:
        return p, v, w
    nx, nz = dx / d, dz / d
    p = (cx + nx * R, cz + nz * R)
    rvx, rvz = v[0] - seg_v[0], v[1] - seg_v[1]
    vn = rvx * nx + rvz * nz
    if vn >= 0:
        return p, v, w
    tx, tz = -nz, nx
    slip = rvx * tx + rvz * tz + w * R
    jn = -(1 + (e if -vn > REST else 0.0)) * vn
    jt = max(-mu * jn, min(mu * jn, -slip / (1 + 1 / K_SHELL))) if spin else 0.0
    v = (v[0] + jn * nx + jt * tx, v[1] + jn * nz + jt * tz)
    return p, v, w + jt / (K_SHELL * R)


def run(ring=0.43, slope_deg=10.0, drive=12.0, tip_depth=2.4, e=0.4, mu=0.4, t_max=3.0, trace=None):
    """One emptying. Returns (time the last POLLEN left the tube, time it reached the intake, POLLEN left in)."""
    slope = math.radians(slope_deg)
    fixed = flower_segments(ring)
    prof = ramp_profile(ring, slope, tip_depth)
    tip_start = FRONT + RING_T + 1.0           # the tip starts an inch clear of the ring
    tip_stop = FRONT - tip_depth
    balls = [[(CX, z), (0.0, 0.0), 0.0] for z in STAGED]
    out_t = [None] * 4
    fed_t = [None] * 4
    tip = tip_start
    t = 0.0
    step = 0
    while t < t_max:
        moving = tip > tip_stop
        rv = (-drive, 0.0) if moving else (0.0, 0.0)
        if moving:
            tip = max(tip_stop, tip - drive * DT)
        pts = [(tip + x, z) for x, z in prof]
        ramp = list(zip(pts, pts[1:]))
        for b in balls:
            p, v, w = b
            v = (v[0], v[1] - G * DT)
            p = (p[0] + v[0] * DT, p[1] + v[1] * DT)
            for s in fixed:
                p, v, w = contact(p, v, w, s, (0.0, 0.0), e, mu)
            for s in ramp:
                p, v, w = contact(p, v, w, s, rv, e, mu)
            b[:] = [p, v, w]
        for i in range(4):          # ball-ball, frictionless
            for j in range(i + 1, 4):
                (p1, v1, w1), (p2, v2, w2) = balls[i], balls[j]
                dx, dz = p2[0] - p1[0], p2[1] - p1[1]
                d = math.hypot(dx, dz)
                if d < 2 * R and d > 1e-9:
                    nx, nz = dx / d, dz / d
                    push = (2 * R - d) / 2
                    p1, p2 = (p1[0] - nx * push, p1[1] - nz * push), (p2[0] + nx * push, p2[1] + nz * push)
                    vn = (v2[0] - v1[0]) * nx + (v2[1] - v1[1]) * nz
                    if vn < 0:
                        j_ = -(1 + (e if -vn > REST else 0.0)) * vn / 2
                        v1, v2 = (v1[0] - j_ * nx, v1[1] - j_ * nz), (v2[0] + j_ * nx, v2[1] + j_ * nz)
                    balls[i], balls[j] = [p1, v1, w1], [p2, v2, w2]
        intake_x = tip + INTAKE_BEHIND_TIP
        for i, (p, v, w) in enumerate(balls):
            if out_t[i] is None and p[0] - R > FRONT + RING_T:
                out_t[i] = t
            if fed_t[i] is None and p[0] >= intake_x - R:
                fed_t[i] = t
        if trace is not None and step % 400 == 0:
            trace.append((t, tip, [b[0] for b in balls]))
        if all(f is not None for f in fed_t):
            break
        t += DT
        step += 1
    left = sum(o is None for o in out_t)
    last_out = max(o for o in out_t) if left == 0 else None
    last_fed = max(f for f in fed_t) if all(f is not None for f in fed_t) else None
    return last_out, last_fed, left


def fmt(r):
    out, fed, left = r
    if left:
        return f"{left} stay in"
    return f"{out:.2f} s / {fed:.2f} s" if fed is not None else f"{out:.2f} s / not fed"


def table(rows, cols, key):
    jobs = [key(r, c) for r in rows for c in cols]
    with multiprocessing.Pool() as pool:
        res = pool.starmap(_run_kw, [(j,) for j in jobs])
    return [res[i * len(cols):(i + 1) * len(cols)] for i in range(len(rows))]


def _run_kw(kw):
    return run(**kw)


def bounce_off_wall(u, lean_deg, e):
    """A POLLEN rolling outward at u hits the arm's inside wall, leaning in by lean_deg from vertical.
    Returns its speed back toward the robot and its hop height after the wall and one floor bounce."""
    b = math.radians(lean_deg)
    n = (-math.cos(b), -math.sin(b))              # the wall's normal: inward, and down if it leans in
    vn = u * n[0]
    vx, vz = u - (1 + e) * vn * n[0], -(1 + e) * vn * n[1]
    if vz < 0:                                    # sent down into the tiles: one floor bounce
        vz = -e * vz
        vx = vx / (1 + K_SHELL) if abs(vx) > 0 else vx  # friction takes it to rolling (no spin before)
    return -vx, vz * vz / (2 * G)


def main():
    print("A. Time until the last POLLEN is out of the tube / at the intake, 8 in back. Ring 0.43 in, drive in at")
    print("   12 in/s, tip 2.4 in into the opening; e and mu are the unmeasured bounce and friction:\n")
    slopes = (5, 10, 15, 20)
    guesses = [(0.25, 0.3), (0.4, 0.4), (0.55, 0.6)]
    print("| ramp slope | " + " | ".join(f"e {e}, mu {mu}" for e, mu in guesses) + " |")
    print("|---|" + "---|" * len(guesses))
    res = table(slopes, guesses, lambda s, g: dict(slope_deg=s, e=g[0], mu=g[1]))
    for s, row in zip(slopes, res):
        print(f"| {s} deg | " + " | ".join(fmt(r) for r in row) + " |")

    print("\nB. How far in the tip has to go, and how fast to drive it (slope 10 deg, e 0.4, mu 0.4):\n")
    depths = (1.5, 1.8, 2.1, 2.4, 2.7)
    drives = (6, 12, 24)
    print("| tip past the opening's edge | " + " | ".join(f"drive {d} in/s" for d in drives) + " |")
    print("|---|" + "---|" * len(drives))
    res = table(depths, drives, lambda dp, dv: dict(tip_depth=dp, drive=dv))
    for dp, row in zip(depths, res):
        print(f"| {dp} in | " + " | ".join(fmt(r) for r in row) + " |")

    print("\nC. If the bottom ring is lower than 0.43 in, or isn't a lip at all (slope 10 deg, drive 12 in/s, tip 2.4 in):\n")
    rings = (0.0, 0.2, 0.43, 0.6)
    print("| ring | e 0.25, mu 0.3 | e 0.4, mu 0.4 | e 0.55, mu 0.6 |")
    print("|---|---|---|---|")
    res = table(rings, guesses, lambda rg, g: dict(ring=rg, e=g[0], mu=g[1]))
    for rg, row in zip(rings, res):
        print(f"| {rg} in | " + " | ".join(fmt(r) for r in row) + " |")

    print("\nD. FieldSim's FLOWER pull for comparison: 4 x flowerPullS (0.5 s) = 2.0 s, one POLLEN at a time, the robot")
    print("   holding still against the tube, plus 0.35 s a piece through the intake.\n")

    print("E. The arm's inside wall: a POLLEN rolling outward at u hits it. Its speed back toward the robot,")
    print("   and the hop after (vertical wall vs one leaning in over the hook's floor), e 0.4:\n")
    print("| wall | back toward the robot | hop |")
    print("|---|---|---|")
    for lean in (0, 15, 30, 45):
        back, hop = bounce_off_wall(60.0, lean, 0.4)
        print(f"| leaning in {lean} deg | {back / 60:.0%} of u | {hop:.1f} in at u = 60 in/s |")


if __name__ == "__main__":
    main()
