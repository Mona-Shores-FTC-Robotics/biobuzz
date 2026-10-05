"""Dark sheets for the spill-study shapes, from the simulator's numbers:

  sim-review/body-shapes.png        each shape from above with its sizes, and on the field over where an 8 POLLEN
                                    spill lands: centred on the 90% box, chassis face halfway between its 100% and
                                    90% lines
  sim-review/body-shapes-ideas.png  more shapes from the 5 Oct review (a front C, a turned robot ...), pictures only
  sim-review/body-shapes-shortlist.png  the best of them, each with its numbers
  sim-review/body-shapes-loads.png  8 POLLEN against the match-start load: where the spill lands, where it lies
                                    3 s later, and what each shape keeps from each

    ./gradlew :TeamCode:testDebugUnitTest --tests '*SpillLandingTest*' --tests '*BodyShapeSpillTest.atTheLandingLine' --tests '*BodyShapeSpillTest.rightHookAtTheSpill'
    python3 tools/spill-window/shapes.py
    chromium --headless --hide-scrollbars --window-size=2500,1000 --screenshot=sim-review/body-shapes.png TeamCode/build/sim-logs/body-shapes.html
    chromium --headless --hide-scrollbars --window-size=2400,1000 --screenshot=sim-review/body-shapes-ideas.png TeamCode/build/sim-logs/body-shapes-ideas.html
    chromium --headless --hide-scrollbars --window-size=2900,1300 --screenshot=sim-review/body-shapes-shortlist.png TeamCode/build/sim-logs/body-shapes-shortlist.html
    chromium --headless --hide-scrollbars --window-size=1500,1250 --screenshot=sim-review/body-shapes-loads.png TeamCode/build/sim-logs/body-shapes-loads.html

The HTML (about 2 MB each, every spilled piece drawn) stays in the build folder. The top views are the Visualizer's
2D style, front up, with the chassis (white), how far flaps or walls stick out (blue) and the whole robot with
everything out (amber) along the edges; the amber dashed box is the 18 x 24 in limit after the start (R105). The field views are draw.py's.
"""
import csv as csvlib
import os
import re
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.abspath(os.path.join(HERE, "..", ".."))
LOGS = os.path.join(REPO, "TeamCode/build/sim-logs")
CSV = os.path.join(LOGS, "spill-first-touch-8-pollen.csv")
CSV_START = os.path.join(LOGS, "spill-first-touch.csv")
RESULTS = os.path.join(LOGS, "body-shapes-landing.csv")

# Number and name, the spec line, BodyShape name (the results' key), width, length, wall slide, flap out, flap forward,
# low guide, smaller chassis to outline.
SHAPES = [
    ("1 · Plain", "18 × 18 chassis", "plain 18", 18, 18, 0, 0, 0, False, ()),
    ("2 · Long U", "18 × 18 chassis · walls slide 6″ forward", "long U", 18, 18, 6, 0, 0, False, ()),
    ("3 · Funnel", "16 × 16 chassis · flaps 3″ out, 2″ forward", "16 + flaps 3 out 2 fwd", 16, 16, 0, 3, 2, False, ()),
    ("4 · Wide funnel", "16 × 16 chassis · flaps 4″ out, 2″ forward", "16 + flaps 4 out 2 fwd", 16, 16, 0, 4, 2, False, ()),
    ("5 · Wide, deep funnel", "15 × 15 chassis · flaps 4.5″ out, 3″ forward", "15 + flaps 4.5 out 3 fwd", 15, 15, 0, 4.5, 3, False, ()),
    ("6 · Short-chassis funnel", "18 wide × 15 long chassis · flaps 3″ out, 3″ forward", "18 wide x 15 long + flaps 3 out 3 fwd",
     18, 15, 0, 3, 3, False, ()),
    ("7 · Long funnel", "16 × 16 chassis · flaps 1″ out, 8″ forward", "16 + flaps 1 out 8 fwd", 16, 16, 0, 1, 8, False, ()),
]
FACES = [(35.0, "on the 100% line"), (36.5, "halfway"), (38.0, "on the 90% line")]
TIPS = 200

results = {}
for row in csvlib.reader(l for l in open(RESULTS) if not l.startswith("#")):
    results[(row[0], float(row[1]), int(row[2]))] = [float(v) for v in row[3:]]

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


