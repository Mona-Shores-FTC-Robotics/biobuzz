"""Draws the spill-study shapes (BodyShape.SHOWN) as cards to compare at a glance: TeamCode/build/sim-logs/body-shapes.html
(about 2 MB, so not committed; sim-review/body-shapes.png is its screenshot).

    ./gradlew :TeamCode:testDebugUnitTest --tests '*SpillLandingTest*'   # the spill CSV draw.py needs
    python3 tools/spill-window/shapes.py [out.html]
    chromium --headless --hide-scrollbars --window-size=1880,1490 --screenshot=sim-review/body-shapes.png TeamCode/build/sim-logs/body-shapes.html

Dark. Each card has the robot from above in the Visualizer's 2D style (front up; wheels, the orange intake bar,
the heading arrow, flaps and walls in blue), the dashed 18 x 18 in start box (R102) and the 18 x 24 in box it must
fit once the match starts (R105), with its sizes along the edges: the chassis (white), how far flaps or walls stick
out (blue), and the whole robot with everything out (amber); beside it, the robot parked at its closest clean spot on the field over where
an 8 POLLEN spill first lands (draw.py, the white dots the "kept" patch); under both, its numbers from
BodyShapeSpillTest.howCloseCanEachShapePark (200 TIPs each). Keep SHAPES in step with BodyShape.SHOWN and the
README's table when the simulator changes.
"""
import os
import re
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.abspath(os.path.join(HERE, "..", ".."))
OUT = sys.argv[1] if len(sys.argv) > 1 else os.path.join(REPO, "TeamCode/build/sim-logs/body-shapes.html")
CSV = os.path.join(REPO, "TeamCode/build/sim-logs/spill-first-touch-8-pollen.csv")

# name, width, length, wall slide, flap out, flap forward, flap height (None: 4 in), clean nose,
# kept 8 POLLEN / match start, most inside at once (8 POLLEN) and TIPs over 4, the first touches 1 in nearer.
SHAPES = [
    ("plain 18 × 18", 18, 18, 0, 0, 0, None, 35, (17, 14), (0, 0), "36 in: frame touches in 2 TIPs"),
    ("long U: walls 6 in forward", 18, 18, 6, 0, 0, None, 36, (41, 36), (7, 31), "37 in: wall touches in 2 TIPs"),
    ("plain 16 × 16", 16, 16, 0, 0, 0, None, 35, (16, 14), (0, 0), "36 in: frame touches in 1 TIP"),
    ("(a) 16 + flaps 3 out, 2 fwd", 16, 16, 0, 3, 2, None, 36, (24, 21), (5, 6), "37 in: a flap only, 1 TIP"),
    ("(b) 16 + flaps 4 out, 2 fwd", 16, 16, 0, 4, 2, None, 36, (22, 21), (5, 8), "37 in: a flap only, 1 TIP"),
    ("(b) 15 + flaps 4.5 out, 3 fwd", 15, 15, 0, 4.5, 3, None, 37, (27, 24), (6, 24), "38 in: a flap only, 1 TIP"),
    ("(c) 18 wide × 15 long + flaps 3 out, 3 fwd", 18, 15, 0, 3, 3, None, 37, (30, 25), (6, 29), "38 in: a flap only, 1 TIP"),
    ("16 + flaps 1 out, 8 fwd", 16, 16, 0, 1, 8, None, 35, (37, 31), (7, 22), "36 in: a flap only, 1 TIP"),
    ("18 + low ramps 6 fwd, 2.5 in tall", 18, 18, 0, 0, 6, 2.5, 35, (33, 28), (6, 21), "36 in: a ramp only, 1 TIP"),
]
BEST = 41  # the long U's kept, the bar every shape is read against

S = 9.0          # top view, pixels per inch
CANVAS = 34.0    # the top view is 34 x 34 in for every shape, so sizes compare
# Dark theme.
BG, CARD, PANEL, LINE = "#111318", "#1b1e25", "#232731", "#343a46"
TEXT, MUTED, DIM = "#e8eaee", "#9aa1ad", "#cfd5df"
BLUE, ORANGE, RED, AMBER, GREY = "#60a5fa", "#ff8a1f", "#f2545b", "#f5b041", "#7b8494"
CHASSIS, CHASSIS_EDGE, WHEEL, TREAD = "#545b69", "#a3abb9", "#14161b", "#e8c21a"


def fmt(v):
    return f"{v:g}″"


