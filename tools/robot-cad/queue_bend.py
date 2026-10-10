# Can a bend in the lane count pieces? Pieces queue single file from the gate, pressed onto the INSIDE of a bend (a
# guide on the convex side, a sprung belt or wall outside). On a straight lane a queue's length is the sum of its
# diameters, and 4 NECTAR (3.62 in) outlast 5 POLLEN (2.8 in): no lane length takes any 4 and refuses every 5.
# Round a bend, small pieces waste more of the corner than big ones, so 5 POLLEN can need more room than 4 NECTAR.
# Window = (shortest 5-piece queue) - (longest 4-piece queue), over every mix; the mouth goes inside it.
#   python3 tools/robot-cad/queue_bend.py        # inches; 2D, ideal spheres, every piece touching the guide
import math, itertools
def guide(s, L1, R, PHI):
    if s <= L1: return (s, 0.0), (0.0, 1.0)
    a = (s - L1) / R
    if a <= PHI: return (L1 + R*math.sin(a), -R + R*math.cos(a)), (math.sin(a), math.cos(a))
    nx, ny = math.sin(PHI), math.cos(PHI); x, y = L1 + R*nx, -R + R*ny; d = s - L1 - R*PHI
    return (x + d*ny, y - d*nx), (nx, ny)
def centre(s, r, g):
    (x, y), (nx, ny) = guide(s, *g); return (x + r*nx, y + r*ny)
def tail(seq, g):
    r0 = seq[0]/2; s = r0; c = centre(s, r0, g); pr = r0; placed = [(c, r0)]
    for d in seq[1:]:
        r = d/2; t = s
        while True:   # scan forward: first s clear of every placed piece
            t += 0.004
            q = centre(t, r, g)
            if all(math.dist(q, pc) >= r + rr - 1e-9 for pc, rr in placed): break
        s, pr = t, r; placed.append((q, r))
    return s + pr
def win(n, p, g):
    f4 = max(tail(q, g) for q in itertools.product((n, p), repeat=4))
    f5 = min(tail(q, g) for q in itertools.product((n, p), repeat=5))
    return f4, f5, f5 - f4
if __name__ == "__main__":
    n, p = 3.62, 2.8
    print(f"straight lane: window {5 * p - 4 * n:+.2f} in")
    rows = []
    for PHI in (90, 120, 150, 180):
        for L1 in (0, 1, 2, 3, 4, 5, 6):
            for R in (0.25, 0.5, 1.0, 1.5, 2.0):
                f4, f5, w = win(n, p, (L1, R, math.radians(PHI)))
                rows.append((w, PHI, L1, R, f4, f5))
    for PHI in (90, 120, 150, 180):
        w, _, L1, R, f4, f5 = max(r for r in rows if r[1] == PHI)
        print(f"bend {PHI:3d} deg, best: gate {L1} in before the bend, inner radius {R:.2f} in -> window {w:+.2f} in "
              f"(mouth between {f4:.2f} and {f5:.2f} in of guide from the gate)")
