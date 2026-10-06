"""How sensitive is "shoot NECTAR into a backboard, it drops into a FLOWER" to what we haven't measured?

    python3 tools/flower-backboard/plate.py            # prints the tables in doc/flower-backboard.md

Not FieldSim. FieldSim's robot parts are upright boxes turned only in yaw, its bounces have no friction or
spin change at impact, and its FLOWER has no top ring or backstop (doc/flower-backboard.md, "Physics"). This
is a 2-D slice through the FLOWER's centre, perpendicular to the wall, x inches from the wall, z up:

- FLOWER, Competition Manual §9.7 and Fig 9-12: opening 4.0 in across, its top 21.5 in up; the backstop
  1.89 in from the centre on the wall side, 1.25 in tall. The centre is 2.71 in from the wall (AdvantageScope's
  field CAD, biobuzz-staged-pieces.csv). NECTAR 3.6 in (§9.8), so a straight drop has +/- 0.2 in.
- The ball: a hollow shell (I = 2/3 m r^2) under gravity, no drag (the flight is about a foot). Impacts
  with restitution E and Coulomb friction MU, which changes its spin. E, MU and the launcher's spin are
  not measured: the point of the script is to sweep them.
- The launch: from 9 in behind the robot's front face and 17 in up (FieldSim's exit height), the front
  face against the FLOWER's top ring (5.11 in from the wall). Speed V, and the angle that aims the ball's
  centre at the plate, on the low or the high arc. Noise from FieldSim's launcher spread (1.5% speed,
  0.8 deg angle and yaw).
- The plate: 6 in long, its face toward the robot, tilted ALPHA from vertical with its top leaning toward
  the robot; placed so a ball touching it 1.5 in from its low end is centred over the opening at PLATE_Z. A plate is
  allowed only if its top is at most 29 in up (R105), its low end is above the backstop with 0.5 in to
  spare, and it does not reach past the wall.

"In": the ball's centre passes down through the rim (21.5 in) within 0.2 in of the opening's centre, in
x (in the slice, where the rim and backstop are, so a ball can also be caught by the backstop and drop in)
and y. The y miss comes from yaw alone, so it is the same for every plate: the share yaw leaves within
0.2 in (YAW_CEILING) multiplies the slice's share. Each slice share is from the same n random draws, so rows
compare fairly, but each is +/- about 8 points (n = 30-40).
"""
import math
import multiprocessing
import random

G = 386.1
R = 1.8
CX, TOP, RING = 2.71, 21.5, 2.0
BACKSTOP_X, BACKSTOP_TOP = CX - 1.89, TOP + 1.25
FACE_X = CX + 2.40
EXIT = (FACE_X + 9.0, 17.0)
SPEED_SPREAD, ANGLE_SPREAD = 0.015, math.radians(0.8)
K_SHELL = 2.0 / 3.0
BELOW, ABOVE = 1.5, 4.5  # the plate's length below and above where the ball meets it
HEIGHT_LIMIT = 29.0  # R105.A


def aim(v, target, high):
    """Launch angle (rad above horizontal, toward the wall) whose path passes through target, or None."""
    dx, dz = EXIT[0] - target[0], target[1] - EXIT[1]
    a = G * dx * dx / (2 * v * v)
    disc = dx * dx - 4 * a * (a + dz)
    if disc < 0:
        return None
    t = (dx + (1 if high else -1) * math.sqrt(disc)) / (2 * a)
    return math.atan(t)


def segments(alpha, plate_z):
    # Plate: face normal toward the robot and down, n = (cos a, -sin a); along it, u = (sin a, cos a).
    n = (math.cos(alpha), -math.sin(alpha))
    u = (math.sin(alpha), math.cos(alpha))
    q = (CX - R * n[0], plate_z - R * n[1])
    plate = ((q[0] - BELOW * u[0], q[1] - BELOW * u[1]), (q[0] + ABOVE * u[0], q[1] + ABOVE * u[1]))
    backstop = ((BACKSTOP_X, TOP), (BACKSTOP_X, BACKSTOP_TOP))
    rim_field = ((CX + RING, TOP), (FACE_X, TOP))
    return [plate, backstop, rim_field], (CX, plate_z)