def top_view(w, l, slide, out, fwd, low, outlines=(), crossbeam=False, sides=(-1, 1)):
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
    # R105, everything out: the 18 x 24 box around the footprint, the way round it fits.
    bw, bl = (24, 18) if across > 18 else (18, 24)
    o.append(f'<rect x="{cx - bw / 2 * S:.1f}" y="{back - bl * S:.1f}" width="{bw * S:.1f}" height="{bl * S:.1f}" fill="{AMBER}" fill-opacity="0.06" '
             f'stroke="{AMBER}" stroke-width="1.6" stroke-dasharray="8 6"/>')
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
    # Smaller chassis drawn over it, sharing the front face (the plain card: 16 and 15 in).
    for k, size in enumerate(outlines):
        o.append(f'<rect x="{cx - size / 2 * S:.1f}" y="{front:.1f}" width="{size * S:.1f}" height="{size * S:.1f}" fill="none" '
                 f'stroke="{TEXT}" stroke-width="1.6" stroke-dasharray="6 4"/>')
        # Labels in opposite bottom corners, so two sizes an inch apart don't overlap.
        x, anchor = (cx - size / 2 * S + 5, "start") if k % 2 == 0 else (cx + size / 2 * S - 5, "end")
        o.append(f'<text x="{x:.1f}" y="{front + size * S - 6:.1f}" font-size="12" font-weight="600" fill="{TEXT}" text-anchor="{anchor}">{fmt(size)}</text>')
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
        if (out or fwd) and side in sides:
            o.append(f'<line x1="{hx:.1f}" y1="{front:.1f}" x2="{hx + side * out * S:.1f}" y2="{nose:.1f}" stroke="{BLUE}" '
                     f'stroke-width="{4 if low else 5.5}" stroke-linecap="round"{dash}/>')
    if crossbeam:  # a beam joining the front ends of the walls or flaps
        half_out = (w / 2 + out) * S
        o.append(f'<line x1="{cx - half_out:.1f}" y1="{nose:.1f}" x2="{cx + half_out:.1f}" y2="{nose:.1f}" stroke="{BLUE}" '
                 f'stroke-width="5.5" stroke-linecap="round"/>')
    # Dimensions. Chassis: width below, length on the left.
    left = cx - w / 2 * S
    o += dim(left, back + 2.2 * S, cx + w / 2 * S, back + 2.2 * S, fmt(w))
    o += dim(left - 2.0 * S, back, left - 2.0 * S, front, fmt(l))
    # What sticks out, on the left: the flap's free end out and forward, or the walls' slide.
    if out:
        o += dim(left - out * S, nose - 1.6 * S, left, nose - 1.6 * S, fmt(out), BLUE)
    if reach:
        if -1 in sides or slide:
            x = left - out * S - 1.6 * S if out else left - 2.0 * S
        else:  # only a right arm: its length beside it
            x = cx + (w / 2 + out) * S + 2.0 * S
        o += dim(x, front, x, nose, fmt(reach), BLUE)
    # Everything out: overall width above, overall length on the right.
    if across != w:
        o += dim(cx - half, nose - 3.6 * S, cx + half, nose - 3.6 * S, fmt(across), AMBER)
    if ahead != l:
        x = cx + half + (4.2 if -1 not in sides else 2.0) * S   # outside a right arm's own length
        o += dim(x, back, x, nose, fmt(ahead), AMBER)
    o.append('</svg>')
    return "".join(o), (across, ahead)


def field_view(w, l, slide, out, fwd, face, csv=None, extra=(), crop=(40, 77, 10, 54), height=None):
    """draw.py's picture of the robot, its front face {face} in from the wall, cropped to the robot and the spill."""
    tmp = os.path.join(REPO, "TeamCode/build/sim-logs/_shape.html")
    args = [sys.executable, os.path.join(HERE, "draw.py"), "--face", str(face), "--body", str(w), str(l), *extra]
    if slide:
        args += ["--arms", str(slide)]
    if out or fwd:
        args += ["--flaps", str(out), str(fwd)]
    subprocess.run(args + [csv or CSV, tmp], check=True, stdout=subprocess.DEVNULL)
    svg = re.search(r"<svg.*</svg>", open(tmp).read(), re.S).group(0)
    os.remove(tmp)
    k, (x0, x1, y0, y1) = 7.0, crop   # draw.py's scale; the crop, Pedro inches
    vb = f'{70 + x0 * k:.0f} {714 - y1 * k:.0f} {(x1 - x0) * k:.0f} {(y1 - y0) * k:.0f}'
    f = (height or CANVAS * S) / (y1 - y0)
    svg = re.sub(r'<svg width="[^"]*" height="[^"]*"', f'<svg width="{(x1 - x0) * f:.0f}" height="{(y1 - y0) * f:.0f}" viewBox="{vb}"', svg, 1)
    # The field image once per page (FIELD_IMAGE below), not once per card.
    global FIELD_IMAGE
    image = re.search(r'<image [^>]*/>', svg).group(0)
    if FIELD_IMAGE is None:
        FIELD_IMAGE = image.replace("<image ", '<image id="fieldimg" ', 1)
    return svg.replace(image, '<use href="#fieldimg"/>')


