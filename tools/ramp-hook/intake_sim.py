"""The v2 blade feeding the intake, in the 2-D FLOWER slice: the blade at its stop on the uprights, the POLLEN lifted
over its ridge and rolling down its back slope onto the tiles, and the intake's first roller at the FLOWER's face,
as close as the bracket and pipes allow. No plate, no sheet: the ball crosses about 2 in of open tile on the speed it
gets from the drop, and the roller takes it before it can settle on the ring's edge.

    python3 tools/ramp-hook/intake_sim.py            # the table: out of the FLOWER / past the roller, by speed and mu
    python3 tools/ramp-hook/intake_sim.py --trace     # also writes the ball positions for the section figure

Adds to ramp.py's slice the bracket's lintel (the strip of Layer B in front of its hole, 3.90 in up, that a lifted
POLLEN passes under on its way out) and a roller, a spinning circle whose surface drags the ball toward the robot.
Frame as ramp.py: the wall at x 0, the robot at +x driving toward -x. The simulator has no rolling resistance, so the
2 in of open tile is the part of this to doubt until it is tested."""
import argparse, json, math, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import ramp
from ramp import R, G, DT, REST, K_SHELL, STAGED, contact

TIP_DEPTH = ramp.use_cad_flower()           # the lip stops at the uprights' face
LINTEL = (2.41, 1.54)                       # Layer B's front edge and its hole's front edge, ahead of the FLOWER's centre
LINTEL_Z = 3.90

# the v2 lane's top surface, x behind the lip, z above the tiles (divot_blade.py defaults: bottom 0.52, land 0.08,
# 45 deg nose to the ridge at 1.05, 0.45 behind the lip; 8 deg run for 0.45; 38 deg down to the back edge)
LIP, RIDGE, RIDGE_X, FLOOR = 0.60, 1.05, 0.45, 0.0
ROLLER_X, ROLLER_R, SQUEEZE = 4.7, 1.0, -0.3  # the roller's axle behind the lip, its rim clear of the bracket's front edge (2.41) at the stop; its surface
# 0.3 in above a ball rolling on the tiles (a real compliant wheel squeezes; the simulator's rigid one blocks a ball still bouncing from the drop)
PROFILE = [(0.0, 0.52), (0.0, LIP), (RIDGE_X, RIDGE), (0.90, 0.99), (1.50, 0.52)]


def roller_segments(axle, surface_speed):
    """The roller as 24 chords with their surface velocities (bottom moving toward the robot, +x)."""
    ax, az = axle
    pts = [(ax + ROLLER_R * math.cos(a), az + ROLLER_R * math.sin(a)) for a in [2 * math.pi * k / 24 for k in range(25)]]
    omega = surface_speed / ROLLER_R
    out = []
    for p, q in zip(pts, pts[1:]):
        mx, mz = (p[0] + q[0]) / 2, (p[1] + q[1]) / 2
        out.append(((p, q), (-omega * (mz - az), omega * (mx - ax))))
    return out


def run(drive, mu, roller=True, surface_speed=40.0, e=0.4, t_max=4.0, trace=None):
    fixed = ramp.flower_segments(0.35)
    fixed.append(((ramp.CX + LINTEL[0], LINTEL_Z), (ramp.CX + LINTEL[1], LINTEL_Z)))     # the lintel's underside (ahead of the centre is +x)
    tip_start = ramp.FRONT + ramp.RING_T + 1.0
    tip_stop = ramp.FRONT - TIP_DEPTH
    balls = [[(max(ramp.CX, ramp.BACK + R), z), (0.0, 0.0), 0.0] for z in STAGED]
    out_t, fed_t = [None] * 4, [None] * 4
    tip, t, step = tip_start, 0.0, 0
    axle_z = FLOOR + 2 * R - SQUEEZE + ROLLER_R
    while t < t_max:
        if tip > tip_stop:
            rv = (-drive, 0.0); tip = max(tip_stop, tip - drive * DT)
        else:
            rv = (0.0, 0.0)
        pts = [(tip + x, z) for x, z in PROFILE]
        moving = [(s, rv) for s in zip(pts, pts[1:])]
        if roller:
            moving += [(s, (rv[0] + sv[0], rv[1] + sv[1])) for s, sv in roller_segments((tip + ROLLER_X, axle_z), surface_speed)]
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
        for i, (p, v, w) in enumerate(balls):
            if out_t[i] is None and p[0] - R > ramp.FRONT + ramp.RING_T:
                out_t[i] = t
            if fed_t[i] is None and p[0] >= tip + ROLLER_X + ROLLER_R:        # past the roller: in the robot
                fed_t[i] = t
        if trace is not None and step % 400 == 0:
            trace.append((round(t, 3), round(tip, 3), [(round(b[0][0], 3), round(b[0][1], 3)) for b in balls]))
        if all(f is not None for f in fed_t):
            break
        t += DT; step += 1
    return sum(o is None for o in out_t), sum(f is None for f in fed_t), (max(fed_t) if all(f is not None for f in fed_t) else None)


def main():
    ap = argparse.ArgumentParser(); ap.add_argument("--trace", action="store_true"); a = ap.parse_args()
    print(f"v2 lane: edge {LIP} in over the tiles, 45 deg nose to a ridge at {RIDGE} ({RIDGE_X} behind the lip), 38 deg down to the back edge 1.50 behind; "
          f"roller r {ROLLER_R} at {ROLLER_X} behind the lip: its axle {(ramp.FRONT - TIP_DEPTH + ROLLER_X) - ramp.CX:.2f} in ahead of the "
          f"FLOWER's centre at the stop, its rim {(ramp.FRONT - TIP_DEPTH + ROLLER_X) - ramp.CX - ROLLER_R - 2.41:.2f} in clear of the bracket's "
          f"front edge (2.41) and {(ramp.FRONT - TIP_DEPTH + ROLLER_X) - ramp.CX - ROLLER_R - 2.25:.2f} of the pipes (2.25); "
          f"lip stops {TIP_DEPTH:.2f} past the hole's front edge")
    print("left in the FLOWER / not yet past the roller's axle, and when the last one passed it:")
    print(f"{'drive':>6} {'mu':>4} | {'no roller':>16} | {'roller':>26}")
    for drive in (2.0, 4.0, 12.0):
        for mu in (0.3, 0.5):
            a0 = run(drive, mu, roller=False)
            a1 = run(drive, mu, roller=True)
            print(f"{drive:6.1f} {mu:4.1f} | {a0[0]:>10} {a0[1]:>12} | {a1[0]:>10} {a1[1]:>12} {("-" if a1[2] is None else f"{a1[2]:.2f} s"):>9}")
    if a.trace:
        tr = []
        run(4.0, 0.4, roller=True, trace=tr)
        path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "intake_trace.json")
        json.dump({"profile": PROFILE, "roller": [ROLLER_X, FLOOR + 2 * R - SQUEEZE + ROLLER_R, ROLLER_R], "tip_stop": ramp.FRONT - TIP_DEPTH,
                   "cx": ramp.CX, "front": ramp.FRONT, "trace": tr}, open(path, "w"))
        print("trace:", path, len(tr), "frames")


if __name__ == "__main__":
    main()
