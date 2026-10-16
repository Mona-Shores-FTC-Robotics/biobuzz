"""Traces for the page's drive-in animation: each blade, at two drive speeds and two frictions, in the 2-D FLOWER slice
with a stall model. Writes animation_runs.json (frames every 10 ms: the lip's x, the four POLLEN; plus events).

    python3 tools/ramp-hook/animate.py

The stall model: ramp.py's blade is driven by position and cannot stall. Here, while the robot is still driving in,
a ball that touches both the blade and the FLOWER's back (the uprights) is checked against the pinch rule: the push
at the blade contact is tilted up by alpha (the contact normal's angle above horizontal), and a steady push lifts the
ball only if alpha > atan(mu) when the ball rolls on the blade and slides on the uprights (the case that fits the
15 Oct test: Block A's 17 deg face at the edge of what PETG on POLLEN allows), or alpha > 2 atan(mu) if it slides on
both. The rolling rule is used; when it fails the robot stops advancing and the run is marked stalled. Everything else
is ramp.py: Coulomb friction, restitution, a hollow ball's inertia, the field-CAD FLOWER, the bracket's lintel, and
for the robot-mounted blades a roller where the 16 Oct CAD puts the intake's first Gecko wheel.
Frame: the FLOWER's, inches, +x toward the wall, origin on the tile under the bottom POLLEN's centre."""
import json, math, multiprocessing as mp, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import ramp
from ramp import R, G, DT, REST, K_SHELL, STAGED, contact
STOP = ramp.use_cad_flower()
CX = ramp.CX
LINTEL = ((CX + 2.41, 3.90), (CX + 1.54, 3.90))
GECKO_R, GECKO_Z, FACE_AHEAD = 0.94, 2.89, 2.5          # the first roller as in the 16 Oct CAD; the robot's face 2.5 in in front of the FLOWER's centre
ROLLER_KISS = 0.3                                          # the simulator's rigid roller sits this far above a rolling ball (a Gecko squeezes it 0.9 in)

BLADES = {
    # name: (profile x behind the lip / z, extra depth past the uprights' points, shaft behind the lip, on the robot?)
    "blockA": ([(0.0, 0.52), (0.0, 0.64), (0.2, 1.30), (0.7, 1.30), (1.4, 0.52)], 0.57, 0.93, False),
    "v2":     ([(0.0, 0.52), (0.0, 0.60), (0.45, 1.05), (0.90, 0.99), (1.50, 0.52)], 0.0, 0.69, True),
    "v2steep": ([(0.0, 0.52), (0.0, 0.53), (0.37, 1.05), (0.97, 1.01), (1.60, 0.52)], 0.0, 0.75, True),
    "v3":     ([(0.0, 0.52), (0.0, 0.53), (0.53, 1.28), (1.29, 0.52)], 0.0, 0.77, True),
}


def roller_segments(axle, surface_speed, r):
    ax, az = axle
    pts = [(ax + r * math.cos(a), az + r * math.sin(a)) for a in [2 * math.pi * k / 24 for k in range(25)]]
    omega = surface_speed / r
    return [((p, q), (-omega * ((p[1] + q[1]) / 2 - az), omega * ((p[0] + q[0]) / 2 - ax))) for p, q in zip(pts, pts[1:])]


def touching(p, seg):
    """(True, normal from the segment toward the ball's centre) if the ball at p touches segment seg."""
    (ax, az), (bx, bz) = seg
    ex, ez = bx - ax, bz - az; L2 = ex * ex + ez * ez
    t = max(0.0, min(1.0, ((p[0] - ax) * ex + (p[1] - az) * ez) / L2))
    cx, cz = ax + t * ex, az + t * ez
    dx, dz = p[0] - cx, p[1] - cz; d = math.hypot(dx, dz)
    return (d <= R + 1e-3, (dx / d, dz / d) if d > 1e-9 else (0.0, 1.0))