FIELD_IMAGE = None



def landing_lines(path):
    """The spill's near edges (all footprints, 90% of them) and the 90% box's centre across, as draw.py draws them."""
    rows = [list(map(float, l.split(","))) for l in open(path) if l.strip() and not l.startswith("#")]
    q = lambda v, p: sorted(v)[min(len(v) - 1, int(p * len(v)))]
    rad = max(r[7] for r in rows)
    ys, xs = [r[5] for r in rows], [r[6] for r in rows]
    line_100 = min(r[5] - r[7] for r in rows)
    line_90 = q(ys, .05) - rad
    return line_100, line_90, (q(xs, .05) + q(xs, .95)) / 2


# Every robot parks the same way (mentor, 5 Oct 2026): centred across on the 90% box, chassis face halfway between
# the 100% and 90% lines, its walls or flaps reaching into the spill.
LINE_100, LINE_90, BOX_X = landing_lines(CSV)
PARK_FACE = (LINE_100 + LINE_90) / 2


STYLE = f"""<style>
body{{margin:0;padding:22px;background:{BG};font-family:Helvetica,Arial,sans-serif;color:{TEXT}}}
h1{{font-size:24px;margin:0 0 8px}} p.sub{{margin:0 0 10px;color:{MUTED};font-size:13px;max-width:1560px;line-height:1.5}}
p.sub b{{color:{TEXT}}}
.legend{{display:flex;flex-wrap:wrap;gap:22px;font-size:13.5px;color:{MUTED};margin:0 0 16px}}
.legend i{{display:inline-block;width:30px;height:0;vertical-align:middle;margin-right:6px}}
.grid{{display:grid;grid-template-columns:repeat(3,auto);gap:14px;justify-content:start}} .grid.four{{grid-template-columns:repeat(4,auto)}} .grid.two{{grid-template-columns:repeat(2,auto)}}
.row-head{{font-size:15px;color:{MUTED};font-weight:600;margin:18px 0 10px}}
.nums td{{font-size:13px}} .nums b{{font-size:16px}} .nums th{{text-align:right}}
.card{{background:{CARD};border:1px solid {LINE};border-radius:12px;padding:12px 14px}}
.card h2{{font-size:16px;margin:0}} .head{{display:flex;align-items:baseline;gap:12px;margin-bottom:9px}} .spec{{font-size:13px;color:{MUTED}}}
.pics{{display:flex;gap:8px;align-items:center}} .pics svg{{border-radius:8px}}
table{{border-collapse:collapse;width:100%;margin-top:9px;font-size:12px}}
th{{color:{MUTED};font-weight:500;text-align:right;padding:2px 6px;border-bottom:1px solid {LINE}}}
th:first-child,td.where{{text-align:left}}
td{{text-align:right;padding:3px 6px;color:{MUTED}}} td b{{color:{TEXT};font-size:13px}}
td.bad{{color:{RED};font-weight:700}} td.good{{color:#4ade80;font-weight:700}} td.warn{{color:{AMBER};font-weight:700}} td.ok{{color:{MUTED}}}
.panel{{background:{CARD};border:1px solid {LINE};border-radius:12px;padding:12px 14px}}
.panel h2{{font-size:16px;margin:0 0 4px}} .panel h3{{font-size:13px;margin:10px 0 6px;color:{MUTED};font-weight:500}}
.stats{{font-size:12.5px;color:{MUTED};margin-top:6px}} .stats b{{color:{TEXT}}}
.cols{{display:grid;grid-template-columns:repeat(2,auto);gap:14px;justify-content:start}}
</style>"""

cards = []
for title, spec, key, w, l, slide, out, fwd, low, outlines in SHAPES:
    svg, _ = top_view(w, l, slide, out, fwd, low, outlines)
    field = field_view(w, l, slide, out, fwd, round(PARK_FACE, 1), extra=["--x", f"{BOX_X:.1f}"])
    cards.append(f'<div class="card"><div class="head"><h2>{title}</h2><span class="spec">{spec}</span></div>'
                 f'<div class="pics">{svg}{field}</div></div>')

