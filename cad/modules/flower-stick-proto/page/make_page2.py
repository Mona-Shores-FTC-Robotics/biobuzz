"""The FLOWER stick page, compact: concept, journey, test, stall, FLOWER numbers, v3 blade + viewer, intake, other teams, next.
Reuses make_page.py's head/CSS and viewer script, and new_svg.py's stall diagram."""
import os, math
S = os.path.dirname(os.path.abspath(__file__))
src = open(os.path.join(S, "make_page.py")).read()
exec(src[:src.index("X = lambda")])                      # head, b64, b64f
X = lambda x: round(x * 100 + 380, 1); Y = lambda z: round(330 - z * 100, 1)
exec(open(os.path.join(S, "new_svg.py")).read())         # theory_svg
head = head.replace("</style>", """
.four { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px }
@media (max-width: 820px) { .four { grid-template-columns: repeat(2, minmax(0, 1fr)) } }
.four .card img { aspect-ratio: 1 / 1; object-fit: cover }
.four .card .foot { font-size: 12px }
section.block > p { max-width: 76ch }
.kv { display: grid; grid-template-columns: auto 1fr; gap: 4px 14px; font-size: 13.5px; max-width: 76ch }
.kv dt { font: 500 13px var(--mono); color: var(--muted); white-space: nowrap } .kv dd { margin: 0 }
.drive { background: var(--panel); border: 1px solid var(--line); border-radius: 10px; overflow: hidden; display: grid }
.drive svg { display: block; width: 100%; height: auto; background: var(--panel) }
.drive svg text { font: 500 11px var(--mono); fill: var(--fg) } .drive svg .lbl { fill: var(--muted) }
.drive .bar { display: flex; flex-wrap: wrap; gap: 8px 14px; align-items: center; padding: 10px 12px; border-top: 1px solid var(--line) }
.drive .bar .seg { display: inline-flex; border: 1px solid var(--line); border-radius: 7px; overflow: hidden }
.drive .bar .seg button { font: 500 12.5px/1 var(--body); padding: 8px 10px; background: transparent; color: var(--fg); border: 0; cursor: pointer }
.drive .bar .seg button + button { border-left: 1px solid var(--line) }
.drive .bar .seg button[aria-pressed="true"] { background: var(--petg); color: #fff }
.drive .bar label { font: 500 11px var(--mono); color: var(--muted); letter-spacing: .05em; text-transform: uppercase }
.drive .bar .btn { font: 500 12.5px/1 var(--body); padding: 8px 12px; background: transparent; color: var(--fg); border: 1px solid var(--line); border-radius: 7px; cursor: pointer }
.drive .bar .btn[aria-pressed="true"] { border-color: var(--petg); color: var(--petg) }
.drive input[type=range] { flex: 1 1 160px; min-width: 120px; accent-color: var(--petg) }
.drive .status { padding: 10px 12px; font-size: 13.5px; border-top: 1px solid var(--line); min-height: 2.6em }
.drive .status b { font: 500 13px var(--mono); color: var(--muted); margin-right: 8px }
.drive .sliders { display: grid; grid-template-columns: repeat(auto-fit, minmax(210px, 1fr)); gap: 6px 16px; padding: 10px 12px; border-top: 1px solid var(--line) }
.drive .sliders label { display: grid; grid-template-columns: 1fr auto; align-items: center; gap: 2px 8px; font: 500 11px var(--mono); color: var(--muted); letter-spacing: .03em }
.drive .sliders label input { grid-column: 1 / -1; width: 100%; accent-color: var(--petg); margin: 0 }
.drive .sliders label span { color: var(--fg) }
.drive .sliders label input:disabled { opacity: .35 }
</style>""")
MOD = "/home/user/biobuzz/cad/modules/flower-stick-proto/"
MODELS = [  # key, stl b64 file, name, blurb, shaftZ mm, shaftBack mm
    ("peak", "v3_peak", "v3 mountain, scoop", "55° up, 1.28 in peak, 45° down; Carter's scoop", 6.35, 19.6),
    ("v3plain", "v3_plain", "v3 mountain, plain", "the same with no hollow", 6.35, 19.6),
    ("scoop", "v2_scoop", "v2 scoop", "45° nose, lane and shoulders; on Bob's plate", 6.35, 17.5),
    ("spoon", "v2_spoon", "v2 spoon", "the bowl reading of the divot; on Bob's plate", 6.35, 17.5),
    ("steep", "v2_steep", "v2 steep", "55° nose on the v2 body", 6.35, 19.05),
    ("blockA", "blockA", "Block A, 15 Oct", "steep face first, ramp toward the robot", 6.35, 12.0),
]
stl_js = "const STL = {" + ", ".join(f'{k}: "{b64(f)}"' for k, f, *_ in MODELS) + "};"
info_js = "const INFO = {" + ", ".join(f'{k}: {{ name: "{n}", shaftZ: {sz}, shaftBack: {sb} }}' for k, f, n, d, sz, sb in MODELS) + "};"
buttons = "\n".join(f'      <button id="pick-{k}" aria-pressed="{"true" if i == 0 else "false"}">{n}<small>{d}</small></button>' for i, (k, f, n, d, *_ ) in enumerate(MODELS))
rows_js = '''const ROWS = {
  peak: [["Profile", "55° nose from a 0.53 in edge, peak 1.28 in at 0.65 ahead of the FLOWER's centre, 45° down to the back edge at -0.12"], ["Width", "5.4 in, one profile across; scoop 1.8 in wide, 0.21 deep"], ["Shaft", "8 mm hex at 0.77 in, 8.8 mm of plastic over it"], ["Size", "137 × 46 × 19 mm"]],
  v3plain: [["Profile", "as v3 mountain"], ["Hollow", "none"], ["Size", "137 × 46 × 19 mm"]],
  scoop: [["Profile", "45° nose from a 0.60 in edge, ridge 1.05, 8° run, 38° down; shoulders 1.25"], ["Hollow", "arc in the front edge, 1.8 in wide"], ["Size", "137 × 49 × 19 mm"]],
  spoon: [["Profile", "as v2 scoop"], ["Hollow", "bowl in the nose face, 1.8 in wide, 0.15 deep"], ["Size", "137 × 49 × 19 mm"]],
  steep: [["Profile", "55° nose from a 0.52 in edge, ridge 1.05, 4° run, 38° down"], ["Hollow", "scoop"], ["Size", "137 × 50 × 19 mm"]],
  blockA: [["As driven", "steep curved face toward the POLLEN, ramp toward the robot"], ["Steep face", "leans 17°: the push is tilted up only 17°, it stalls"], ["Ramp out", "flat top at 1.30, 41° down to a sharp edge at 0.52"], ["Size", "36 × 69 × 20 mm"]],
};'''
js_src = src[src.index('<script src="https://cdnjs'):src.index("</script>\n'''") + len("</script>")]
js_src = js_src[:js_src.index("const ROWS = {{")] + rows_js.replace("{", "{{").replace("}", "}}") + js_src[js_src.index("}};\nfunction pick(k)") + 3:]
js_src = js_src.replace('pick("scoop");', 'pick("peak");').replace("new THREE.Mesh(geos.scoop, mat)", "new THREE.Mesh(geos.peak, mat)")
js = eval("f'''" + js_src + "'''")
import json
flower_section = json.load(open(os.path.join(S, "flower_section.json")))
robot_section = json.load(open(os.path.join(S, "robot2", "robot_section.json")))
anim_js = open(os.path.join(S, "sim.js")).read() + open(os.path.join(S, "anim2.js")).read().replace("__FLOWER__", json.dumps(flower_section, separators=(",", ":"))).replace("__ROBOT__", json.dumps(robot_section, separators=(",", ":")))
sim_table = "\n".join(l for l in open(os.path.join(S, "sim_out.txt")).read().strip().splitlines()[1:] if not l.startswith("trace:"))