def dim(x1, y1, x2, y2, label, colour=DIM):
    """A dimension line with end ticks and its length in a pill at the middle (pixels)."""
    horiz = abs(y2 - y1) < 1e-6
    t = 6
    ticks = ((x1, y1 - t, x1, y1 + t), (x2, y2 - t, x2, y2 + t)) if horiz else ((x1 - t, y1, x1 + t, y1), (x2 - t, y2, x2 + t, y2))
    o = [f'<line x1="{x1:.1f}" y1="{y1:.1f}" x2="{x2:.1f}" y2="{y2:.1f}" stroke="{colour}" stroke-width="1.4"/>']
    o += [f'<line x1="{a:.1f}" y1="{b:.1f}" x2="{c:.1f}" y2="{d:.1f}" stroke="{colour}" stroke-width="1.4"/>' for a, b, c, d in ticks]
    mx, my = (x1 + x2) / 2, (y1 + y2) / 2
    wpx = 8 + 7.2 * len(label)
    o += [f'<rect x="{mx - wpx / 2:.1f}" y="{my - 9:.1f}" width="{wpx:.1f}" height="18" rx="9" fill="{PANEL}" stroke="{colour}" stroke-width="1"/>',
          f'<text x="{mx:.1f}" y="{my + 4.5:.1f}" font-size="12.5" font-weight="600" fill="{colour}" text-anchor="middle">{label}</text>']
    return o


def top_view(w, l, slide, out, fwd, low):
    """The robot from above, front up, on a 34 x 34 in panel, with its dimensions along the edges."""
    size = CANVAS * S
    cx = size / 2
    reach = max(slide, fwd)
    across, ahead = w + 2 * out, l + reach
    back = size - 4.5 * S                       # room below for the chassis width
    front, nose = back - l * S, back - ahead * S
    half = max(w / 2, w / 2 + out) * S          # widest half, px
    o = [f'<svg width="{size:.0f}" height="{size:.0f}" viewBox="0 0 {size:.0f} {size:.0f}" xmlns="http://www.w3.org/2000/svg" font-family="Helvetica,Arial,sans-serif">',
         f'<rect width="100%" height="100%" fill="{PANEL}"/>']
    # R105, everything out: the 18 x 24 box around the footprint, the way round it fits; R102: the 18 in start box.
    bw, bl = (24, 18) if across > 18 else (18, 24)
    o.append(f'<rect x="{cx - bw / 2 * S:.1f}" y="{back - bl * S:.1f}" width="{bw * S:.1f}" height="{bl * S:.1f}" fill="{AMBER}" fill-opacity="0.06" '
             f'stroke="{AMBER}" stroke-width="1.6" stroke-dasharray="8 6"/>')
    o.append(f'<rect x="{cx - 9 * S:.1f}" y="{back - 18 * S:.1f}" width="{18 * S:.1f}" height="{18 * S:.1f}" fill="none" '
             f'stroke="{GREY}" stroke-width="1.6" stroke-dasharray="4 5"/>')
    # The frame and its four mecanum wheels.
    x0 = cx - w / 2 * S
    o.append(f'<rect x="{x0:.1f}" y="{front:.1f}" width="{w * S:.1f}" height="{l * S:.1f}" rx="3" fill="{CHASSIS}" stroke="{CHASSIS_EDGE}" stroke-width="2.5"/>')
    ww, wl = 1.9 * S, 4.4 * S
    for fx in (-1, 1):
        for fy in (-1, 1):
            wx = cx + fx * (w / 2 - 1.35) * S - ww / 2
            wy = (front + back) / 2 + fy * (l / 2 - 3.1) * S - wl / 2
            o.append(f'<rect x="{wx:.1f}" y="{wy:.1f}" width="{ww:.1f}" height="{wl:.1f}" rx="5" fill="{WHEEL}" stroke="{TREAD}" stroke-width="1.6"/>')
            d = 1 if fx * fy > 0 else -1
            for k in range(4):
                y1 = wy + (k + 0.5) * wl / 4
                o.append(f'<line x1="{wx + 3:.1f}" y1="{y1 + d * 4.5:.1f}" x2="{wx + ww - 3:.1f}" y2="{y1 - d * 4.5:.1f}" stroke="{TREAD}" stroke-width="2.2" stroke-linecap="round"/>')
    # The intake bar across the front, and the heading arrow.
    iw = min(w - 4.5, 14)
    o.append(f'<rect x="{cx - iw / 2 * S:.1f}" y="{front + 0.4 * S:.1f}" width="{iw * S:.1f}" height="{1.3 * S:.1f}" rx="6" fill="{ORANGE}"/>')
    my = (front + back) / 2
    o += [f'<line x1="{cx:.1f}" y1="{my + 2.4 * S:.1f}" x2="{cx:.1f}" y2="{my - 2.7 * S:.1f}" stroke="{RED}" stroke-width="5.5" stroke-linecap="round"/>',
          f'<polyline points="{cx - 1.5 * S:.1f},{my - 1.2 * S:.1f} {cx:.1f},{my - 2.9 * S:.1f} {cx + 1.5 * S:.1f},{my - 1.2 * S:.1f}" fill="none" stroke="{RED}" stroke-width="5.5" stroke-linecap="round" stroke-linejoin="round"/>',
          f'<circle cx="{cx:.1f}" cy="{my:.1f}" r="4.5" fill="{TEXT}"/>']
    # Walls slid forward along the sides, or a flap from each front corner (a low guide dashed).
    dash = ' stroke-dasharray="5 4"' if low else ""
    for side in (-1, 1):
        hx = cx + side * w / 2 * S
        if slide:
            o.append(f'<line x1="{hx - side * 1.5:.1f}" y1="{back:.1f}" x2="{hx - side * 1.5:.1f}" y2="{nose:.1f}" stroke="{BLUE}" stroke-width="5"/>')
        if out or fwd:
            o.append(f'<line x1="{hx:.1f}" y1="{front:.1f}" x2="{hx + side * out * S:.1f}" y2="{nose:.1f}" stroke="{BLUE}" '
                     f'stroke-width="{4 if low else 5.5}" stroke-linecap="round"{dash}/>')
    # Dimensions. Chassis: width below, length on the left.
    left = cx - w / 2 * S
    o += dim(left, back + 2.2 * S, cx + w / 2 * S, back + 2.2 * S, fmt(w))
    o += dim(left - 2.0 * S, back, left - 2.0 * S, front, fmt(l))
    # What sticks out, on the left: the flap's free end out and forward, or the walls' slide.
    if out:
        o += dim(left - out * S, nose - 1.6 * S, left, nose - 1.6 * S, fmt(out), BLUE)
    if reach:
        x = left - out * S - 1.6 * S if out else left - 2.0 * S
        o += dim(x, front, x, nose, fmt(reach), BLUE)
    # Everything out: overall width above, overall length on the right.
    if across != w:
        o += dim(cx - half, nose - 3.6 * S, cx + half, nose - 3.6 * S, fmt(across), AMBER)
    if ahead != l:
        o += dim(cx + half + 2.0 * S, back, cx + half + 2.0 * S, nose, fmt(ahead), AMBER)
    o.append('</svg>')
    return "".join(o), (across, ahead)


