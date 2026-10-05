"""Draws the spill-study shapes (BodyShape.SHOWN) as cards to compare at a glance: TeamCode/build/sim-logs/body-shapes.html
(about 2 MB, so not committed; sim-review/body-shapes.png is its screenshot).

    ./gradlew :TeamCode:testDebugUnitTest --tests '*SpillLandingTest*'   # the spill CSV draw.py needs
    python3 tools/spill-window/shapes.py [out.html]
    chromium --headless --hide-scrollbars --window-size=1640,1370 --screenshot=sim-review/body-shapes.png TeamCode/build/sim-logs/body-shapes.html

Each card has the robot from above in the Visualizer's 2D style (front up; wheels, the orange intake bar, the
heading arrow, flaps and walls in blue), the dashed 18 x 18 in start box (R102) and the 18 x 24 in box it must
fit once the match starts (R105); beside it, the robot parked at its closest clean spot on the field over where
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

S = 7.5  # card pixels per inch
INK, MUTED, BLUE, ORANGE, RED = "#1f2328", "#6b7280", "#3b82f6", "#ff7a00", "#d62839"


def top_view(w, l, slide, out, fwd, low):
    """The robot from above, front up, in a 30 x 30 in box: the Visualizer's robot style."""
    size = 30 * S
    cx, back = size / 2, size / 2 + 9 * S          # body centred across; its back edge 9 in below the centre
    front = back - l * S
    reach = max(slide, fwd)
    o = [f'<svg width="{size:.0f}" height="{size:.0f}" viewBox="0 0 {size:.0f} {size:.0f}" xmlns="http://www.w3.org/2000/svg">',
         f'<rect width="100%" height="100%" fill="#ffffff"/>']
    # R105 (once the match starts): the 18 x 24 box around the footprint, the way round it fits.
    across, ahead = w + 2 * out, l + reach
    bw, bl = (24, 18) if across > 18 else (18, 24)
    o.append(f'<rect x="{cx - bw / 2 * S:.1f}" y="{back - bl * S:.1f}" width="{bw * S:.1f}" height="{bl * S:.1f}" fill="none" '
             f'stroke="#f59e0b" stroke-width="2" stroke-dasharray="9 6"/>')
    # R102: the 18 in start box, flaps folded.
    o.append(f'<rect x="{cx - 9 * S:.1f}" y="{back - 18 * S:.1f}" width="{18 * S:.1f}" height="{18 * S:.1f}" fill="none" '
             f'stroke="#c7cbd1" stroke-width="2" stroke-dasharray="6 6"/>')
    # The frame and its four mecanum wheels.
    x0 = cx - w / 2 * S
    o.append(f'<rect x="{x0:.1f}" y="{front:.1f}" width="{w * S:.1f}" height="{l * S:.1f}" fill="#d0d3da" stroke="#5f6670" stroke-width="3"/>')
    ww, wl = 1.9 * S, 4.6 * S
    for fx in (-1, 1):
        for fy in (-1, 1):
            wx = cx + fx * (w / 2 - 1.35) * S - ww / 2
            wy = (front + back) / 2 + fy * (l / 2 - 3.2) * S - wl / 2
            o.append(f'<rect x="{wx:.1f}" y="{wy:.1f}" width="{ww:.1f}" height="{wl:.1f}" rx="5" fill="#1f2328" stroke="#f2c200" stroke-width="2"/>')
            for k in range(4):
                y1 = wy + (k + 0.5) * wl / 4
                d = 1 if fx * fy > 0 else -1
                o.append(f'<line x1="{wx + 3:.1f}" y1="{y1 + d * 5:.1f}" x2="{wx + ww - 3:.1f}" y2="{y1 - d * 5:.1f}" stroke="#f2c200" stroke-width="2.4" stroke-linecap="round"/>')
    # The intake bar across the front, and the heading arrow.
    iw = min(w - 4.5, 14)
    o.append(f'<rect x="{cx - iw / 2 * S:.1f}" y="{front + 0.4 * S:.1f}" width="{iw * S:.1f}" height="{1.3 * S:.1f}" rx="6" fill="{ORANGE}" stroke="#7a3b00" stroke-width="1.5"/>')
    my = (front + back) / 2
    o += [f'<line x1="{cx:.1f}" y1="{my + 2.5 * S:.1f}" x2="{cx:.1f}" y2="{my - 2.8 * S:.1f}" stroke="{RED}" stroke-width="6" stroke-linecap="round"/>',
          f'<polyline points="{cx - 1.6 * S:.1f},{my - 1.2 * S:.1f} {cx:.1f},{my - 3 * S:.1f} {cx + 1.6 * S:.1f},{my - 1.2 * S:.1f}" fill="none" stroke="{RED}" stroke-width="6" stroke-linecap="round" stroke-linejoin="round"/>',
          f'<circle cx="{cx:.1f}" cy="{my:.1f}" r="5" fill="{INK}"/>']
    # Walls slid forward along the sides, or a flap from each front corner.
    width_px = 4 if low else 6
    dash = ' stroke-dasharray="5 4"' if low else ""   # a low guide: dashed
    for side in (-1, 1):
        hx = cx + side * w / 2 * S
        if slide:
            o.append(f'<line x1="{hx - side * 1.5:.1f}" y1="{back:.1f}" x2="{hx - side * 1.5:.1f}" y2="{back - (l + slide) * S:.1f}" stroke="{BLUE}" stroke-width="5"/>')
        if out or fwd:
            o.append(f'<line x1="{hx:.1f}" y1="{front:.1f}" x2="{hx + side * out * S:.1f}" y2="{front - fwd * S:.1f}" stroke="{BLUE}" '
                     f'stroke-width="{width_px}" stroke-linecap="round"{dash}/>')
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
    k, x0, x1, y0, y1 = 7.0, 31, 85, 10, 54   # draw.py's scale; the crop, Pedro inches
    vb = f'{70 + x0 * k:.0f} {714 - y1 * k:.0f} {(x1 - x0) * k:.0f} {(y1 - y0) * k:.0f}'
    svg = re.sub(r'<svg width="[^"]*" height="[^"]*"', f'<svg width="{(x1 - x0) * 4.6:.0f}" height="{(y1 - y0) * 4.6:.0f}" viewBox="{vb}"', svg, 1)
    # The field image once per page (FIELD_IMAGE below), not once per card.
    global FIELD_IMAGE
    image = re.search(r'<image [^>]*/>', svg).group(0)
    if FIELD_IMAGE is None:
        FIELD_IMAGE = image.replace("<image ", '<image id="fieldimg" ', 1)
    return svg.replace(image, '<use href="#fieldimg"/>')


