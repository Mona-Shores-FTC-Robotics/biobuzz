"""Side and top sketches of the transfer concepts, in the robot frame.

+X forward, +Y left, +Z up, inches, origin on the floor under the chassis centre (7.56 in behind the
front face). Run `python3 doc/transfer/sketch.py` to rewrite the SVGs next to it. Every number in the
sketches comes from the constants below, which doc/transfer.md quotes.
"""
import math, os

OUT = os.path.dirname(os.path.abspath(__file__))
S = 28  # px per inch

# ---- fixed robot (doc/unified-design.md, cad/intake-b) ----
FACE, BACK, HALF_W = 7.56, -7.56, 7.62
WHEEL_X, WHEEL_R, WHEEL_Y = 5.67, 1.89, 6.85
ROLLER_X, ROLLER_Z, ROLLER_R = 8.56, 3.35, 0.945
V_TIP_X, V_TIP_Y = 10.40, 8.89
START_BACK = V_TIP_X - 18.0            # R102: nothing behind this at the start
LENS = (4.0, 14.0)
TURRET_X = -3.17                        # 10.73 in behind the face (scorer chat's CAD read)
BORE_R = 105 / 25.4 / 2                 # goBILDA 3208-0004-0001, 105 mm ID
POLLEN_R, NECTAR_R = 1.40, 1.81

# ---- concept A: floor lane + J-kicker up the turret axis ----
FLOOR = 0.90                             # lane floor top
RAMP = (8.0, 0.05, 5.8, FLOOR)            # ramp from under the roller's rear half up to the lane floor
JW_R = 0.945                             # 48 mm gecko
J_X, J_Z = -1.32, FLOOR + 2.80 - 0.10 + JW_R   # J-wheel axle at its POLLEN hard stop
J_OUT = JW_R + 2.80 - 0.10               # outer J radius about that axle
J_REAR = J_X - J_OUT                     # vertical rear wall of the chute
PIVOT = (J_X + 2.044, J_Z - 1.18)        # arm pivot, 60 mm away at 30 deg (16T HTD5 x2, 40T belt): the queue's push closes the arm
BEARING_Z = (6.6, 7.8)                   # turret bearing, height to confirm from goBILDA's CAD
LANE_W = 4.2                             # clear width between lane walls


def lead(r):
    """Centre X of the first piece, stopped against the stopped J-wheel."""
    cz = FLOOR + r
    d = JW_R + r
    return J_X + math.sqrt(max(d * d - (J_Z - cz) ** 2, 0))


class Svg:
    def __init__(self, x0, x1, y0, y1, title):
        self.x0, self.y1 = x0, y1
        self.w, self.h = (x1 - x0) * S, (y1 - y0) * S
        self.items = [f'<text x="8" y="18" class="t">{title}</text>']

    def p(self, x, y):
        return (x - self.x0) * S, (self.y1 - y) * S

    def rect(self, xa, ya, xb, yb, cls, label=None):
        (px, py), (qx, qy) = self.p(min(xa, xb), max(ya, yb)), self.p(max(xa, xb), min(ya, yb))
        self.items.append(f'<rect x="{px:.1f}" y="{py:.1f}" width="{qx-px:.1f}" height="{qy-py:.1f}" class="{cls}"/>')
        if label:
            self.text((xa + xb) / 2, max(ya, yb) + 0.15, label)

    def circle(self, x, y, r, cls):
        px, py = self.p(x, y)
        self.items.append(f'<circle cx="{px:.1f}" cy="{py:.1f}" r="{r*S:.1f}" class="{cls}"/>')

    def line(self, pts, cls):
        d = " ".join(f"{a:.1f},{b:.1f}" for a, b in (self.p(x, y) for x, y in pts))
        self.items.append(f'<polyline points="{d}" class="{cls}"/>')

    def arc(self, cx, cy, r, a0, a1, cls, n=24):
        pts = [(cx + r * math.cos(math.radians(a0 + (a1 - a0) * i / n)),
                cy + r * math.sin(math.radians(a0 + (a1 - a0) * i / n))) for i in range(n + 1)]
        self.line(pts, cls)

    def text(self, x, y, s, cls="l"):
        px, py = self.p(x, y)
        self.items.append(f'<text x="{px:.1f}" y="{py:.1f}" class="{cls}">{s}</text>')

    def save(self, name):
        css = """<style>
.t{font:600 15px sans-serif;fill:#222}.l{font:11px sans-serif;fill:#333;text-anchor:middle}
.n{font:11px sans-serif;fill:#a33;text-anchor:start}
.body{fill:#eee;stroke:#999}.rail{fill:none;stroke:#999;stroke-dasharray:2 2}.wheel{fill:#ccc;stroke:#777}.roll{fill:#9c9;stroke:#585}
.taken{fill:#f6d7a7;stroke:#c90;opacity:.8}.new{fill:#cde3fb;stroke:#2a6fb5;stroke-width:1.5}
.lane{fill:none;stroke:#2a6fb5;stroke-width:2.5}.belt{fill:none;stroke:#d33;stroke-width:2}
.thin{fill:none;stroke:#555;stroke-dasharray:4 3}.fov{fill:none;stroke:#a3a;stroke-dasharray:6 3}
.pol{fill:#ffe680;stroke:#b90}.nec{fill:#ffb3c6;stroke:#c36}.tur{fill:#ddd0f5;stroke:#649;opacity:.85}
.path{fill:none;stroke:#2a6fb5;stroke-width:1.5;stroke-dasharray:3 3}.env{fill:none;stroke:#000;stroke-dasharray:8 4}
</style>"""
        body = "\n".join(self.items)
        svg = (f'<svg xmlns="http://www.w3.org/2000/svg" width="{self.w:.0f}" height="{self.h:.0f}" '
               f'viewBox="0 0 {self.w:.0f} {self.h:.0f}"><rect width="100%" height="100%" fill="#fff"/>'
               f'{css}{body}</svg>')
        with open(os.path.join(OUT, name), "w") as f:
            f.write(svg)