def allowed(alpha, plate_z):
    lo, hi = segments(alpha, plate_z)[0][0]
    return hi[1] <= HEIGHT_LIMIT and lo[1] >= BACKSTOP_TOP + 0.5 and min(lo[0], hi[0]) >= 0


def collide(p, v, w, seg, e, mu):
    (ax, az), (bx, bz) = seg
    ex, ez = bx - ax, bz - az
    t = max(0.0, min(1.0, ((p[0] - ax) * ex + (p[1] - az) * ez) / (ex * ex + ez * ez)))
    cx, cz = ax + t * ex, az + t * ez
    dx, dz = p[0] - cx, p[1] - cz
    d = math.hypot(dx, dz)
    if d >= R or d < 1e-9:
        return p, v, w, False
    nx, nz = dx / d, dz / d
    vn = v[0] * nx + v[1] * nz
    p = (cx + nx * R, cz + nz * R)
    if vn >= 0:
        return p, v, w, False
    tx, tz = -nz, nx
    # Slip of the contact point along t: ball velocity plus spin (w, about the y axis toward the viewer).
    slip = v[0] * tx + v[1] * tz + w * R
    jn = -(1 + e) * vn
    jt_stick = -slip / (1 + 1 / K_SHELL)
    jt = max(-mu * jn, min(mu * jn, jt_stick))
    v = (v[0] + jn * nx + jt * tx, v[1] + jn * nz + jt * tz)
    w = w + jt * R / (K_SHELL * R * R)
    return p, v, w, True


def shot(alpha, plate_z, v0, high, e, mu, spin, rnd, speed_off=0.0, angle_off=0.0):
    """One shot; alpha None is no plate, aimed to drop through the opening's centre. True if it goes in."""
    if alpha is None:
        segs, target = segments(0.0, plate_z)
        segs = segs[1:]
    else:
        segs, target = segments(alpha, plate_z)
    th = aim(v0, target, high)
    if th is None:
        return None
    v0 *= 1 + speed_off + SPEED_SPREAD * rnd.gauss(0, 1)
    th += angle_off + ANGLE_SPREAD * rnd.gauss(0, 1)
    p = EXIT
    v = (-v0 * math.cos(th), v0 * math.sin(th))
    w = -spin * v0 / R  # spin > 0: backspin
    hit_plate = alpha is None
    dt = 1e-4
    for _ in range(20000):
        v = (v[0], v[1] - G * dt)
        p = (p[0] + v[0] * dt, p[1] + v[1] * dt)
        for i, s in enumerate(segs):
            p, v, w, hit = collide(p, v, w, s, e, mu)
            hit_plate |= hit and i == 0
        if p[1] < TOP and v[1] < 0:  # through the rim plane
            return hit_plate and abs(p[0] - CX) <= RING - R
        if p[0] < 0 or p[0] > EXIT[0] + 1 or p[1] > 40:
            return False
    return False


def yaw_ceiling(n=20000, seed=7):
    """The share of shots that yaw leaves within 0.2 in sideways, over the exit-to-opening distance."""
    rnd = random.Random(seed)
    return sum(abs((EXIT[0] - CX) * math.tan(ANGLE_SPREAD * rnd.gauss(0, 1))) <= RING - R for _ in range(n)) / n


YAW_CEILING = yaw_ceiling()


def rate(alpha, plate_z, v0, high, e, mu, spin, n=60, seed=1, speed_off=0.0, angle_off=0.0):
    """The share in: in the slice (n shots, the same random draws for every call), times the yaw ceiling."""
    rnd = random.Random(seed)
    got = [shot(alpha, plate_z, v0, high, e, mu, spin, rnd, speed_off, angle_off) for _ in range(n)]
    if got[0] is None:
        return None
    return sum(got) / n * YAW_CEILING


def best_plate(e, mu, spin, n=30):
    best = None
    for a in ANGLES:
        for z in HEIGHTS:
            if not allowed(math.radians(a), z):
                continue
            for v in SPEEDS:
                for h in (False, True):
                    r = rate(math.radians(a), z, v, h, e, mu, spin, n=n)
                    if r is not None and (best is None or r > best[0]):
                        best = (r, a, z, v, h)
    return best