page = f"""<!doctype html><html><head><meta charset="utf-8"><meta name="color-scheme" content="dark"><title>Robot shapes at the spill</title>{STYLE}</head><body>
<h1>Robot Shapes at the Spill</h1>
<div class="legend"><span><i style="border-top:2px dashed {RED}"></i>every spilled piece lands inside</span>
<span><i style="border-top:2px solid {RED}"></i>90% land inside</span></div>
<svg width="0" height="0" style="position:absolute"><defs>{{FIELD}}</defs></svg>
<div class="grid four">{"".join(cards)}</div></body></html>"""
open(os.path.join(LOGS, "body-shapes.html"), "w").write(page.replace("{FIELD}", FIELD_IMAGE))


# Sheet 3: ideas from the 5 Oct review, drawn before any simulation, to discard by eye.
rows8 = [list(map(float, l.split(","))) for l in open(CSV) if l.strip() and not l.startswith("#")]
LEFT_100 = min(r[6] - r[7] for r in rows8)                       # the spill's left edge, every piece
q = lambda v, p: sorted(v)[min(len(v) - 1, int(p * len(v)))]
rad = max(r[7] for r in rows8)
BOX_Y = (q([r[5] for r in rows8], .05) + q([r[5] for r in rows8], .95)) / 2  # the 90% box's centre, out from the wall
# The right "95%" line: halfway between the right edges of the 100% and 90% boxes.
RIGHT_95 = (max(r[6] + r[7] for r in rows8) + q([r[6] for r in rows8], .95) + rad) / 2
# Number and name, spec line, width, length, wall slide, flap out, flap forward, crossbeam, (x, y, heading) on the field.
IDEAS = [
    ("8 · Front C", "18 wide × 12 long chassis · arms 12″ forward + crossbeam, pivoted down before the TIP",
     18, 12, 0, 0, 12, True, (BOX_X, LINE_100 - 6, 0)),
    ("9 · Front U", "18 wide × 12 long chassis · arms 12″ forward, no crossbeam",
     18, 12, 0, 0, 12, False, (BOX_X, LINE_100 - 6, 0)),
    ("10 · Long U + crossbeam", "18 × 18 chassis · walls slide 6″ forward, joined by a crossbeam",
     18, 18, 6, 0, 0, True, (BOX_X, PARK_FACE - 9, 0)),
    ("11 · Side-on Long U", "18 × 18 chassis · walls 6″ forward · facing along the spill, from its left end",
     18, 18, 6, 0, 0, False, (LEFT_100 - 9, BOX_Y, -90)),
    ("12 · Long U turned 20°", "18 × 18 chassis · walls 6″ forward · turned 20° counterclockwise",
     18, 18, 6, 0, 0, False, (BOX_X, PARK_FACE - 9, 20)),
    ("13 · Right hook", "18 wide × 14 long chassis · one 10″ arm on the right + crossbeam, pivoted down before the TIP",
     18, 14, 0, 0, 10, "right", (RIGHT_95 - 9, PARK_FACE - 7, 0)),
]
cards3 = []
for title, spec, w, l, slide, out, fwd, beam, (x, y, heading) in IDEAS:
    sides = (1,) if beam == "right" else (-1, 1)
    svg, _ = top_view(w, l, slide, out, fwd, False, (), bool(beam), sides)
    extra = ["--x", f"{x:.1f}", "--y", f"{y:.1f}", "--heading", f"{heading:g}"] + (["--crossbeam"] if beam else []) \
        + (["--right-only"] if beam == "right" else [])
    field = field_view(w, l, slide, out, fwd, 0, extra=extra, crop=(22, 77, 10, 54))
    cards3.append(f'<div class="card"><div class="head"><h2>{title}</h2><span class="spec">{spec}</span></div>'
                  f'<div class="pics">{svg}{field}</div></div>')
ideas = f"""<!doctype html><html><head><meta charset="utf-8"><meta name="color-scheme" content="dark"><title>Robot shape ideas</title>{STYLE}</head><body>
<h1>Robot Shapes at the Spill: New Ideas</h1>
<div class="legend"><span><i style="border-top:2px dashed {RED}"></i>every spilled piece lands inside</span>
<span><i style="border-top:2px solid {RED}"></i>90% land inside</span></div>
<svg width="0" height="0" style="position:absolute"><defs>{{FIELD}}</defs></svg>
<div class="grid">{"".join(cards3)}</div></body></html>"""
open(os.path.join(LOGS, "body-shapes-ideas.html"), "w").write(ideas.replace("{FIELD}", FIELD_IMAGE))