def robot_side(s):
    s.rect(BACK, 0.9, FACE, 3.6, "rail")                       # side rails, outboard of the lane
    s.text(-6.0, 3.75, "side rails (outboard)")
    for x in (WHEEL_X, -WHEEL_X):
        s.circle(x, WHEEL_R, WHEEL_R, "wheel")
    s.circle(ROLLER_X, ROLLER_Z, ROLLER_R, "roll")
    s.rect(FACE - 0.11, 2.73, FACE + 1.63, 8.80, "taken", "stowed extractor")
    s.rect(FACE, 4.3, FACE + 1.6, 6.6, "taken")
    s.line([(START_BACK, 0), (START_BACK, 18), (V_TIP_X, 18), (V_TIP_X, 0)], "env")
    s.text(START_BACK + 0.2, 17.4, "R102 18 in at the start", "n")
    s.circle(*LENS, 0.25, "taken")
    s.line([LENS, (V_TIP_X + 1.5, LENS[1] + 0.445 * (V_TIP_X + 1.5 - LENS[0]))], "fov")
    s.text(LENS[0] + 0.3, LENS[1] + 0.5, "Limelight, lowest ray", "n")
    s.line([(-9, 0), (12, 0)], "thin")


def robot_top(s):
    s.rect(BACK, -HALF_W + 1.0, FACE, HALF_W - 1.0, "body")
    for x in (WHEEL_X, -WHEEL_X):
        for y in (WHEEL_Y, -WHEEL_Y):
            s.rect(x - WHEEL_R, y - 0.75, x + WHEEL_R, y + 0.75, "wheel")
    s.rect(ROLLER_X - ROLLER_R, -6.9, ROLLER_X + ROLLER_R, 6.9, "roll")
    for sg in (1, -1):
        s.line([(FACE + 1.4, sg * 7.68), (V_TIP_X, sg * V_TIP_Y)], "lane")
    s.rect(FACE - 0.11, -1.86, FACE + 1.63, 1.86, "taken")
    s.rect(FACE, -4.0, FACE + 1.6, -1.9, "taken")
    s.rect(FACE - 1.0, 2.0, FACE + 0.6, 6.7, "taken")
    s.text(FACE + 0.8, -4.4, "extractor drive")
    s.text(FACE - 0.2, 7.05, "roller motor")
    s.line([(START_BACK, -9), (START_BACK, 9), (V_TIP_X, 9), (V_TIP_X, -9)], "env")


def pieces_side(s, xs):
    for x, r in xs:
        s.circle(x, FLOOR + r, r, "pol" if r < 1.6 else "nec")