def angle_row(a):
    zs = [z for z in HEIGHTS if allowed(math.radians(a), z)]
    if not zs:
        return None
    cells = []
    for v in SPEEDS:
        rs = [(rate(math.radians(a), z, v, h, 0.3, 0.3, 0.0, n=30), z) for z in zs for h in (False, True)]
        rs = [r for r in rs if r[0] is not None]
        cells.append(f"{max(rs)[0]:.0%} ({max(rs)[1]:g} in)" if rs else "–")
    return f"| {a} deg | " + " | ".join(cells) + " |"


ANGLES = (0, 10, 20, 30, 40, 50, 60, 70, 80)
HEIGHTS = (23.5, 24.0, 24.5, 25.0, 25.5)
SPEEDS = (100, 120, 140, 170, 200, 240)
GUESSES = [(e, mu, 0.0) for e in (0.15, 0.3, 0.5) for mu in (0.0, 0.3, 0.6)] + [(0.3, 0.3, 0.2), (0.3, 0.3, -0.2)]


def main():
    print("A. The best allowed plate for each guess at the unknowns (restitution e, friction mu, launcher spin")
    print("   r*w/v, + = backspin), 30 shots each with FieldSim's launcher spread, times the yaw ceiling (D):\n")
    print("| e | mu | spin | plate angle from vertical | plate height | launch speed | arc | in |")
    print("|---|---|---|---|---|---|---|---|")
    with multiprocessing.Pool() as pool:
        bests = pool.starmap(best_plate, GUESSES)
    for (e, mu, s), b in zip(GUESSES, bests):
        if b is None:
            print(f"| {e} | {mu} | {s} | none | | | | 0% |")
            continue
        r, a, z, v, h = b
        print(f"| {e} | {mu} | {s} | {a} deg | {z:g} in | {v} in/s | {'high' if h else 'low'} | {r:.0%} |")
    r, a, z, v, h = best_plate(0.3, 0.3, 0.0, n=40)
    print(f"\nB. That plate tuned for the middle guess (e 0.3, mu 0.3, no spin: {a} deg, {z:g} in, {v} in/s, "
          f"{'high' if h else 'low'} arc), when the real ball is different:\n")
    print("| e | mu | spin | in |")
    print("|---|---|---|---|")
    for e, mu, s in GUESSES:
        print(f"| {e} | {mu} | {s} | {rate(math.radians(a), z, v, h, e, mu, s, n=40):.0%} |")
    print("\nC. The same plate at the middle guess, with the launcher off by a fixed amount as well as its spread:\n")
    print("| launch speed off by | launch angle off by | in |")
    print("|---|---|---|")
    for so in (-0.10, -0.05, 0.0, 0.05, 0.10):
        for ao in (-2.0, 0.0, 2.0):
            r2 = rate(math.radians(a), z, v, h, 0.3, 0.3, 0.0, n=40, speed_off=so, angle_off=math.radians(ao))
            print(f"| {so:+.0%} | {ao:+g} deg | {r2:.0%} |")
    print(f"\nD. The ceiling from yaw alone: FieldSim's 0.8 deg yaw spread, "
          f"over the {EXIT[0] - CX:.1f} in from the exit to the opening, keeps {YAW_CEILING:.0%} of shots within 0.2 in sideways. Every number above is already multiplied by it.")
    print("\nF. Plate angle against launch speed, at the middle guess (e 0.3, mu 0.3, no spin), "
          "for each angle, the best allowed height (in brackets) and the better of the two arcs:\n")
    print("| plate angle | " + " | ".join(f"{v2} in/s" for v2 in SPEEDS) + " |")
    print("|---|" + "---|" * len(SPEEDS))
    with multiprocessing.Pool() as pool:
        rows = pool.starmap(angle_row, [(a2,) for a2 in ANGLES])
    for row in rows:
        if row:
            print(row)
    print("\nE. No backboard: aimed to drop through the opening's centre (the rim and backstop still there):\n")
    print("| launch speed | arc | in, e 0.15 | in, e 0.3 | in, e 0.5 |")
    print("|---|---|---|---|---|")
    for v2 in (100, 120, 140, 170, 200):
        for h2 in (False, True):
            rs = [rate(None, TOP + R + 0.3, v2, h2, e2, 0.3, 0.0, n=40) for e2 in (0.15, 0.3, 0.5)]
            if rs[0] is None:
                continue
            print(f"| {v2} in/s | {'high' if h2 else 'low'} | " + " | ".join(f"{x:.0%}" for x in rs) + " |")


if __name__ == "__main__":
    main()