# Sheet 4: the shortlist, each card with its own numbers (BodyShapeSpillTest.rightHookAtTheSpill, the same spots).
short = {}
for row in csvlib.reader(l for l in open(os.path.join(LOGS, "body-shapes-shortlist.csv")) if not l.startswith("#")):
    short[(row[0], round(float(row[1]), 1), int(row[3]))] = [float(v) for v in row[4:]]
HOOK_X = 61.6  # the right hook on the match-start spill's right 95% line (it lands 2 in right of the 8 POLLEN one)
# Rows: the baseline and the designs nothing lands on, then the ones that reach into the spill.
SHORTLIST = [
    ("Nothing lands on these", [
        ("1 · Plain", "18 × 18 chassis", "plain 18", 18, 18, 0, 0, 0, False, (BOX_X, PARK_FACE - 9)),
        ("3 · Funnel", "16 × 16 chassis · flaps 3″ out, 2″ forward", "16 + flaps 3 out 2 fwd", 16, 16, 0, 3, 2, False,
         (BOX_X, PARK_FACE - 8)),
        ("6 · Short-chassis funnel", "18 wide × 15 long chassis · flaps 3″ out, 3″ forward", "18 wide x 15 long + flaps 3 out 3 fwd",
         18, 15, 0, 3, 3, False, (BOX_X, PARK_FACE - 7.5)),
        ("13 · Right hook", "18 wide × 14 long chassis · one 10″ arm on the right + crossbeam", "right hook 18x14, arm 10",
         18, 14, 0, 0, 10, "right", (HOOK_X, PARK_FACE - 7)),
    ]),
    ("These reach into the spill: they keep more, but pieces land on them most TIPs", [
        ("2 · Long U", "18 × 18 chassis · walls slide 6″ forward", "long U", 18, 18, 6, 0, 0, False, (BOX_X, PARK_FACE - 9)),
        ("8 · Front C", "18 wide × 12 long chassis · arms 12″ forward + crossbeam", "front C 18x12, arms 12",
         18, 12, 0, 0, 12, True, (BOX_X, 35.0 - 6)),
    ]),
]


def touch_class(n):
    return "good" if n < 30 else "warn" if n < 100 else "bad"


rows4 = []
for heading, designs in SHORTLIST:
    cards4 = []
    for title, spec, key, w, l, slide, out, fwd, beam, (x, y) in designs:
        sides = (1,) if beam == "right" else (-1, 1)
        svg, _ = top_view(w, l, slide, out, fwd, False, (), bool(beam), sides)
        extra = ["--x", f"{x:.1f}", "--y", f"{y:.1f}"] + (["--crossbeam"] if beam else []) + (["--right-only"] if beam == "right" else [])
        field = field_view(w, l, slide, out, fwd, 0, extra=extra, crop=(36, 81, 10, 54))
        p8, ps = short[(key, round(x, 1), 0)], short[(key, round(x, 1), 3)]
        nums = (f'<table class="nums"><tr><th></th><th>8 POLLEN</th><th>match start</th></tr>'
                f'<tr><td class="where">kept a TIP</td><td><b>{p8[0]:.1f}</b> of {p8[1]:.0f}</td><td><b>{ps[0]:.1f}</b> of {ps[1]:.0f}</td></tr>'
                f'<tr><td class="where">TIPs it is touched</td><td class="{touch_class(p8[2])}">{p8[2]:.0f} of {TIPS}</td>'
                f'<td class="{touch_class(ps[2])}">{ps[2]:.0f} of {TIPS}</td></tr></table>')
        cards4.append(f'<div class="card"><div class="head"><h2>{title}</h2><span class="spec">{spec}</span></div>'
                      f'<div class="pics">{svg}{field}</div>{nums}</div>')
    rows4.append(f'<h3 class="row-head">{heading}</h3><div class="grid four">{"".join(cards4)}</div>')