def field_view(w, l, slide, out, fwd, nose):
    """draw.py's picture of the robot at its clean spot, cropped to the robot and the spill."""
    face = nose - max(slide, fwd)
    tmp = os.path.join(REPO, "TeamCode/build/sim-logs/_shape.html")
    args = [sys.executable, os.path.join(HERE, "draw.py"), "--face", str(face), "--body", str(w), str(l), "--patch"]
    if slide:
        args += ["--arms", str(slide)]
    if out or fwd:
        args += ["--flaps", str(out), str(fwd)]
    subprocess.run(args + [CSV, tmp], check=True, stdout=subprocess.DEVNULL)
    svg = re.search(r"<svg.*</svg>", open(tmp).read(), re.S).group(0)
    os.remove(tmp)
    k, x0, x1, y0, y1 = 7.0, 40, 77, 10, 54   # draw.py's scale; the crop, Pedro inches
    vb = f'{70 + x0 * k:.0f} {714 - y1 * k:.0f} {(x1 - x0) * k:.0f} {(y1 - y0) * k:.0f}'
    f = CANVAS * S / (y1 - y0)
    svg = re.sub(r'<svg width="[^"]*" height="[^"]*"', f'<svg width="{(x1 - x0) * f:.0f}" height="{(y1 - y0) * f:.0f}" viewBox="{vb}"', svg, 1)
    # The field image once per page (FIELD_IMAGE below), not once per card.
    global FIELD_IMAGE
    image = re.search(r'<image [^>]*/>', svg).group(0)
    if FIELD_IMAGE is None:
        FIELD_IMAGE = image.replace("<image ", '<image id="fieldimg" ', 1)
    return svg.replace(image, '<use href="#fieldimg"/>')