FIELD_IMAGE = None


def bar(label, value, best=BEST):
    pct = min(100.0, 100.0 * value / 50)
    ref = 100.0 * best / 50
    return (f'<div class="row"><span class="lbl">{label}</span><span class="track"><span class="fill" style="width:{pct:.0f}%"></span>'
            f'<span class="ref" style="left:{ref:.0f}%"></span></span><b>{value}%</b></div>')


cards = []
for name, w, l, slide, out, fwd, low, nose, kept, inside, closer in SHAPES:
    svg, (across, ahead) = top_view(w, l, slide, out, fwd, low)
    cards.append(
        f'<div class="card"><h2>{name}</h2><div class="pics">{svg}{field_view(w, l, slide, out, fwd, nose)}</div>'
        f'<div class="facts">body {w:g} × {l:g} in · out: {across:g} wide × {ahead:g} long · parks {nose} in from the wall</div>'
        f'{bar("kept, 8 POLLEN", kept[0])}{bar("kept, match start", kept[1])}'
        f'<div class="facts">G409: 0 of 200 TIPs here; one inch nearer, {closer}</div>'
        f'<div class="facts">G407: most inside at once {inside[0] or "–"}{f", over 4 in {inside[1]} of 200 TIPs" if inside[1] else ""}</div></div>')

html = f"""<!doctype html><html><head><meta charset="utf-8"><title>Robot shapes and the spill</title>
<style>
body{{margin:0;padding:20px;background:#f3f4f6;font-family:Helvetica,Arial,sans-serif;color:{INK}}}
h1{{font-size:20px;margin:0 0 4px}} p.sub{{margin:0 0 16px;color:{MUTED};font-size:13px;max-width:1100px}}
.grid{{display:grid;grid-template-columns:repeat(3,auto);gap:14px;justify-content:start}}
.card{{background:#fff;border:1px solid #e5e7eb;border-radius:10px;padding:12px 14px}}
.card h2{{font-size:15px;margin:0 0 8px}} .pics{{display:flex;gap:10px;align-items:center}}
.pics svg{{border-radius:6px;border:1px solid #e5e7eb}}
.facts{{font-size:12px;color:{MUTED};margin-top:6px}}
.row{{display:flex;align-items:center;gap:8px;font-size:12px;margin-top:6px}} .lbl{{width:108px;color:{MUTED}}}
.track{{position:relative;flex:1;height:12px;background:#eef0f3;border-radius:6px}}
.fill{{position:absolute;left:0;top:0;bottom:0;background:{BLUE};border-radius:6px}}
.ref{{position:absolute;top:-3px;bottom:-3px;width:2px;background:{INK}}}
</style></head><body>
<h1>Which robot shape keeps a TIP's spill?</h1>
<p class="sub">Each robot parked as near the HIVE as it can with no G409 touch in 200 TIPs (8 POLLEN and match-start loads).
Left: from above, front up; grey dashes the 18 × 18 in start box, orange dashes the 18 × 24 in it must fit after the start; flaps and walls blue.
Right: on the field over where the spill first lands; the white dots are the patch counted as kept 3 s later. Bars: share of the spill kept;
the black line is the long U (41%).</p>
<svg width="0" height="0" style="position:absolute"><defs>{FIELD_IMAGE}</defs></svg>
<div class="grid">{"".join(cards)}</div></body></html>"""
open(OUT, "w").write(html)
print(f"wrote {OUT}: {len(cards)} shapes")