shortlist = f"""<!doctype html><html><head><meta charset="utf-8"><meta name="color-scheme" content="dark"><title>Robot shape shortlist</title>{STYLE}</head><body>
<h1>Robot Shapes at the Spill: Best Of</h1>
<div class="legend"><span><i style="border-top:2px dashed {RED}"></i>every spilled piece lands inside</span>
<span><i style="border-top:2px solid {RED}"></i>90% land inside</span>
<span><b style="color:{TEXT}">kept</b>: pieces lying in front of the robot 3 s after the TIP</span>
<span><b style="color:{TEXT}">touched</b>: a falling piece hits the robot before the floor (G409)</span></div>
<svg width="0" height="0" style="position:absolute"><defs>{{FIELD}}</defs></svg>
{"".join(rows4)}</body></html>"""
open(os.path.join(LOGS, "body-shapes-shortlist.html"), "w").write(shortlist.replace("{FIELD}", FIELD_IMAGE))


# Sheet 2: the two loads.
def spill_stats(path):
    rows = [list(map(float, l.split(","))) for l in open(path) if l.strip() and not l.startswith("#")]
    nectar = sum(1 for r in rows if r[7] > 1.6)
    rolls = sorted(((r[3] - r[5]) ** 2 + (r[4] - r[6]) ** 2) ** 0.5 for r in rows)  # first landing to rest
    return len(rows) / TIPS, nectar / TIPS, (len(rows) - nectar) / TIPS, rolls[len(rolls) // 2]


panels = []
for name, path, nectar in (("8 POLLEN", CSV, 0), ("Match start: 3 NECTAR, then POLLEN until it tips", CSV_START, 3)):
    total, nec, pol, near = spill_stats(path)
    view = lambda extra: field_view(18, 18, 0, 0, 0, 35, csv=path, extra=["--kinds", "--no-robot", *extra], crop=(0, 141.5, 0, 78), height=360)
    panels.append(f'<div class="panel"><h2>{name}</h2>'
                  f'<div class="stats"><b>{total:.1f}</b> pieces spill a TIP: <b style="color:#ff5a5a">{nec:.1f} NECTAR</b>, '
                  f'<b style="color:#ffc247">{pol:.1f} POLLEN</b>. With no robot there, half of them roll more than <b>{near:.0f}″</b> from where they land.</div>'
                  f'<h3>Where each piece first lands (200 TIPs)</h3>{view([])}'
                  f'<h3>Where it lies 3 s later, no robot</h3>{view(["--rest"])}</div>')
rows = []
for title, spec, key, *_ in SHAPES:
    cells = []
    for nectar in (0, 3):
        kept_pct, kept, pieces, g409, chassis, guides, most, over = results[(key, 35.0, nectar)]
        cells.append(f'<td><b>{kept:.1f}</b> of {pieces:.0f}</td><td class="{"bad" if chassis else "ok"}">{chassis:.0f}</td>'
                     f'<td class="{"warn" if guides else "ok"}">{guides:.0f}</td>')
    rows.append(f'<tr><td class="where">{title}: {spec}</td>{"".join(cells)}</tr>')
compare = ('<div class="panel"><h2>Each shape, chassis face on the 100% line (35″)</h2><table>'
           '<tr><th></th><th colspan="3" style="text-align:center">8 POLLEN</th><th colspan="3" style="text-align:center">match start</th></tr>'
           '<tr><th>shape</th><th>kept a TIP</th><th>G409 chassis</th><th>G409 guides only</th><th>kept a TIP</th><th>G409 chassis</th><th>G409 guides only</th></tr>'
           f'{"".join(rows)}</table></div>')
loads = f"""<!doctype html><html><head><meta charset="utf-8"><meta name="color-scheme" content="dark"><title>The two spills</title>{STYLE}</head><body>
<h1>8 POLLEN against the match-start load</h1>
<p class="sub">Our CELL tips either from 8 POLLEN, or, at the start of a match, from its 3 NECTAR plus the POLLEN that tips it. In the match-start
spill the NECTAR (red) lands in the left part of the box and the POLLEN (amber) in the right. Dashed red box: every first landing; solid: 90%. Field: the near half, our alliance wall
at the bottom. The table counts as the shape sheet does, for 200 TIPs of each load.</p>
<svg width="0" height="0" style="position:absolute"><defs>{{FIELD}}</defs></svg>
<div class="cols">{"".join(panels)}</div><div style="height:14px"></div>{compare}</body></html>"""
open(os.path.join(LOGS, "body-shapes-loads.html"), "w").write(loads.replace("{FIELD}", FIELD_IMAGE))
print("wrote body-shapes.html and body-shapes-loads.html in", LOGS)
