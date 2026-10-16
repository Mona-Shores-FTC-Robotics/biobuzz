"""Assemble the FLOWER stick page: write-up + viewer with every option. Reads the b64 files in this folder."""
import os, sys
S = os.path.dirname(os.path.abspath(__file__))
def b64(n): return open(os.path.join(S, n + ".b64")).read()
import base64
def b64f(path): return base64.b64encode(open(os.path.join(S, path), "rb").read()).decode()
head = open(os.path.join(S, "viewer_head.html")).read().replace("<title>FLOWER Stick Blocks</title>", "<title>The FLOWER Stick</title>")
head = head.replace("</style>", """
.wrap { max-width: 1120px }
.toc { display: flex; flex-wrap: wrap; gap: 8px 18px; font: 500 13px/1.4 var(--mono); color: var(--muted) }
.toc a { color: var(--petg); text-decoration: none } .toc a:hover { text-decoration: underline }
section.block { display: grid; gap: 14px; scroll-margin-top: 16px }
section.block > p, section.block > ul { margin: 0; max-width: 70ch }
section.block ul { padding-left: 20px; display: grid; gap: 8px }
.two { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1fr); gap: 20px; align-items: start }
@media (max-width: 820px) { .two { grid-template-columns: minmax(0, 1fr) } }
.card { background: var(--panel); border: 1px solid var(--line); border-radius: 10px; overflow: hidden; display: grid; grid-template-rows: auto 1fr auto }
.card h3 { font: 600 13px/1 var(--mono); letter-spacing: .06em; text-transform: uppercase; color: var(--muted); margin: 0; padding: 12px 14px; border-bottom: 1px solid var(--line) }
.card img { display: block; width: 100%; height: auto }
.card .foot { padding: 10px 14px; font-size: 13px; color: var(--muted); border-top: 1px solid var(--line) }
.callout { border-left: 4px solid var(--petg); padding: 4px 0 4px 16px; max-width: 72ch }
table.cmp { border-collapse: collapse; width: 100%; font-size: 13.5px; background: var(--panel); border: 1px solid var(--line); border-radius: 10px; overflow: hidden }
table.cmp th, table.cmp td { text-align: left; padding: 9px 12px; border-bottom: 1px solid var(--line); vertical-align: top }
table.cmp th { font: 600 12px/1.3 var(--mono); letter-spacing: .05em; text-transform: uppercase; color: var(--muted) }
table.cmp tr:last-child td { border-bottom: 0 }
table.cmp td.n { font: 500 13px var(--mono); white-space: nowrap }
.tablewrap { overflow-x: auto }
.theory svg { display: block; width: 100%; max-width: 640px; height: auto; margin: 0 auto }
.theory svg text { font: 500 12px var(--mono); fill: var(--fg) } .theory svg .lbl { fill: var(--muted) }
.formula { font: 500 15px var(--mono); background: var(--panel); border: 1px solid var(--line); border-radius: 8px; padding: 10px 14px; display: inline-block }
.pick { display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 8px; padding: 12px; border-top: 1px solid var(--line) }
.pick button { font: 500 13px/1.25 var(--body); padding: 9px 10px; background: transparent; color: var(--fg); border: 1px solid var(--line); border-radius: 7px; cursor: pointer; text-align: left }
.pick button[aria-pressed="true"] { background: var(--petg); color: #fff; border-color: var(--petg) }
.pick button small { display: block; font: 400 11px/1.3 var(--mono); opacity: .8; margin-top: 2px }
</style>""")
X = lambda x: round(x * 100 + 380, 1); Y = lambda z: round(330 - z * 100, 1)
import math
# the theory diagram: ball pinched between the blade face and the upright
def theory_svg():
    """Block A the way the lab drove it: steep face first, ramp toward the robot. The steep face leans 17 deg from
    vertical and meets the POLLEN (centre 1.4 in, against the uprights) below its middle. x along the push, z up, inches."""
    R = 1.4; cx, cz = 0.35, 1.4
    bottom, top, land, slant, flat, depth = 0.52, 1.30, 0.12, 0.2, 0.5, 1.4
    lean = math.atan2(slant, top - bottom - land)            # 17 deg from vertical
    nx, nz = math.cos(lean), math.sin(lean)                  # the face's normal, toward the ball: tilted up by `lean`
    px, pz = cx - R * nx, cz - R * nz                        # where the leaning face touches the ball
    # place the block so its leaning face passes through the contact point: the face runs from (tx, bottom+land) to (tx+slant, top)
    tx = px + (pz - (bottom + land)) * (slant / (top - bottom - land))   # the face's foot, nearest the ball; the block extends toward the robot (-x)
    block = [(tx, bottom), (tx, bottom + land), (tx - slant, top), (tx - slant - flat, top), (tx - depth, bottom)]
    rod = (tx - 12 / 25.4, 0.77)
    up_x = cx + R
    pts = " ".join(f"{X(x)},{Y(z)}" for x, z in block)
    deg = math.degrees(lean)
    return (f'<svg viewBox="0 0 660 400" role="img" aria-labelledby="th">\n'
    f'<title id="th">Block A driven steep face first, its ramp toward the robot. The steep face, leaning {deg:.0f} degrees from vertical, meets the POLLEN below its middle; the POLLEN rests against the upright. The push at the face is tilted up only {deg:.0f} degrees; the upright pushes back horizontally; friction at both contacts resists the POLLEN rising.</title>\n'
    '<defs><marker id="ar" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse"><path d="M0,0 L10,5 L0,10 z" fill="var(--petg)"/></marker>\n'
    '<marker id="ag" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse"><path d="M0,0 L10,5 L0,10 z" fill="var(--muted)"/></marker></defs>\n'
    f'<line x1="0" y1="{Y(0)}" x2="660" y2="{Y(0)}" stroke="var(--muted)" stroke-width="2"/>\n'
    f'<rect x="{X(up_x)}" y="{Y(3.6)}" width="30" height="{Y(0) - Y(3.6)}" fill="var(--steel)" opacity=".8"/>\n'
    f'<text x="{X(up_x) + 36}" y="{Y(2.6)}" class="lbl">upright</text>\n'
    f'<polygon points="{pts}" fill="var(--petg)" opacity=".85"/>\n'
    f'<circle cx="{X(rod[0])}" cy="{Y(rod[1])}" r="16" fill="var(--steel)" stroke="var(--fg)" stroke-width="1"/>\n'
    f'<text x="{X(tx - depth)}" y="{Y(0) + 20}" font-size="12">Block A: steep face first, ramp toward the robot</text>\n'
    f'<text x="{X(tx - depth)}" y="{Y(0) + 36}" class="lbl" font-size="11">the face leans {deg:.0f}°; the ball should pop onto the top and roll out, left</text>\n'
    f'<line x1="{X(tx - slant - flat)}" y1="{Y(1.9)}" x2="{X(tx - depth - 0.8)}" y2="{Y(1.9)}" stroke="var(--muted)" stroke-width="2" marker-end="url(#ag)"/>\n'
    f'<text x="{X(tx - depth - 0.8)}" y="{Y(1.9) - 8}" class="lbl" font-size="11">the ramp out: toward the robot</text>\n'
    f'<circle cx="{X(cx)}" cy="{Y(cz)}" r="{R * 100}" fill="var(--pollen)" opacity=".5" stroke="var(--fg)" stroke-width="1.5"/>\n'
    f'<text x="{X(cx)}" y="{Y(cz + 0.75)}" text-anchor="middle">POLLEN</text>\n'
    f'<line x1="{X(px)}" y1="{Y(pz)}" x2="{X(px + 1.0 * nx)}" y2="{Y(pz + 1.0 * nz)}" stroke="var(--petg)" stroke-width="3" marker-end="url(#ar)"/>\n'
    f'<text x="{X(px + 1.0 * nx) + 4}" y="{Y(pz) + 2}" fill="var(--petg)">push at the face,</text>\n'
    f'<text x="{X(px + 1.0 * nx) + 4}" y="{Y(pz) + 16}" fill="var(--petg)">tilted up only {deg:.0f}°</text>\n'
    f'<path d="M{X(px + 0.6)},{Y(pz)} A60,60 0 0 0 {X(px + 0.6 * nx)},{Y(pz + 0.6 * nz)}" fill="none" stroke="var(--petg)" stroke-width="1.5"/>\n'
    f'<line x1="{X(px)}" y1="{Y(pz)}" x2="{X(px + 0.9)}" y2="{Y(pz)}" stroke="var(--petg)" stroke-dasharray="4 4"/>\n'
    f'<text x="{X(px + 0.7)}" y="{Y(pz + 0.18)}" fill="var(--petg)">α</text>\n'
    f'<line x1="{X(up_x)}" y1="{Y(cz)}" x2="{X(up_x - 0.9)}" y2="{Y(cz)}" stroke="var(--muted)" stroke-width="3" marker-end="url(#ag)"/>\n'
    f'<text x="{X(up_x - 0.95)}" y="{Y(cz) - 8}" class="lbl" text-anchor="end">the upright pushes back</text>\n'
    f'<line x1="{X(up_x)}" y1="{Y(cz)}" x2="{X(up_x)}" y2="{Y(cz - 0.8)}" stroke="var(--muted)" stroke-width="3" marker-end="url(#ag)"/>\n'
    f'<text x="{X(up_x) - 8}" y="{Y(cz - 0.8) + 16}" class="lbl" text-anchor="end">friction, down</text>\n'
    f'<line x1="{X(px)}" y1="{Y(pz)}" x2="{X(px + 0.5 * nz)}" y2="{Y(pz - 0.5 * nx)}" stroke="var(--muted)" stroke-width="3" marker-end="url(#ag)"/>\n'
    f'<text x="{X(px) - 6}" y="{Y(0.3)}" class="lbl" text-anchor="end">friction at the face, down</text>\n'
    '</svg>')