def concept_a():
    s = Svg(-9, 12, -0.5, 19.6, "A. J-kicker up the turret axis (recommended): side, from the left")
    robot_side(s)
    # turret bearing and the launcher above it (launcher form is the launcher chat's)
    s.rect(TURRET_X - BORE_R - 0.8, BEARING_Z[0], TURRET_X - BORE_R, BEARING_Z[1], "tur")
    s.rect(TURRET_X + BORE_R, BEARING_Z[0], TURRET_X + BORE_R + 0.8, BEARING_Z[1], "tur")
    s.rect(TURRET_X - 3.0, BEARING_Z[1], TURRET_X + 3.0, 13.0, "thin")
    s.text(TURRET_X, 12.4, "launcher on the turret")
    s.text(TURRET_X, 11.8, "(its throat takes the ball here)")
    s.line([(TURRET_X, 0.2), (TURRET_X, 15.5)], "thin")
    s.text(TURRET_X, 15.8, "turret axis X -3.17")
    # lane
    s.line([(RAMP[0], RAMP[1]), (RAMP[2], RAMP[3]), (J_X, FLOOR)], "lane")
    s.line([(0.55, 5.0), (2.3, 5.0)], "lane")
    s.line([(5.6, FLOOR + 0.05), (-1.0, FLOOR + 0.05)], "belt")
    s.circle(5.6, 0.65, 0.25, "new"); s.circle(-1.0, 0.65, 0.25, "new")
    s.line([(ROLLER_X, ROLLER_Z), (6.0, 4.0)], "belt")        # roller shaft -> countershaft, span constant over the float
    s.line([(6.0, 4.0), (5.6, 0.65)], "belt")                 # countershaft -> lane shaft, fixed
    s.circle(6.0, 4.0, 0.3, "new")
    s.text(2.6, 0.2, "lane floor 0.9 in up, 2 polycord strands, driven off the roller shaft")
    # J
    s.arc(J_X, J_Z, J_OUT, 180, 270, "lane")
    s.line([(J_REAR, J_Z), (J_REAR, BEARING_Z[0])], "lane")
    s.circle(J_X, J_Z, JW_R, "new")
    s.circle(J_X + 0.55, J_Z + 0.95, JW_R, "thin")
    s.line([PIVOT, (J_X, J_Z)], "belt")
    s.circle(*PIVOT, 0.3, "new")
    s.text(PIVOT[0] + 1.6, PIVOT[1] - 0.35, "arm pivot + J motor", "l")
    s.text(J_X + 1.4, J_Z + 2.15, "J-wheel floats up to 1.1 in for NECTAR", "l")
    # pieces: one being kicked, then the queue
    s.circle(TURRET_X - 0.2, 5.6, POLLEN_R, "pol")
    s.line([(TURRET_X - 0.2, 7.2), (TURRET_X - 0.2, 9.4)], "path")
    pieces_side(s, [(lead(NECTAR_R), NECTAR_R), (lead(NECTAR_R) + 3.62, NECTAR_R),
                    (lead(NECTAR_R) + 3.62 + 1.81 + 1.40, POLLEN_R)])
    s.save("a-side.svg")

    t = Svg(-9, 12, -9.5, 9.5, "A. J-kicker up the turret axis: top")
    robot_top(t)
    t.rect(J_X, -LANE_W / 2, 7.2, LANE_W / 2, "new", None)
    t.text(3.0, LANE_W / 2 + 0.2, "lane 4.2 in clear: the magazine")
    for y in (0.5, -0.5):
        t.line([(5.6, y), (-1.0, y)], "belt")
    t.rect(J_REAR, -2.1, J_X + 0.8, 2.1, "new")
    t.rect(PIVOT[0] - 0.3, -2.6, PIVOT[0] + 0.3, 2.6, "new")
    t.rect(-4.0, 2.6, 2.0, 4.6, "new", "J motor (box)")
    t.circle(TURRET_X, 0, BORE_R, "tur")
    t.circle(TURRET_X, 0, 0.15, "tur")
    t.text(TURRET_X, -BORE_R - 0.5, "105 mm bore")
    for x, r in [(lead(POLLEN_R), POLLEN_R), (lead(POLLEN_R) + 2.8, POLLEN_R),
                 (lead(POLLEN_R) + 5.6, POLLEN_R), (lead(POLLEN_R) + 8.4, POLLEN_R)]:
        t.circle(x, 0, r, "pol")
    t.text(3.0, -2.9, "4 POLLEN queued; 3 NECTAR fit")
    t.save("a-top.svg")