FIELD_IMAGE = None


def bar(label, value, best=BEST):
    pct, ref = 100.0 * value / 50, 100.0 * best / 50   # the scale runs to 50%
    return (f'<div class="row"><span class="lbl">{label}</span><span class="track"><span class="fill" style="width:{pct:.0f}%"></span>'
            f'<span class="ref" style="left:{ref:.0f}%"></span></span><b>{value}%</b></div>')


cards = []
for name, w, l, slide, out, fwd, low, nose, kept, inside, closer in SHAPES:
    svg, (across, ahead) = top_view(w, l, slide, out, fwd, low)
    g407 = f"{inside[0]} at once; over 4 in {inside[1]} of 200 TIPs" if inside[1] else (f"{inside[0]} at once" if inside[0] else "none (no guides)")
    cards.append(
        f'<div class="card"><div class="head"><h2>{name}</h2><span class="park">parks {nose}″ from the wall</span></div>'
        f'<div class="pics">{svg}{field_view(w, l, slide, out, fwd, nose)}</div>'
        f'{bar("kept · 8 POLLEN", kept[0])}{bar("kept · match start", kept[1])}'
        f'<div class="facts"><span>G409</span>0 of 200 TIPs here · 1″ nearer, {closer}</div>'
        f'<div class="facts"><span>G407</span>inside the guides: {g407}</div></div>')

html = f"""<!doctype html><html><head><meta charset="utf-8"><meta name="color-scheme" content="dark"><title>Robot shapes and the spill</title>
<style>
body{{margin:0;padding:22px;background:{BG};font-family:Helvetica,Arial,sans-serif;color:{TEXT}}}
h1{{font-size:21px;margin:0 0 6px}} p.sub{{margin:0 0 10px;color:{MUTED};font-size:13px;max-width:1500px;line-height:1.45}}
.legend{{display:flex;flex-wrap:wrap;gap:18px;font-size:12.5px;color:{MUTED};margin:0 0 16px}}
.legend i{{display:inline-block;width:22px;height:0;vertical-align:middle;margin-right:6px}}
.grid{{display:grid;grid-template-columns:repeat(3,auto);gap:14px;justify-content:start}}
.card{{background:{CARD};border:1px solid {LINE};border-radius:12px;padding:12px 14px 12px}}
.head{{display:flex;justify-content:space-between;align-items:baseline;gap:12px;margin-bottom:8px}}
.card h2{{font-size:15px;margin:0}} .park{{font-size:12px;color:{MUTED};white-space:nowrap}}
.pics{{display:flex;gap:8px;align-items:center}} .pics svg{{border-radius:8px}}
.row{{display:flex;align-items:center;gap:8px;font-size:12px;margin-top:7px}} .lbl{{width:120px;color:{MUTED}}}
.track{{position:relative;flex:1;height:10px;background:{PANEL};border-radius:5px}}
.fill{{position:absolute;left:0;top:0;bottom:0;background:{BLUE};border-radius:5px}}
.ref{{position:absolute;top:-4px;bottom:-4px;width:2px;background:{TEXT}}}
.row b{{width:34px;text-align:right}}
.facts{{font-size:12px;color:{MUTED};margin-top:6px}} .facts span{{display:inline-block;width:44px;color:{TEXT};font-weight:600}}
</style></head><body>
<h1>Which robot shape keeps a TIP's spill?</h1>
<p class="sub">Each robot parked as near the HIVE as it can with no G409 touch in 200 TIPs. Left: from above, front up, inches.
Right: the same robot on the field over where an 8 POLLEN spill first lands; the white dots are the floor counted as kept 3 s later.
Bars run to 50%; the white line is the long U's 41%.</p>
<div class="legend"><span><i style="border-top:2px dashed {GREY}"></i>18 × 18 start box (R102)</span>
<span><i style="border-top:2px dashed {AMBER}"></i>18 × 24 limit after the start (R105), and the size fully out</span>
<span><i style="border-top:2px solid {DIM}"></i>chassis</span><span><i style="border-top:3px solid {BLUE}"></i>flaps / walls, and how far they stick out</span>
<span><i style="border-top:3px dashed {BLUE}"></i>low guide (2.5″)</span></div>
<svg width="0" height="0" style="position:absolute"><defs>{FIELD_IMAGE}</defs></svg>
<div class="grid">{"".join(cards)}</div></body></html>"""
open(OUT, "w").write(html)
print(f"wrote {OUT}: {len(cards)} shapes")