body = f'''
<div class="wrap">
<header>
  <span class="eyebrow">Mona Shores FTC 19429 / 20245 · BIOBUZZ · issue #179 · updated 16 Oct 2026</span>
  <h1>The FLOWER stick</h1>
  <p class="lede">A blade on a short arm under the intake. The robot drives into a FLOWER, the blade gets under the bottom POLLEN,
  and all four roll out of the FLOWER straight into the intake. What the lab built and tested, why it stalled, what the field CAD
  and the mentor's robot CAD fix, and the blade to print next. The 3D models are the print files.</p>
  <nav class="toc" aria-label="Sections">
    <a href="#concept">Concept</a><a href="#journey">How we got here</a><a href="#test">15 Oct test</a><a href="#jam">Why it stalled</a>
    <a href="#flower">The FLOWER</a><a href="#blade">The blade</a><a href="#drive">Simulator</a><a href="#intake">Into the intake</a><a href="#others">Other teams</a><a href="#next">Next</a>
  </nav>
</header>

<section class="block" id="concept">
  <h2>The concept</h2>
  <p>A FLOWER holds four POLLEN stacked in a cup at its base; the bottom one sits on the tiles inside a 3.08 in hole in a 0.35 in
  ring, against two uprights at the back. The blade drives in <strong>steep face first, ramp toward the robot</strong>. Once its top
  is under the bottom ball, the ball rides up and rolls down the ramp, straight out of the FLOWER perpendicular to the wall, into
  the intake. The stack drops one and the next ball follows. The FLOWER itself is the depth stop.</p>
  <div class="card"><h3>Frame by frame</h3><img src="data:image/png;base64,{b64f(MOD + "stack_sequence.png")}" alt="Six side sections: the block approaching steep face first, meeting the ball below its middle, the ball up on the flat top, rolling down the ramp away from the wall, the next ball following, all four out."><div class="foot">Schematic, in the FLOWER's slice from the field CAD, with the 15 Oct block. The real block stalls at frame 2; a wiggle gets it past.</div></div>
</section>

<section class="block" id="journey">
  <h2>How we got here</h2>
  <ul>
    <li><strong>Ramp hook, then Blocks A and B.</strong> The hook's block printed at two heights (small wheels, 48 mm wheels), 8 mm hex bore, first for a fixed grid-plate mount.</li>
    <li><strong>10 Oct, the sled.</strong> The fixed mount fought the lab on height; two pattern-beam arms pivot off the front and the block's shaft rides on small wheels at 0.77 in.</li>
    <li><strong>15 Oct, the test with Carter.</strong> Two blocks side by side, pushed by hand. It emptied the FLOWER but only with a back-and-forth, and never seated. Carter sketched one blade with a divot.</li>
    <li><strong>16 Oct, the field CAD and v2.</strong> FIRST's FLOWER measured from its STEP, the stall explained, a wedge entry, Carter's divot both ways. A full-width ramp was drawn and dropped the same day: it put a sheet in front of the intake.</li>
    <li><strong>16 Oct, v3 and the real intake.</strong> The flat on v2 was the shaft's fault; v3 is one mountain. The mentor's 16 Oct robot CAD puts the intake's mouth on the floor at the front, which is what the blade needs.</li>
  </ul>
</section>

<section class="block" id="test">
  <h2>The 15 Oct test</h2>
  <div class="two">
    <div class="card"><h3>Two blocks at the FLOWER</h3><img src="data:image/jpeg;base64,{b64("ph")}" alt="Two blue printed blocks side by side on the shaft at the base of a loaded FLOWER."><div class="foot">Curved steep faces toward the FLOWER, ramps toward the robot. Carter and a mentor pushed the sled in by hand; three runs on video.</div></div>
    <div class="card"><h3>One run</h3><img src="data:image/jpeg;base64,{b64("frames")}" alt="Three frames: the sled approaching, the blocks under the POLLEN, four POLLEN on the tiles."><div class="foot">Once under, the stack came out in about a second. Getting under took 4 to 6 s of working back and forth, and it never reached the uprights.</div></div>
  </div>
  <ul>
    <li><strong>One block alone was hard to get under;</strong> two side by side went under, the ball in the valley between their curved faces.</li>
    <li><strong>It would not seat against the back.</strong> With a curved tip the block passes the uprights' forward points and finds its own depth; the stop was never the same twice.</li>
    <li><strong>The simulator adds one thing:</strong> with this block the first ball comes down the ramp onto the ring's inner edge under the stack, and only a back-out frees it. The wiggle was doing two jobs.</li>
  </ul>
</section>

<section class="block theory" id="jam">
  <h2>Why it stalled</h2>
  <p>While the blade pushes, the bottom POLLEN is pinched between the blade's face and the uprights behind it, both with friction.
  Write the force balance with friction μ at both contacts and the blade's contact normal tilted up by α: a steady push lifts the
  ball only if</p>
  <p><span class="formula">tan α &gt; 2μ / (1 − μ²), i.e. α &gt; 2·atan μ</span> &nbsp; (33° at μ 0.3, 44° at μ 0.4; half that if the ball rolls on the blade)</p>
  {theory_svg()}
  <div class="callout">Block A's steep face leans 17°, so the push is tilted up only 17°. That lifts the ball only if μ is under
  0.15 with the ball sliding on both contacts, or under 0.31 if it rolls on the blade and slides on the uprights. PETG on the
  POLLEN is about 0.3 to 0.4, so it sits at the edge and stalls under a steady push, and goes on only when jolted. The simulator never showed
  it: its blade is driven by position and cannot stall. The ramp out was right; the entry must be a wedge, not a wall.</div>
  <p><strong>The levers, in order:</strong> a steep wedge entry (55°: good to μ 0.52); the edge low and sharp, since the edge height
  sets the first contact (0.53 in: 38°); a slippery skin (UHMW tape, μ 0.1 to 0.2); and the intake's roller, which pulls the ball
  and removes the limit once the ball reaches it.</p>
</section>

<section class="block" id="flower">
  <h2>The FLOWER, from FIRST's field CAD</h2>
  <p>Measured from the published STEP about the bottom POLLEN's centre, +x toward the wall. These numbers set the blade.</p>
  <div class="tablewrap"><table class="cmp">
    <tr><th>feature</th><th>where</th><th>what it sets</th></tr>
    <tr><td>base ring</td><td class="n">0.35 in tall, hole 3.08, front edge at −2.36</td><td>the blade's bottom at 0.52 clears it by 0.17</td></tr>
    <tr><td>uprights' forward points</td><td class="n">x 1.18, y ±1.42</td><td>the stop: anything wider than 2.84 in hits both and squares up</td></tr>
    <tr><td>uprights' inner corners</td><td class="n">(1.61, ±0.61)</td><td>a lifted POLLEN is pinned at x 0.35: the peak must sit past it</td></tr>
    <tr><td>bracket and its lintel</td><td class="n">3.90 to 5.25 up, front edge at −2.41</td><td>no roller fits inside the FLOWER; a ball leaves with at most 1.1 in of lift</td></tr>
    <tr><td>pipes</td><td class="n">(±1.72, ±1.72), from 4.25 up</td><td>nothing in front of the uprights below 4.25 in</td></tr>
  </table></div>
</section>

<section class="block" id="blade">
  <h2>The blade: v3, the mountain</h2>
  <div class="card"><h3>Three profiles at their stop</h3><img src="data:image/png;base64,{b64f(MOD + "profiles_compare.png")}" alt="Three side sections at the stop with the pinned POLLEN resting on each profile: Block A, the v2 lane, and the v3 mountain."><div class="foot">Block A stops 0.57 in deep with the ball over its ramp's foot. v2 and v3 stop on the uprights' points with the ball 0.3 in down the back slope, ready to roll.</div></div>
  <ul>
    <li><strong>One mountain, no flat.</strong> v2's 8° run existed only to leave 1.5 mm of skin over the 8 mm shaft. With a 55° nose the peak rises to 1.28 in at the same 0.30 in past the pinned ball and the shaft gets 8.8 mm over it, so the flat goes.</li>
    <li><strong>The peak height is capped by one rule:</strong> the peak sits at least 0.30 in past the pinned ball so the ball is on the down slope at the stop. 55° allows 1.28 in, 60° 1.44. The back angle hardly matters: the exit speed comes from the drop, about 19 in/s from 1.28 in.</li>
    <li><strong>One profile the full width, so no centring.</strong> The nose meets both forward points anywhere within ±1.3 in of centre (±2 in at 7 in wide), and the ramp is the same wherever the ball sits. The scoop is Carter's option; if the plain blade seats as well on the 17th, it goes.</li>
  </ul>
  <div class="viewer">
    <canvas id="stage" aria-label="3D model of the selected blade. Drag to turn it, scroll or pinch to zoom."></canvas>
    <div class="pick" role="group" aria-label="Which blade">
{buttons}
    </div>
    <dl class="spec" id="spec"></dl>
  </div>
  <p>All on the branch as STL; Bob's plate still holds v2 scoop and spoon. v3 is the one to print next, the scoop and plain side by side on one plate.</p>
</section>

<section class="block" id="drive">
  <h2>Drive in: the simulator, live</h2>
  <p>The 2-D simulator runs here, in the page, in about a second. The FLOWER is its own section from FIRST's field CAD; the robot's
  front is the mentor's 16 Oct CAD cut at the centreline, with the first Gecko wheel where it sits. The blade is on its arm.
  The POLLEN have friction, bounce and a hollow ball's inertia. A stall check stops the drive when the push at the blade
  contact is tilted up less than the friction angle (the ball rolling on the blade, sliding on the uprights). The Gecko is
  modelled as what it is, a soft wheel that drags anything within its reach toward the robot. Pick a preset or move the
  sliders, then Run.</p>
  <div class="drive">
    <svg id="sim-svg" viewBox="0 0 697 452" role="img" aria-label="Side view animation of the blade driving into the FLOWER and the POLLEN rolling out into the intake."></svg>
    <div class="bar">
      <label>Preset</label>
      <span class="seg"><button data-preset="blockA" aria-pressed="false">Block A</button><button data-preset="v2" aria-pressed="false">v2 lane</button><button data-preset="v3" aria-pressed="false">v3 mountain</button><button data-preset="v4" aria-pressed="true">v4 carry</button></span>
      <button class="btn" id="sim-run">Run</button>
      <button class="btn" id="sim-play">Play</button>
      <button class="btn" id="sim-slow" aria-pressed="false">Slow</button>
      <input type="range" id="sim-scrub" min="0" max="1" value="0" aria-label="Scrub through the run">
      <b id="sim-clock" style="font: 500 13px var(--mono); color: var(--muted)">0.00 s</b>
    </div>
    <div class="sliders">
      <label>edge height <input type="range" id="s-edge" min="0.45" max="0.70" step="0.01" value="0.53"><span id="v-edge"></span> in</label>
      <label>nose angle <input type="range" id="s-nose" min="35" max="70" step="1" value="55"><span id="v-nose"></span>°</label>
      <label>peak height <input type="range" id="s-peak" min="0.9" max="1.6" step="0.02" value="1.28"><span id="v-peak"></span> in</label>
      <label>tail angle <input type="range" id="s-tail" min="8" max="45" step="1" value="14"><span id="v-tail"></span>°</label>
      <label>drive speed <input type="range" id="s-drive" min="2" max="12" step="1" value="4"><span id="v-drive"></span> in/s</label>
      <label>friction μ <input type="range" id="s-mu" min="0.2" max="0.5" step="0.05" value="0.4"><span id="v-mu"></span></label>
      <label>stop error <input type="range" id="s-stopErr" min="-0.3" max="0.3" step="0.05" value="0"><span id="v-stopErr"></span> in</label>
    </div>
    <dl class="spec" style="border-top: 1px solid var(--line)">
      <dt>peak past the pinned ball</dt><dd id="ro-margin"></dd>
      <dt>first contact at the edge</dt><dd id="ro-edge"></dd>
      <dt>tail ends at</dt><dd id="ro-tail"></dd>
    </dl>
    <div class="status"><span id="sim-result">…</span></div>
  </div>
  <ul>
    <li><strong>Block A stalls</strong> the moment its face meets the ball, at every speed and friction: the push there is tilted up 14° to 17°.</li>
    <li><strong>v3 works but dips:</strong> the ball drops off the back edge into the hole and climbs the ring on its own speed. <strong>v4 carries</strong> it on a 14° tail that ends over the ring, so the ball reaches the Gecko without touching the tiles, and the stop error slider shows how much depth error it tolerates.</li>
    <li><strong>The three numbers under the sliders are the design rules:</strong> the peak must land past the pinned ball (0.3 in or more), the edge sets the first-contact angle and so the friction it can beat, and the tail should end past the ring.</li>
    <li><strong>Not modelled:</strong> rolling resistance, the sideways motion of a real stack, the print's edge radius. The 17 Oct test answers those.</li>
  </ul>
</section>

<section class="block" id="intake">
  <h2>Into the intake</h2>
  <p>The blade only releases the ball; the intake has to take it. A sheet or ramp in front of the intake does not work: it moves the
  problem further from the FLOWER and leaves the ball on a shelf. The mentor's 16 Oct robot CAD makes the real answer simple.</p>
  <dl class="kv">
    <dt>first roller</dt><dd>seven 48 mm Gecko wheels on one 240 mm REX shaft, axle 2.9 in high, wheel bottoms 1.94 in off the floor, rims flush with the robot's front face; 7.5 in wide</dd>
    <dt>intake opening</dt><dd>9.8 in between the uprights; second stage two 96 mm Geckos 6.3 to 10.1 in back, low</dd>
    <dt>so the mouth</dt><dd>is the floor itself, at the front: the good case</dd>
    <dt>the FLOWER then sets</dt><dd>robot face 2.5 in from the FLOWER's centre (0.1 in off the ring and bracket); blade stop 3.7 in ahead of the face; blade shaft 2.9 in ahead at 0.77 in, passing 0.66 in under the Gecko row; arm 4.4 in from the Gecko shaft to the blade shaft, or pivots on the uprights beside the outer wheels</dd>
    <dt>watch</dt><dd>the servo-driven 4.5 in disc at the front left clears the FLOWER only while the robot is within about 1 in of centred on it</dd>
  </dl>
</section>

<section class="block" id="others">
  <h2>Other teams</h2>
  <div class="four">
    <div class="card"><h3>19705 WXYZ, Ri3D</h3><img src="data:image/jpeg;base64,{b64f("clips/wxyz.jpg")}" alt="A wide red ramp with a thin lip and a row of compliant wheels behind it, on pivot arms."><div class="foot">Thin lip, driven wheels right behind it, folds up. Their floor intake's lip.</div></div>
    <div class="card"><h3>CrossFire 9968</h3><img src="data:image/jpeg;base64,{b64f("clips/crossfire.jpg")}" alt="A pattern beam ending in a bent plate wedge under a FLOWER's stack, a string roller behind it."><div class="foot">Bent sheet wedge on a beam, string roller behind, hand-pushed like our sled.</div></div>
    <div class="card"><h3>Hexadecimal Nibble</h3><img src="data:image/jpeg;base64,{b64f("clips/hexnibble.jpg")}" alt="A large yellow printed wedge on a pattern beam in front of a loaded FLOWER."><div class="foot">A passive wedge as tall as a ball; it climbs into the robot.</div></div>
    <div class="card"><h3>Eaglebots 13532</h3><img src="data:image/jpeg;base64,{b64f("clips/eaglebots.jpg")}" alt="A plywood robot parked beside a FLOWER with a servo arm swinging a wedge through its base."><div class="foot">Parks against the wall, a servo arm sweeps the base sideways: nothing to pinch against.</div></div>
  </div>
  <p>Two recipes besides ours: a lip with a powered roller right behind it, and a sideways sweep. Every wedge meets the ball low. The
  roller is the idea that carries over: it is why the intake's first wheels sit at the FLOWER's face.</p>
</section>

<section class="block" id="next">
  <h2>Next</h2>
  <ul>
    <li><strong>17 Oct, the sled test:</strong> five straight pushes and one 1 in off-centre per blade, counting POLLEN out and timing from the side. Does it seat with one steady push? If it still wants a wiggle, tape the nose first.</li>
    <li><strong>Print v3</strong>, scoop and plain on one plate, and ask Carter which hollow they drew.</li>
    <li><strong>Hang the blade under the intake:</strong> 4.4 in arms from the Gecko shaft or pivots on the uprights, the blade's shaft 2.9 in ahead of the face at 0.77 in. Then the test is the robot driving in, not a hand pushing.</li>
    <li><strong>What is the 4.5 in servo disc at the front left?</strong> If it is a FLOWER tool, this page changes.</li>
  </ul>
  <p style="color: var(--muted); font-size: 13px">Files: <code>cad/modules/flower-stick-proto/</code> on <code>spike/179-flower-stick-proto</code>. Every blade is
  <code>divot_blade.py</code> with flags (the README lists them); simulation in <code>tools/ramp-hook/</code>.</p>
</section>
</div>
{js}
{anim_js}
'''
out = os.path.join(S, "flower-stick.html"); open(out, "w").write(head + body); print(out, len(head + body))