# the viewer's models
MODELS = [
    ("scoop", "v2 scoop", "Carter's divot read as an arc in the front edge", 6.35, 17.5),
    ("spoon", "v2 spoon", "read as a bowl in the ramp face", 6.35, 17.5),
    ("vee", "v2 V-notch", "a V in plan: strongest self-centring", 6.35, 17.5),
    ("tongue", "v2 tongue", "a 1.2 in lane reaching 0.4 in deeper, between the lobes", 6.35, 32.8),
    ("highride", "v2 high ride", "for 48 mm wheels: 0.35 in over the ring", 6.22, 17.5),
    ("plain", "v2 plain", "no hollow, as a control", 6.35, 17.5),
    ("steep", "v2 steep", "the scoop with a 55° nose and a sharp 0.52 in edge: jam margin", 6.35, 19.05),
    ("blockA", "Block A (tested 15 Oct)", "steep face first, ramp toward the robot; it stalled", 6.35, 12.0),
]
stl_js = "const STL = {" + ", ".join(f'{k}: "{b64("v2_" + k if k != "blockA" else "blockA")}"' for k, *_ in MODELS) + "};"
info_js = "const INFO = {" + ", ".join(f'{k}: {{ name: "{n}", shaftZ: {sz}, shaftBack: {sb} }}' for k, n, _, sz, sb in MODELS) + "};"
buttons = "\n".join(f'      <button id="pick-{k}" aria-pressed="{"true" if i == 0 else "false"}">{n}<small>{d}</small></button>' for i, (k, n, d, *_ ) in enumerate(MODELS))
body = f'''
<div class="wrap">
<header>
  <span class="eyebrow">Mona Shores FTC 19429 / 20245 · BIOBUZZ · issue #179 · updated 16 Oct 2026</span>
  <h1>The FLOWER stick</h1>
  <p class="lede">A blade on a stick that drives into a FLOWER and empties its four POLLEN in about a second. What the
  team built and tested, why one block jammed, what FIRST's field CAD says the FLOWER really is, Carter's divot, and
  seven ways to cut it, what other teams built, and how the blade hands the POLLEN to the intake. The 3D models are the actual print files.</p>
  <nav class="toc" aria-label="Sections">
    <a href="#how">How it works</a><a href="#journey">How we got here</a><a href="#test">The 15 Oct test</a><a href="#jam">Why one block jammed</a>
    <a href="#flower">The real FLOWER</a><a href="#carter">Carter's divot</a><a href="#options">The options</a><a href="#others">Other teams</a><a href="#intake">Into the intake</a><a href="#next">What to test</a>
  </nav>
</header>

<section class="block" id="how">
  <h2>How it works</h2>
  <p>A FLOWER holds four POLLEN (2.8 in wiffle-type balls) stacked in a cup at its base. The bottom one sits on the tiles inside a
  3.08 in hole in a 0.35 in base ring, against two uprights at the back. The concept the lab built and tested: a block on a
  stick drives in <strong>steep face first, with its ramp facing the robot</strong>. Once the block's top corner is under the
  bottom POLLEN, the ball rides up onto the flat top and rolls down the ramp, straight out of the FLOWER perpendicular to the
  wall, into the intake. The stack drops one and the next ball follows. In the 2-D simulator the last ball is out 1.2 to 1.5 s
  after the block touches.</p>
  <p>The goal, in one line: drive in once, steadily, and have all four POLLEN roll out of the FLOWER toward the robot, with the
  FLOWER itself as the depth stop so it lands the same every time. The ramp out is the part that worked. The entry, how the
  block gets under the first ball, is the part this page is about.</p>
  <p>On the prototype the wedge hangs from two pattern-beam arms that pivot on the robot's front; small wheels on the wedge's
  shaft roll on the tiles and set its height. The FLOWER's uprights are the depth stop.</p>
</section>

<section class="block" id="journey">
  <h2>How we got here</h2>
  <ul>
    <li><strong>The ramp hook</strong> (<code>cad/ramp-hook</code>): a bar with a ramp, the first idea for pulling POLLEN out from under the FLOWER.</li>
    <li><strong>Blocks A and B printed</strong> (<code>block.py</code>): the hook's block at two heights, for the robot's small wheels and for 48 mm wheels, with an 8 mm hex bore. First meant for a fixed grid-plate mount on the goBILDA chassis.</li>
    <li><strong>10 Oct, the pivoting sled.</strong> The fixed mount fought the lab on height, so two pattern-beam arms pivot off the front and the block's shaft rides on small wheels at 0.77 in.</li>
    <li><strong>15 Oct, the test with Carter.</strong> Two blocks side by side on the sled, pushed in by hand, three runs on video. It emptied the FLOWER but only with a back-and-forth, and never seated against the back. Carter sketched a single blade with a divot.</li>
    <li><strong>16 Oct, the field CAD and v2.</strong> FIRST's FLOWER measured from its STEP file, the jam explained, and the v2 blades: a wedge entry, Carter's divot, the ramp out kept.</li>
    <li><strong>16 Oct, other teams, and a wrong turn.</strong> What the Ri3D and league teams built. A full-width ramp with a plate and roller was drawn and dropped the same day: it put a sheet in front of the intake and left the ball on a shelf. What replaced it is the original concept with numbers: the blade releases straight into the intake's mouth at the FLOWER's face.</li>
  </ul>
</section>

<section class="block" id="test">
  <h2>The 15 Oct test</h2>
  <div class="two">
    <div class="card"><h3>Two blocks at the FLOWER</h3><img src="data:image/jpeg;base64,{b64("ph")}" alt="Two blue printed blocks side by side on the shaft at the base of a loaded FLOWER."><div class="foot">Carter and a mentor pushed the sled in by hand with both printed blocks on the shaft. Three runs on video; the last emptied all four.</div></div>
    <div class="card"><h3>One run: in, under, out</h3><img src="data:image/jpeg;base64,{b64("frames")}" alt="Three frames: the sled approaching, the blocks under the POLLEN, four POLLEN on the tiles."><div class="foot">Once under, the stack came out in about a second. Getting under took 4 to 6 s of working the sled back and forth, and it never seated against the uprights.</div></div>
  </div>
  <ul>
    <li><strong>Two printed blocks, A and B,</strong> the same shape at two heights (for the small wheels and for 48 mm wheels), on the sled's shaft. Each has a steep curved face, a 0.5 in flat top, and a 41° ramp down to a sharp edge.</li>
    <li><strong>They went in steep face first, ramp toward the robot.</strong> The steep face, which leans 17° from vertical, pushed against the bottom POLLEN; once under it, the ball was meant to pop onto the flat top and roll down the ramp out of the FLOWER.</li>
    <li><strong>One block alone was hard to get under.</strong> Two side by side went under, with the POLLEN sitting in the valley between their two curved faces.</li>
    <li><strong>It would not seat against the back.</strong> That turned out to be the important one.</li>
  </ul>
  <div class="card"><h3>The stack, frame by frame</h3><img src="data:image/png;base64,{b64f("/home/user/biobuzz/cad/modules/flower-stick-proto/stack_sequence.png")}" alt="Six side sections of the FLOWER with Block A driving in steep face first: approach, contact below the ball's middle, the ball up on the flat top, rolling down the ramp away from the wall, the next ball lifted, all four out."><div class="foot">The concept as the lab built it, drawn in the field-CAD FLOWER's slice: Block A on the sled, steep face first, ramp toward the robot. Schematic. The 2-D simulator with this block reaches frame 3 on its own, since its block cannot stall, and then shows a second thing the videos show: the first ball comes off the ramp onto the ring's inner edge with the stack on top of it, and a single steady push never frees it. One back-out frees one ball. So the real block needed the back-and-forth twice over: to get past the face, and to clear each ball off the ring.</div></div>
</section>

<section class="block theory" id="jam">
  <h2>Why one block jammed</h2>
  <p>While the blade pushes, the bottom POLLEN is pinched between the blade's pushing face and the uprights behind it. Two
  contacts, both with friction. For the ball to move at all it has to slide up both surfaces at once, and friction at both
  contacts resists that. The drawing is Block A the way the lab drove it: steep face first, ramp toward the robot.</p>
  {theory_svg()}
  <p>Write the force balance with Coulomb friction μ at both contacts and the blade's contact normal tilted up by α. The ball can
  be lifted by a steady push only if</p>
  <p><span class="formula">tan α &gt; 2μ / (1 − μ²), i.e. α &gt; 2·atan μ</span></p>
  <p>Below that angle the harder you push, the harder it jams. The push force cancels out: no amount of it helps. That is 33° at
  μ 0.3 and 44° at μ 0.4. If the ball rolls on the blade instead of sliding, the limit halves (17° and 22°).</p>
  <div class="callout">Block A's steep face leans 17° from vertical, so the push on the ball is tilted up only 17°. By the
  rule above it lifts only if friction is under 0.15, and PETG on the POLLEN's plastic is two or three times that. It stalls
  under any steady push and goes on only when wiggled or hit, because kinetic friction is lower than static and a jolt
  carries the ball up over the corner. That is the back-and-forth in the videos. The simulator never showed it: its block is
  driven by position, so it cannot stall.</div>
  <p>The exit half of the concept is right: once the ball is on top, the ramp rolls it out toward the robot, and every design
  on this page keeps that. The entry is what has to change: the ball must meet a wedge, not a wall. The levers, in order:</p>
  <ul>
    <li><strong>A wedge entry.</strong> The v2 nose is a 45° face whose edge, at 0.60 in, meets the ball at 35°; it lifts up to μ 0.41 sliding. The <em>v2 steep</em> option is 55° with a sharp 0.52 in edge (39° first contact), good to μ 0.52.</li>
    <li><strong>A lower edge still.</strong> The edge height sets the first contact. A sheet lip sitting on the ring at 0.37 in: 47°.</li>
    <li><strong>A slippery skin.</strong> UHMW or packing tape on the face drops μ to 0.1 to 0.2, which clears the limit by itself.</li>
    <li><strong>A roller.</strong> Pulls the ball up directly and removes the limit. That is what the other teams below have.</li>
  </ul>
  <p>If the ball rolls on the blade instead of sliding, every limit above halves in angle, so the real margin sits between the
  two. The four-POLLEN stack weighs 0.22 lb; the push to lift it is a pound or two either way.</p>
</section>

<section class="block" id="flower">
  <h2>The real FLOWER, from FIRST's field CAD</h2>
  <p>Until 16 Oct the FLOWER in our tools was scaled off the manual's figure. FIRST publishes the field as a STEP file;
  <code>tools/robot-cad/flower_from_field_cad.py</code> pulls the Flower Assembly out of it and measures it about the bottom
  POLLEN's centre.</p>
  <div class="tablewrap"><table class="cmp">
    <tr><th></th><th>from the figure</th><th>field CAD</th><th>why it matters</th></tr>
    <tr><td>base ring height</td><td class="n">0.43</td><td class="n">0.35</td><td>0.17 in under a blade at 0.52</td></tr>
    <tr><td>hole</td><td class="n">2.79</td><td class="n">3.08</td><td>a 2.72 in convex block has 0.18 in of slop, not 0.03</td></tr>
    <tr><td>uprights' forward points</td><td class="n">1.25 behind</td><td class="n">1.18 behind, at y ±1.42</td><td>the hard stop</td></tr>
    <tr><td>uprights' inner corners</td><td class="n">(1.25, ±0.62)</td><td class="n">(1.61, ±0.61)</td><td>a lifted POLLEN is pinned 0.35 in <em>behind</em> the hole's centre, not in front of it: the slope has to start behind that point</td></tr>
    <tr><td>bracket opening</td><td class="n">3.55</td><td class="n">3.90</td><td>the lifted ball just clears it</td></tr>
    <tr><td>green pipes</td><td class="n">reach the base</td><td class="n">start 4.25 in up</td><td>nothing in front of the uprights is in the blade's way</td></tr>
  </table></div>
  <div class="card"><h3>Blade v2 seated in the CAD FLOWER</h3><img src="data:image/jpeg;base64,{b64("seated")}" alt="Side and plan sections of the blade seated against the FLOWER's uprights, from the field CAD."><div class="foot">Swept into the CAD, every v2 blade touches nothing until its shoulders meet the uprights' forward points at x 1.18. The dashed circle is the first POLLEN, lifted and pinned at x 0.35, sitting 0.38 in down the lane's slope.</div></div>
</section>

<section class="block" id="carter">
  <h2>Carter's divot</h2>
  <div class="two">
    <div class="card"><h3>The sketch, 15 Oct</h3><img src="data:image/jpeg;base64,{b64("sk")}" alt="Carter's pencil sketch: the two blocks with a ball between them, one long blade with a divot, and its front and end views."><div class="foot">Left: today's two blocks with a POLLEN between them. Middle: one blade with a divot. Right: its front view on the shaft; above, the end view with the hex.</div></div>
    <div>
      <p><strong>What came from the sketch:</strong> one blade instead of two blocks, spanning the shaft; a divot in the middle of the front that the POLLEN seats in, doing on purpose what the valley between two blocks did by accident; the shaft through the blade; the wedge end profile.</p>
      <p><strong>What the test added:</strong> the jam limit: the entry must be a wedge, not a wall, and the edge height and face angle are the numbers that matter. The ramp out stays. <strong>What the CAD added:</strong> the depth, the stop, and the rule that the lane's slope must start at the ridge, because a flat that reaches back to the pinned POLLEN holds it (0 of 4 out in the simulator at a slow push; 4 of 4 at 2, 4 and 12 in/s without the flat).</p>
      <p><strong>What the sketch leaves open:</strong> whether the divot is a scoop cut into the front edge (the top view) or a spoon hollowed into the ramp face (the front view's trapezoid). Both are built. Carter says which they meant; the test can run both.</p>
    </div>
  </div>
</section>

<section class="block" id="options">
  <h2>The options</h2>
  <p>Every blade below shares the v2 body: a 45° nose (55° on the steep one), a low centre lane whose slope starts at the ridge, taller shoulders that take
  the stop and carry the 8 mm hex shaft, 5.4 in wide, the shaft at the sled's 0.77 in. They differ in the hollow, the lane, or the
  height. Pick one and turn it. Scoop side is the front.</p>
  <div class="viewer">
    <canvas id="stage" aria-label="3D model of the selected blade. Drag to turn it, scroll or pinch to zoom."></canvas>
    <div class="pick" role="group" aria-label="Which blade">
{buttons}
    </div>
    <dl class="spec" id="spec"></dl>
  </div>
  <div class="tablewrap"><table class="cmp">
    <tr><th>option</th><th>what changes</th><th>for</th><th>against</th><th>2-D sim, slow and fast push</th></tr>
    <tr><td class="n">scoop</td><td>arc in the front edge, 1.8 wide, 0.21 deep</td><td>Carter's top view; simple cutter; centres the ball</td><td>vertical walls: the ball touches two edges</td><td class="n">4/4, 4/4</td></tr>
    <tr><td class="n">spoon</td><td>bowl in the nose face, 1.8 wide, 0.15 deep</td><td>Carter's front view; contact spread over the bowl</td><td>shallow by necessity: the nose face is only 0.64 in long</td><td class="n">4/4, 4/4</td></tr>
    <tr><td class="n">V-notch</td><td>a V in plan, 2.0 wide, 0.3 deep</td><td>strongest self-centring; two clean lines of contact</td><td>sharp apex to print; the ball touches at two points</td><td class="n">4/4, 4/4</td></tr>
    <tr><td class="n">tongue</td><td>1.2 in lane reaching 0.4 in deeper, between the uprights' lobes</td><td>the ridge lands 0.78 in past the pinned POLLEN, double the margin; the tongue's corners find the lobes</td><td>narrow support under the ball; 0.03 in from the lobes at the stop</td><td class="n">4/4, 4/4</td></tr>
    <tr><td class="n">high ride</td><td>bottom 0.70 in, for 48 mm wheels</td><td>0.35 in over the ring instead of 0.17; same geometry otherwise</td><td>needs the 48 mm wheels fitted</td><td class="n">same profile</td></tr>
    <tr><td class="n">plain</td><td>no hollow</td><td>the control: tells us whether the divot earns its keep</td><td>no centring beyond the shoulders</td><td class="n">4/4, 4/4</td></tr>
    <tr><td class="n">steep</td><td>the scoop with a 55° nose and a sharp 0.52 in edge</td><td>lifts up to μ 0.52 instead of 0.41; the ridge lands 0.46 in past the pinned ball</td><td>a sharper edge to print; less plastic ahead of the bore</td><td class="n">4/4, 4/4</td></tr>
    <tr><td class="n">block A</td><td>the 15 Oct block: steep face first (leaning 17°), ramp toward the robot</td><td>already printed; the ramp out works</td><td>the entry is a wall: stalls above μ 0.15</td><td class="n">4/4 in the sim, which cannot stall</td></tr>
  </table></div>
  <p>The simulator is 2-D along the centreline with friction, so it sees the lane and the stop but not the hollow's shape. The
  hollows differ in how they centre the ball and how they feel on a real push; that is what the test is for.</p>
</section>

<section class="block" id="others">
  <h2>What other teams built</h2>
  <p>Four clips from YouTube Shorts, 16 Oct. Three recipes: a thin lip with a powered roller right behind it (WXYZ, CrossFire), a
  big passive wedge (Hexadecimal Nibble), and a servo arm that sweeps the base from the side (Eaglebots).</p>
  <div class="two">
    <div class="card"><h3>19705 WXYZ, Ri3D (FUN Robotics)</h3><img src="data:image/jpeg;base64,{b64f("clips/wxyz.jpg")}" alt="A wide red ramp with a thin front lip and a row of white compliant wheels on a shaft just behind it, on red pivot arms."><div class="foot">A wide thin ramp at tile level, a row of driven compliant wheels just behind the lip, the whole thing folding up on pivot arms. Their floor intake's lip, which also works at the FLOWER.</div></div>
    <div class="card"><h3>CrossFire 9968</h3><img src="data:image/jpeg;base64,{b64f("clips/crossfire.jpg")}" alt="A pattern beam off a purple chassis ending in a bent plate wedge under a FLOWER's stack, a string roller behind it."><div class="foot">A pattern beam with a bent sheet wedge at the tip and a string roller behind it, pushed in by hand like our sled. Three POLLEN already out in the frame.</div></div>
    <div class="card"><h3>Hexadecimal Nibble</h3><img src="data:image/jpeg;base64,{b64f("clips/hexnibble.jpg")}" alt="A large yellow printed wedge on a pattern beam, about as tall as a POLLEN, in front of a loaded FLOWER."><div class="foot">A passive wedge about as tall as a ball and 5 to 6 in long. The scale is the point: the ball climbs it into the robot.</div></div>
    <div class="card"><h3>Eaglebots 13532</h3><img src="data:image/jpeg;base64,{b64f("clips/eaglebots.jpg")}" alt="A plywood robot parked beside a FLOWER with a servo arm swinging a bent wedge through the FLOWER's base."><div class="foot">The robot parks beside the FLOWER against the wall and a servo arm sweeps a wedge through the base, sideways. The wall is the depth stop, and a sideways push has nothing to pinch the ball against.</div></div>
  </div>
  <p>All three of the wedge designs meet the ball low. Where the edge first meets a 1.4 in ball sets the first contact's angle,
  before the face's own angle takes over:</p>
  <div class="tablewrap"><table class="cmp">
    <tr><th>edge meets the ball at</th><th>contact normal tilted up</th><th>which is</th></tr>
    <tr><td class="n">a face leaning 17°, Block A's steep face</td><td class="n">17°</td><td>Block A as driven: stalls</td></tr>
    <tr><td class="n">0.60 in, the v2 nose's edge above its land</td><td class="n">35°</td><td>v2 scoop, spoon, vee, tongue, plain</td></tr>
    <tr><td class="n">0.52 in, sharp edge</td><td class="n">39°</td><td>v2 steep</td></tr>
    <tr><td class="n">0.37 in, a sheet lip sitting on the ring</td><td class="n">47°</td><td>WXYZ, CrossFire</td></tr>
  </table></div>
  <p>The roller is the bigger idea. Our blade works by pushing, so the ball's lift comes only from the wedge and fails when
  friction is high. A roller on top of the ball pulls it back and up directly, so the push only has to get the lip under the ball.
  That is why every VEX intake is a ramp plus flex wheels, and why neither WXYZ nor CrossFire needs a hard stop. For us it says where the intake's first roller has to be: at the FLOWER's face, see below.</p>
</section>

<section class="block" id="intake">
  <h2>Into the intake</h2>
  <p class="callout">The blades above are small because they were scoped to one question on a sled: can a wedge get under the
  bottom POLLEN and lift it out. Their top is lower than the ball's centre, so the ball rolls off the back onto the tiles. That
  is only useful if the intake is right there to take it. A ramp or sheet in front of the intake is not the answer: it moves the
  problem 6 in further from the FLOWER and makes floor pickup worse. The answer is the original concept with the numbers filled
  in: the blade releases the ball straight into the intake's mouth, and the mouth sits at the FLOWER's face.</p>
  <div class="tablewrap"><table class="cmp">
    <tr><th>part</th><th>where</th><th>why</th></tr>
    <tr><td>blade's stop</td><td class="n">1.18 in behind the FLOWER's centre</td><td>the uprights' forward points: the same depth every time, and the blade's 5.4 in shoulders reach them</td></tr>
    <tr><td>blade's back edge</td><td class="n">0.5 in in front of the centre</td><td>the ball leaves the back slope here, still 1.4 in inside the ring</td></tr>
    <tr><td>intake roller's rim</td><td class="n">at least 2.5 in in front of the centre</td><td>the first spot clear of the bracket (2.41) and the pipes (2.25): 0.1 in in front of the ring's edge</td></tr>
    <tr><td>open tile between them</td><td class="n">about 2 in</td><td>the ball crosses it on the speed it gets from the drop, and the roller takes it before it can settle on the ring's edge</td></tr>
    <tr><td>stick length</td><td class="n">about 3.7 in beyond the mouth</td><td>short sled arms: the blade's shaft about 2 in ahead of the roller's axle</td></tr>
  </table></div>
  <div class="card"><h3>In the FLOWER's slice, from the simulator</h3><img src="data:image/png;base64,{b64f("/home/user/biobuzz/cad/modules/flower-stick-proto/intake_section.png")}" alt="Three side sections: the v2 blade at its stop in the FLOWER, the first POLLEN over the ridge, rolling down the back slope onto the tiles, and at the intake's roller just in front of the ring, the stack dropped behind it."><div class="foot">The 2-D simulator with the field-CAD FLOWER, the v2 lane profile, the lintel and a roller at the FLOWER's face. All four POLLEN leave and pass the roller at 2, 4 and 12 in/s, friction 0.3 and 0.5. The simulator has no rolling resistance, so the 2 in of open tile is the part to doubt until it is tested.</div></div>
  <pre class="formula" id="simtable" style="display:block; white-space:pre; overflow-x:auto; font-size:13px">{{SIM_TABLE}}</pre>
  <p>What this needs from the lab, and nothing on this page can supply: the intake's mouth height and where its first roller sits
  relative to the robot's front. If the mouth is low and at the front, the sled arms shorten to a couple of inches and the FLOWER
  tool is a small blade plus the intake you already have. If the mouth is high or set back, there is a transport problem no
  blade solves, and that is worth knowing before Dean prints anything.</p>
  <p>Two things the field CAD rules out for anyone tempted by a bigger ramp: a roller cannot sit inside the FLOWER (a ball on a
  0.45 in lip tops out at 3.25, the bracket's underside is 3.90, nothing fits between), and a rising ramp cannot carry the ball
  to a crest (the lip stops 0.83 in past the pinned ball). Lift over a low ridge, drop, roller outside: that is the whole
  design space.</p>
</section>

<section class="block" id="next">
  <h2>What to test</h2>
  <ul>
    <li><strong>Does it seat with one steady push?</strong> That is the whole question. Five straight pushes and one 1 in off-centre per blade; count POLLEN out and time from the side.</li>
    <li><strong>If it still wants a wiggle,</strong> tape the nose first. If tape fixes it, friction is the problem and the next print is <em>v2 steep</em>, already in the viewer.</li>
    <li><strong>Then ask Carter</strong> which hollow is the one they drew, and print that one for the robot.</li>
    <li><strong>Measure the intake:</strong> mouth height off the tiles and the first roller's position behind the robot's front. That sets the sled's arm length, and it is the one number the next design cannot do without.</li>
  </ul>
  <p style="color: var(--muted); font-size: 13px">Files: <code>cad/modules/flower-stick-proto/</code> on <code>spike/179-flower-stick-proto</code>. Every blade here is
  <code>divot_blade.py</code> with flags; the print plate holds scoop and spoon. Theory and simulation: <code>tools/ramp-hook/blade_variants.py</code>.</p>
</section>
</div>
<script src="https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/three@0.128.0/examples/js/loaders/STLLoader.js"></script>
<script src="https://cdn.jsdelivr.net/npm/three@0.128.0/examples/js/controls/OrbitControls.js"></script>
<script>
{stl_js}
{info_js}
const css = n => getComputedStyle(document.documentElement).getPropertyValue(n).trim();
const canvas = document.getElementById("stage");
const renderer = new THREE.WebGLRenderer({{ canvas, antialias: true }});
renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
const scene = new THREE.Scene();
const camera = new THREE.PerspectiveCamera(35, 4 / 3, 1, 2000);
camera.position.set(175, 90, 0);
const controls = new THREE.OrbitControls(camera, canvas);
controls.enableDamping = true; controls.target.set(0, 8, 0);
scene.add(new THREE.HemisphereLight(0xffffff, 0x445566, 0.85));
const sun = new THREE.DirectionalLight(0xffffff, 0.75); sun.position.set(60, 120, 80); scene.add(sun);
const grid = new THREE.GridHelper(200, 20); scene.add(grid);
const loader = new THREE.STLLoader(); const geos = {{}};
for (const k of Object.keys(STL)) {{
  const bin = atob(STL[k]); const buf = new Uint8Array(bin.length);
  for (let i = 0; i < bin.length; i++) buf[i] = bin.charCodeAt(i);
  const g = loader.parse(buf.buffer);
  g.rotateX(-Math.PI / 2); g.computeBoundingBox(); const bb = g.boundingBox;
  g.translate(-(bb.min.x + bb.max.x) / 2, -bb.min.y, -(bb.min.z + bb.max.z) / 2); g.computeVertexNormals(); geos[k] = g;
}}
const mat = new THREE.MeshStandardMaterial({{ color: css("--petg"), roughness: 0.55, metalness: 0.05 }});
const mesh = new THREE.Mesh(geos.scoop, mat); scene.add(mesh);
let dist = 175, targetY = 8, elev = 0.5;
const shaftGeo = new THREE.CylinderGeometry(8 / Math.sqrt(3), 8 / Math.sqrt(3), 180, 6);
shaftGeo.rotateY(Math.PI / 6); shaftGeo.rotateX(Math.PI / 2);
const shaft = new THREE.Mesh(shaftGeo, new THREE.MeshStandardMaterial({{ color: 0x9aa4ae, roughness: 0.35, metalness: 0.6 }})); scene.add(shaft);
const ROWS = {{
  scoop: [["Hollow", "arc in the front edge, 1.8 in wide, 0.21 deep"], ["Lane", "2.0 in wide, top 1.05 in; shoulders 1.25"], ["Bottom", "0.52 in: 0.17 over the ring"], ["Size", "137 × 49 × 19 mm"]],
  spoon: [["Hollow", "bowl in the nose face, 1.8 in wide, 0.15 deep"], ["Lane", "2.0 in wide, top 1.05 in; shoulders 1.25"], ["Bottom", "0.52 in"], ["Size", "137 × 49 × 19 mm"]],
  vee: [["Hollow", "V notch in plan, 2.0 in wide, 0.3 deep"], ["Lane", "2.0 in wide, top 1.05 in; shoulders 1.25"], ["Bottom", "0.52 in"], ["Size", "137 × 49 × 19 mm"]],
  tongue: [["Hollow", "small scoop, 1.0 in wide"], ["Lane", "1.2 in wide, reaching 0.4 in past the shoulders; its ridge 0.78 in past the pinned POLLEN"], ["Bottom", "0.52 in"], ["Size", "137 × 64 × 19 mm"]],
  highride: [["Hollow", "scoop, as v2 scoop"], ["Lane", "2.0 in wide, top 1.23 in; shoulders 1.43"], ["Bottom", "0.70 in: 0.35 over the ring; for 48 mm wheels (shaft 0.945)"], ["Size", "137 × 49 × 19 mm"]],
  plain: [["Hollow", "none"], ["Lane", "2.0 in wide, top 1.05 in; shoulders 1.25"], ["Bottom", "0.52 in"], ["Size", "137 × 49 × 19 mm"]],
  steep: [["Hollow", "scoop, as v2 scoop"], ["Nose", "55° from horizontal, sharp edge at 0.52 in: first contact 39°, face 55°"], ["Lane", "2.0 in wide, top 1.05 in; shoulders 1.25"], ["Bottom", "0.52 in"]],
  blockA: [["As used", "steep curved face toward the POLLEN, ramp toward the robot"], ["Steep face", "leans 17° from vertical: the push is tilted up only 17°"], ["Ramp out", "0.5 in flat top at 1.30, then 41° down to a sharp edge at 0.52"], ["Result", "stalls under a steady push; empties when wiggled"], ["Size", "36 × 69 × 20 mm"]],
}};
function pick(k) {{
  mesh.geometry = geos[k];
  const bb = new THREE.Box3().setFromObject(mesh);
  shaft.position.set(bb.max.x - INFO[k].shaftBack, INFO[k].shaftZ, 0);
  for (const j of Object.keys(INFO)) document.getElementById("pick-" + j).setAttribute("aria-pressed", j === k);
  const spec = document.getElementById("spec"); spec.innerHTML = "";
  for (const [a, b] of ROWS[k]) {{ const dt = document.createElement("dt"); dt.textContent = a; const dd = document.createElement("dd"); dd.textContent = b; spec.append(dt, dd); }}
}}
for (const k of Object.keys(INFO)) document.getElementById("pick-" + k).onclick = () => pick(k);
function themeColors() {{
  scene.background = new THREE.Color(css("--panel")); mat.color.set(css("--petg"));
  grid.material.color = new THREE.Color(css("--line")); grid.material.opacity = 0.9;
}}
themeColors();
new MutationObserver(themeColors).observe(document.documentElement, {{ attributes: true, attributeFilter: ["data-theme"] }});
try {{ matchMedia("(prefers-color-scheme: dark)").addEventListener("change", themeColors); }} catch (e) {{}}
function resize() {{
  const w = canvas.clientWidth, h = canvas.clientHeight, r = renderer.getPixelRatio();
  if (canvas.width !== Math.round(w * r) || canvas.height !== Math.round(h * r)) {{ renderer.setSize(w, h, false); camera.aspect = w / h; camera.updateProjectionMatrix(); }}
}}
let spin = !matchMedia("(prefers-reduced-motion: reduce)").matches;
canvas.addEventListener("pointerdown", () => {{ spin = false; }});
pick("scoop");
(function loop() {{
  resize();
  if (spin) {{ const t = performance.now() / 6000; const a = 0.55 * Math.sin(t * 2); camera.position.set(Math.cos(a) * dist, dist * elev, Math.sin(a) * dist); }}
  controls.update(); renderer.render(scene, camera); requestAnimationFrame(loop);
}})();
</script>
'''
sim_table = "\n".join(l for l in open(os.path.join(S, "sim_out.txt")).read().strip().splitlines()[1:] if not l.startswith("trace:"))
body = body.replace("{SIM_TABLE}", sim_table.replace("&", "&amp;").replace("<", "&lt;"))
out = os.path.join(S, "flower-stick.html"); open(out, "w").write(head + body); print(out, len(head + body))