def concept_b():
    s = Svg(-9, 12, -0.5, 19.6, "B. Tower on the turret axis: side")
    robot_side(s)
    ax = -2.24
    xr = ax - 1.81
    s.rect(ax - BORE_R - 0.8, BEARING_Z[0], ax - BORE_R, BEARING_Z[1], "tur")
    s.rect(ax + BORE_R, BEARING_Z[0], ax + BORE_R + 0.8, BEARING_Z[1], "tur")
    s.line([(ax, 0.2), (ax, 15.5)], "thin"); s.text(ax, 15.8, "turret axis X -2.24")
    s.line([(RAMP[0], RAMP[1]), (RAMP[2], RAMP[3]), (xr, FLOOR)], "lane")
    s.line([(5.6, FLOOR + 0.05), (xr + 0.3, FLOOR + 0.05)], "belt")
    s.circle(xr - 0.5, 1.6, 0.945, "new"); s.circle(xr - 0.5, 6.2, 0.5, "new")
    s.line([(xr, 1.6), (xr, 6.2)], "belt")
    s.text(xr - 1.1, 3.9, "rear belt", "l")
    s.line([(ax + 1.0, 6.5), (ax + 1.0, 4.7)], "lane")
    s.line([(ax + 1.0, 4.7), (ax + 1.75, 2.9)], "belt")
    s.text(ax + 2.6, 5.4, "sprung front wall + check flap", "n")
    pieces_side(s, [(ax, NECTAR_R), (ax + 3.62, NECTAR_R), (ax + 7.24, NECTAR_R), (ax + 10.86, NECTAR_R)])
    s.save("b-side.svg")


def concept_c():
    s = Svg(-9, 12, -0.5, 19.6, "C. Front feed, limited-arc turret: side")
    robot_side(s)
    s.line([(RAMP[0], RAMP[1]), (RAMP[2], RAMP[3]), (-0.5, FLOOR), (-3.2, 5.5)], "lane")
    s.line([(5.6, FLOOR + 0.05), (-0.5, FLOOR + 0.05)], "belt")
    s.line([(0.3, 3.9), (-2.6, 8.4)], "belt")
    s.text(-0.5, 7.4, "sprung top belt", "n")
    s.rect(-6.0, 6.6, -0.3, 7.8, "tur")
    s.rect(-6.0, 7.8, -0.3, 13.0, "thin")
    s.text(-3.2, 12.4, "launcher, throat faces forward")
    pieces_side(s, [(-0.2, POLLEN_R), (2.6, POLLEN_R), (5.4, POLLEN_R)])
    s.save("c-side.svg")
    t = Svg(-9, 12, -9.5, 9.5, "C. Front feed: top, turret works within about +/-30 deg while feeding")
    robot_top(t)
    t.rect(-0.5, -LANE_W / 2, 7.2, LANE_W / 2, "new")
    t.circle(-3.2, 0, 2.9, "tur")
    for a in (-30, 30):
        t.line([(-3.2, 0), (-3.2 + 5 * math.cos(math.radians(a)), 5 * math.sin(math.radians(a)))], "fov")
    t.save("c-top.svg")
    tb = Svg(-9, 12, -9.5, 9.5, "B. Tower on the turret axis: top")
    robot_top(tb)
    tb.rect(-4.6, -LANE_W / 2, 7.2, LANE_W / 2, "new")
    tb.circle(-2.24, 0, BORE_R, "tur")
    for i in range(4):
        tb.circle(-2.24 + 3.62 * i, 0, NECTAR_R, "nec")
    tb.text(3.0, -2.9, "4 NECTAR fit, but so do 5 POLLEN")
    tb.save("b-top.svg")


if __name__ == "__main__":
    concept_a(); concept_b(); concept_c()
    print(f"J-wheel axle ({J_X:.2f}, {J_Z:.2f}), outer J r {J_OUT:.2f}, rear wall X {J_REAR:.2f}, pivot ({PIVOT[0]:.2f}, {PIVOT[1]:.2f})")
    print(f"lead centre: POLLEN {lead(POLLEN_R):.2f}, NECTAR {lead(NECTAR_R):.2f}; start back limit {START_BACK:.2f}")
    print(f"column centre: POLLEN {J_REAR+POLLEN_R:.2f}, NECTAR {J_REAR+NECTAR_R:.2f}; bore {TURRET_X-BORE_R:.2f}..{TURRET_X+BORE_R:.2f}")