def run(blade, drive, mu, e=0.3, t_max=4.0):
    prof, extra, shaft_back, on_robot = BLADES[blade]
    fixed = ramp.flower_segments(0.35) + [LINTEL]
    back_wall = fixed[0]
    tip_depth = STOP + extra
    tip_start = ramp.FRONT + ramp.RING_T + 2.0
    tip_stop = ramp.FRONT - tip_depth
    balls = [[(max(CX, ramp.BACK + R), z), (0.0, 0.0), 0.0] for z in STAGED]
    stalled = None; out_t = [None] * 4; fed_t = [None] * 4
    tip, t, step = tip_start, 0.0, 0
    frames = []
    limit = math.atan(mu)                                  # rolling on the blade, sliding on the uprights
    while t < t_max:
        driving = tip > tip_stop and stalled is None
        rv = (-drive, 0.0) if driving else (0.0, 0.0)
        if driving:
            tip = max(tip_stop, tip - drive * DT)
        pts = [(tip + x, z) for x, z in prof]
        blade_segs = list(zip(pts, pts[1:]))
        moving = [(s, rv) for s in blade_segs]
        if on_robot:
            axle = (tip - (STOP - (-FACE_AHEAD)) - GECKO_R + 0.0, 0.0)   # placeholder, replaced below
            face = tip + (STOP + FACE_AHEAD)                            # the face is 3.68 in behind the lip (sim x grows toward the robot)
            axle = (face + GECKO_R, 2 * R + ROLLER_KISS + GECKO_R)
            moving += [(s, (rv[0] + sv[0], rv[1] + sv[1])) for s, sv in roller_segments(axle, 40.0, GECKO_R)]
        for b in balls:
            p, v, w = b
            v = (v[0], v[1] - G * DT); p = (p[0] + v[0] * DT, p[1] + v[1] * DT)
            for s in fixed:
                p, v, w = contact(p, v, w, s, (0.0, 0.0), e, mu)
            for s, sv in moving:
                p, v, w = contact(p, v, w, s, sv, e, mu)
            b[:] = [p, v, w]
        for i in range(4):
            for j in range(i + 1, 4):
                (p1, v1, w1), (p2, v2, w2) = balls[i], balls[j]
                dx, dz = p2[0] - p1[0], p2[1] - p1[1]; d = math.hypot(dx, dz)
                if 1e-9 < d < 2 * R:
                    nx, nz = dx / d, dz / d; push = (2 * R - d) / 2
                    p1, p2 = (p1[0] - nx * push, p1[1] - nz * push), (p2[0] + nx * push, p2[1] + nz * push)
                    vn = (v2[0] - v1[0]) * nx + (v2[1] - v1[1]) * nz
                    if vn < 0:
                        j_ = -(1 + (e if -vn > REST else 0.0)) * vn / 2
                        v1, v2 = (v1[0] - j_ * nx, v1[1] - j_ * nz), (v2[0] + j_ * nx, v2[1] + j_ * nz)
                    balls[i], balls[j] = [p1, v1, w1], [p2, v2, w2]
        # the stall check: a ball against the back and the blade, with the blade's push too flat to lift it
        if driving:
            for p, v, w in balls:
                if not touching(p, back_wall)[0]:
                    continue
                for s in blade_segs:
                    hit, (nx, nz) = touching(p, s)
                    if hit and nx < -1e-6:                              # the blade pushes the ball toward the wall (-x)
                        alpha = math.atan2(nz, -nx)
                        if alpha < limit:
                            stalled = (round(t, 3), round(math.degrees(alpha), 1)); break
                if stalled: break
        for i, (p, v, w) in enumerate(balls):
            if out_t[i] is None and p[0] - R > ramp.FRONT + ramp.RING_T: out_t[i] = round(t, 3)
            if on_robot and fed_t[i] is None and p[0] >= tip + (STOP + FACE_AHEAD) + GECKO_R: fed_t[i] = round(t, 3)
        if step % 200 == 0:
            frames.append([round(t, 3), round(CX - tip, 3), [[round(CX - p[0], 3), round(p[1], 3)] for p, v, w in balls]])
        if all(f is not None for f in fed_t) or (not on_robot and all(o is not None for o in out_t)):
            frames.append([round(t, 3), round(CX - tip, 3), [[round(CX - p[0], 3), round(p[1], 3)] for p, v, w in balls]]); break
        t += DT; step += 1
    return {"blade": blade, "drive": drive, "mu": mu, "stop": round(CX - tip_stop, 3), "stalled": stalled,
            "out": out_t, "fed": fed_t, "frames": frames}


def main():
    cases = [(b, d, m) for b in BLADES for d in (4.0, 12.0) for m in (0.3, 0.4)]
    with mp.Pool(min(8, len(cases))) as pool:
        runs = pool.starmap(run, cases)
    for r in runs:
        print(f"{r['blade']:8s} {r['drive']:4.0f} in/s mu {r['mu']}: stalled {r['stalled']}, out {r['out']}, fed {r['fed']}, {len(r['frames'])} frames")
    out = {"blades": {k: {"profile": v[0], "shaft_back": v[2], "on_robot": v[3]} for k, v in BLADES.items()},
           "gecko": {"r": GECKO_R, "z": GECKO_Z, "face_ahead": FACE_AHEAD}, "stop": STOP, "runs": runs}
    path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "animation_runs.json")
    json.dump(out, open(path, "w"), separators=(",", ":")); print(path, os.path.getsize(path), "bytes")


if __name__ == "__main__":
    main()
