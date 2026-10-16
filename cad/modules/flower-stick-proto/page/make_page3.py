"""The FLOWER stick page, third pass: the simulator first, the model evolution does the talking, everything else short."""
import os, json
S = os.path.dirname(os.path.abspath(__file__))
src2 = open(os.path.join(S, "make_page2.py")).read()
exec(src2[:src2.index('MOD = "/home/user')])                 # head (with CSS), b64, b64f, theory_svg
MOD = "/home/user/biobuzz/cad/modules/flower-stick-proto/"
head = head.replace("</style>", """
.hero { display: grid; gap: 10px }
.views { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1fr); gap: 0 }
.views > * { min-width: 0 }
.views svg { border-right: 1px solid var(--line) }
@media (max-width: 900px) { .views { grid-template-columns: minmax(0, 1fr) } .views svg { border-right: 0; border-bottom: 1px solid var(--line) } }
#drive3d { display: block; width: 100%; aspect-ratio: 697 / 452; touch-action: none; cursor: grab; background: var(--panel) }
.viewlbl { position: absolute; top: 8px; left: 10px; font: 500 11px var(--mono); letter-spacing: .05em; text-transform: uppercase; color: var(--muted); pointer-events: none }
.views > div { position: relative }
.evo { display: grid; grid-template-columns: repeat(auto-fit, minmax(190px, 1fr)); gap: 10px }
.evo .card { padding: 12px 14px; display: block }
.evo .card b { font: 600 14px var(--display); letter-spacing: .02em; display: block; margin-bottom: 4px }
.evo .card span { font-size: 13px; color: var(--muted) }
.evo .card.now { border-color: var(--petg) }
.rules { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 10px }
.rules .card { padding: 12px 14px; display: block; font-size: 13.5px }
.rules .card b { display: block; font: 500 12px var(--mono); color: var(--petg); letter-spacing: .05em; text-transform: uppercase; margin-bottom: 4px }
</style>""")
V5 = json.load(open(os.path.join(S, "v5.json"))) if os.path.exists(os.path.join(S, "v5.json")) else None
MODELS = [
    ("v4", "v4_carry", "v4 carry", "55° nose, 1.28 in peak, 14° tail to the ring", 6.3, 19.1),
    ("peak", "v3_peak", "v3 mountain, scoop", "55° up, 1.28 in peak, 45° down; Carter's scoop", 6.3, 19.1),
    ("scoop", "v2_scoop", "v2 scoop", "45° nose, lane and shoulders; on Bob's plate", 6.3, 17.2),
    ("spoon", "v2_spoon", "v2 spoon", "the bowl reading of the divot; on Bob's plate", 6.3, 17.2),
    ("blockA", "blockA", "Block A, 15 Oct", "steep face first, ramp toward the robot", 6.0, 10.9),
]
if V5:
    MODELS.insert(0, ("v5s", "v5_scoop", "v5 with Carter's scoop", "the same blade with the scoop in the nose", 6.3, 13.1))
    MODELS.insert(0, ("v5", "v5", "v5 plain", V5["blurb"], 6.3, 13.1))
stl_js = "const STL = {" + ", ".join(f'{k}: "{b64(f)}"' for k, f, *_ in MODELS) + "};"
info_js = "const INFO = {" + ", ".join(f'{k}: {{ name: "{n}", shaftZ: {sz}, shaftBack: {sb} }}' for k, f, n, d, sz, sb in MODELS) + "};"
buttons = "\n".join(f'      <button id="pick-{k}" aria-pressed="{"true" if i == 0 else "false"}">{n}<small>{d}</small></button>' for i, (k, f, n, d, *_ ) in enumerate(MODELS))
rows = {
    "v5": [["Profile", V5["profile_text"] if V5 else ""], ["Width", "5.4 in, one profile across"], ["Shaft", "8 mm hex at 0.77 in"], ["Size", V5["size"] if V5 else ""]],
    "v5s": [["Profile", "as v5 plain"], ["Hollow", "Carter's scoop: an arc in the front edge, 1.8 in wide, 0.21 deep"], ["Size", V5["size"] if V5 else ""]],
    "v4": [["Profile", "55° nose from a 0.53 in edge, peak 1.28 in, 14° tail down to the blade's bottom 2.3 in in front of the FLOWER's centre, over the ring"], ["Width", "5.4 in, one profile across"], ["Shaft", "8 mm hex at 0.77 in, 8.8 mm of plastic over it"], ["Size", "137 × 102 × 19 mm"]],
    "peak": [["Profile", "55° nose from a 0.53 in edge, peak 1.28 in, 45° down to the back edge at -0.12"], ["Hollow", "scoop 1.8 in wide, 0.21 deep"], ["Size", "137 × 46 × 19 mm"]],
    "scoop": [["Profile", "45° nose from a 0.60 in edge, ridge 1.05, 8° run, 38° down; shoulders 1.25"], ["Hollow", "arc in the front edge, 1.8 in wide"], ["Size", "137 × 49 × 19 mm"]],
    "spoon": [["Profile", "as v2 scoop"], ["Hollow", "bowl in the nose face, 1.8 in wide, 0.15 deep"], ["Size", "137 × 49 × 19 mm"]],
    "blockA": [["As driven", "steep curved face toward the POLLEN, ramp toward the robot"], ["Steep face", "leans 17°: the push is tilted up only 17°, it stalls"], ["Ramp out", "flat top at 1.30, 41° down to a sharp edge at 0.52"], ["Size", "36 × 69 × 20 mm"]],
}
rows_js = "const ROWS = " + json.dumps({k: v for k, v in rows.items() if any(m[0] == k for m in MODELS)}) + ";"
src = open(os.path.join(S, "make_page.py")).read()
js_src = src[src.index('<script src="https://cdnjs'):src.index("</script>\n'''") + len("</script>")]
js_src = js_src[:js_src.index("const ROWS = {{")] + rows_js.replace("{", "{{").replace("}", "}}") + js_src[js_src.index("}};\nfunction pick(k)") + 3:]
first = MODELS[0][0]
js_src = js_src.replace('pick("scoop");', f'pick("{first}");').replace("new THREE.Mesh(geos.scoop, mat)", f"new THREE.Mesh(geos.{first}, mat)")
js = eval("f'''" + js_src + "'''")
flower_section = json.load(open(os.path.join(S, "flower_section.json")))
robot_section = json.load(open(os.path.join(S, "robot2", "robot_section.json")))
anim_js = open(os.path.join(S, "sim.js")).read() + open(os.path.join(S, "anim2.js")).read().replace("__FLOWER__", json.dumps(flower_section, separators=(",", ":"))).replace("__ROBOT__", json.dumps(robot_section, separators=(",", ":")))
if V5:
    anim_js = anim_js.replace('    v4: { name: "v4 carry", shape: { edge: 0.53, nose: 55, peak: 1.28, tail: 14.4 }, shaftBack: 0.77, onRobot: true },',
                              '    v4: { name: "v4 carry", shape: { edge: 0.53, nose: 55, peak: 1.28, tail: 14.4 }, shaftBack: 0.77, onRobot: true },\n    v5: { name: "v5", shape: ' + json.dumps(V5["shape"]) + ', shaftBack: 0.77, onRobot: true },')
    anim_js = anim_js.replace('choose("v4");', 'choose("v5");')
drive3d = open(os.path.join(S, "drive3d.js")).read().replace("__FLOWER3D__", open(os.path.join(S, "flower3d.b64")).read())
v5_button = '<button data-preset="v5" aria-pressed="true">v5</button>' if V5 else ''
v4_pressed = "false" if V5 else "true"
evo_v5 = f'''<div class="card now"><b>v5, searched</b><span>{V5["evo"]}</span></div>''' if V5 else ''

body = f'''
<div class="wrap">
<header class="hero">
  <span class="eyebrow">Mona Shores FTC 19429 / 20245 · BIOBUZZ · issue #179 · 16 Oct 2026</span>
  <h1>The FLOWER stick</h1>
  <p class="lede">A blade on two arms under the intake. The robot drives into a FLOWER until the FLOWER stops it, the blade gets
  under the bottom POLLEN, and all four roll out over the blade into the intake. This page is for Bob, who prints, and Carter,
  whose sketch started the blade: try the simulator, see how the shape got here, and tell us what to print next.</p>
</header>

<section class="block" id="drive">
  <h2>Try it</h2>
  <p>One run of the 2-D simulator, drawn twice: a side view with the FLOWER from FIRST's field CAD and the robot's front from the
  mentor's 16 Oct CAD cut at the centreline, and a 3D view you can orbit. Pick a blade, or drag a slider and it reruns. The
  readouts under the sliders are the rules the shape has to obey. The scoop slider cuts Carter's divot into the 3D blade (the 2-D
  physics runs on the centreline and does not see it); the width and arms choices change only the 3D picture, so you can see
  what fits beside the corner disc.</p>
  <div class="drive">
    <div class="views">
      <div><svg id="sim-svg" viewBox="0 0 697 452" role="img" aria-label="Side view animation of the blade driving into the FLOWER and the POLLEN rolling out into the intake."></svg><span class="viewlbl">side view</span></div>
      <div><canvas id="drive3d" aria-label="3D view of the same run: the robot's intake, the blade on its arms, the FLOWER and the POLLEN. Drag to orbit."></canvas><span class="viewlbl">3D, drag to orbit</span></div>
    </div>
    <div class="bar">
      <label>Blade</label>
      <span class="seg">{v5_button}<button data-preset="v4" aria-pressed="{v4_pressed}">v4 carry</button><button data-preset="v3" aria-pressed="false">v3 mountain</button><button data-preset="v2" aria-pressed="false">v2 lane</button><button data-preset="blockA" aria-pressed="false">Block A</button></span>
      <button class="btn" id="sim-play">Play</button>
      <button class="btn" id="sim-slow" aria-pressed="false">Slow</button>
      <input type="range" id="sim-scrub" min="0" max="1" value="0" aria-label="Scrub through the run">
      <b id="sim-clock" style="font: 500 13px var(--mono); color: var(--muted)">0.00 s</b>
      <button class="btn" id="sim-run" title="The sliders run on their own; this replays">Run</button>
    </div>
    <div class="sliders">
      <label>edge height <input type="range" id="s-edge" min="0.52" max="0.70" step="0.01" value="0.53"><span id="v-edge"></span> in</label>
      <label>nose angle <input type="range" id="s-nose" min="35" max="70" step="1" value="55"><span id="v-nose"></span>°</label>
      <label>peak height <input type="range" id="s-peak" min="0.9" max="1.6" step="0.02" value="1.28"><span id="v-peak"></span> in</label>
      <label>tail angle <input type="range" id="s-tail" min="8" max="45" step="1" value="14"><span id="v-tail"></span>°</label>
      <label>drive speed <input type="range" id="s-drive" min="2" max="12" step="1" value="4"><span id="v-drive"></span> in/s</label>
      <label>friction μ <input type="range" id="s-mu" min="0.2" max="0.5" step="0.05" value="0.4"><span id="v-mu"></span></label>
      <label>stop error <input type="range" id="s-stopErr" min="-0.4" max="0.3" step="0.05" value="0"><span id="v-stopErr"></span> in</label>
      <label>Carter's scoop, depth <input type="range" id="s-scoop" min="0" max="0.4" step="0.05" value="0"><span id="v-scoop"></span> in</label>
    </div>
    <div class="bar" style="border-top: 0">
      <label>Blade width</label>
      <span class="seg"><button data-width="5.4" aria-pressed="true">5.4 in, as printed</button><button data-width="7.5" aria-pressed="false">7.5 in, the Gecko row</button><button data-width="12" aria-pressed="false">12 in</button></span>
      <label>Arms</label>
      <span class="seg"><button data-arms="uprights" aria-pressed="false">on the intake uprights</button><button data-arms="left" aria-pressed="false">one, outboard left</button><button data-arms="both" aria-pressed="true">both outboard</button></span>
    </div>
    <dl class="spec" style="border-top: 1px solid var(--line)">
      <dt>peak past the pinned ball</dt><dd id="ro-margin"></dd>
      <dt>first contact at the edge</dt><dd id="ro-edge"></dd>
      <dt>tail ends at</dt><dd id="ro-tail"></dd>
      <dt>blade width</dt><dd id="ro-width"></dd>
    </dl>
    <div class="status"><span id="sim-result">…</span></div>
  </div>
  <p>What is in the model: the POLLEN with friction, bounce and a hollow ball's inertia; the blade driven in at the chosen speed
  until the uprights stop it; a stall check that halts the drive when the push at the blade contact is tilted up less than the
  friction angle; the Gecko as a soft wheel that drags anything in its reach. What is not: rolling resistance, the print's edge
  radius, and the sideways motion of a real stack. The 17 Oct test answers those. The 3D robot is simple solids at the CAD's
  positions, not the CAD itself.</p>
</section>

<section class="block" id="evolution">
  <h2>How the blade got here</h2>
  <p>Five shapes in two days. Each one fixed the thing the one before it taught us.</p>
  <div class="evo">
    <div class="card"><b>Block A, 15 Oct</b><span>Steep face first, ramp toward the robot. Emptied the FLOWER by hand only with a back-and-forth. Its face leans 17°: the push cannot lift the ball, it stalls. Its curved tip finds its own depth.</span></div>
    <div class="card"><b>v2 lane</b><span>From the field CAD: a 45° wedge entry, the uprights' forward points as the stop, the peak 0.3 in past the pinned ball, Carter's divot. Works in the simulator; the flat run over the shaft is a weakness.</span></div>
    <div class="card"><b>v3 mountain</b><span>55° nose lets the peak rise to 1.28 in with the shaft safely under it: one profile the full width, no flat, no centring. The ball drops into the hole and climbs the ring on its own speed.</span></div>
    <div class="card"><b>v4 carry</b><span>v3 with a 14° tail that ends over the ring: the ball rides the blade all the way to the Gecko. Feeds all four in 2.1 s at 4 in/s. A stop 0.3 in short puts the ball on the peak.</span></div>
    {evo_v5}
  </div>
  <div class="card"><h3>Four profiles at their stop</h3><img src="data:image/png;base64,{b64f(MOD + "profiles_compare.png")}" alt="Side sections at the stop with the pinned POLLEN resting on each profile: Block A, v3, v4 and v5."><div class="foot">The pinned POLLEN resting on each profile at its stop, in the FLOWER from the field CAD. Block A stops 0.57 in deep with the ball over its ramp's foot; the others stop on the uprights' points with the ball past the peak, by 0.30 in on v3 and v4, 0.54 on v5.</div></div>
  <div class="rules">
    <div class="card"><b>the stop</b>The uprights' forward points, 1.18 in behind the FLOWER's centre, 2.84 in apart. A straight nose wider than that hits both and squares up, anywhere within ±1.3 in of centre.</div>
    <div class="card"><b>the margin</b>A lifted POLLEN is pinned 0.35 in behind the centre by the uprights' inner corners. The peak must sit past it, or the ball rests on the peak when the robot stops.</div>
    <div class="card"><b>the entry</b>A ball pinched against the uprights lifts only if the push is tilted up more than the friction angle. The edge height sets the first contact (0.53 in: 38°); a wall at 17° never lifts.</div>
    <div class="card"><b>the exit</b>The bracket's lintel at 3.90 in allows 1.1 in of lift on the way out, and no roller fits inside the FLOWER. The intake's Gecko sits at the FLOWER's face; a tail to the ring hands the ball straight to it.</div>
  </div>
</section>

<section class="block theory" id="test">
  <h2>The test that set the rules</h2>
  <div class="two">
    <div class="card"><h3>15 Oct: two blocks at the FLOWER</h3><img src="data:image/jpeg;base64,{b64("ph")}" alt="Two blue printed blocks side by side on the shaft at the base of a loaded FLOWER."><div class="foot">Curved steep faces toward the FLOWER, ramps toward the robot, on the hand sled. Carter and a mentor pushed; three runs on video.</div></div>
    <div class="card"><h3>One run</h3><img src="data:image/jpeg;base64,{b64("frames")}" alt="Three frames: the sled approaching, the blocks under the POLLEN, four POLLEN on the tiles."><div class="foot">Once under, out in about a second. Getting under took 4 to 6 s of back-and-forth, and it never seated against the uprights.</div></div>
  </div>
  <p>The back-and-forth is the physics, not the driver. The bottom POLLEN is pinched between the block's face and the uprights, both
  with friction, and a steady push lifts it only if the push at the blade contact is tilted up by more than the friction angle:
  <span class="formula">α &gt; atan μ</span>. Block A's face leans 17°; at μ 0.3 to 0.4 that is at the limit, so it stalls and
  moves only when jolted. The simulator reproduces it, and adds a second thing the videos show: with this block the first ball
  lands on the ring's edge under the stack, and only a back-out frees it. Hence the wedge entry, the stop on the uprights' points,
  and the tail.</p>
  {theory_svg()}
</section>

<section class="block" id="numbers">
  <h2>The numbers</h2>
  <div class="two">
    <div><h3 style="font: 600 13px var(--mono); color: var(--muted); letter-spacing: .06em; text-transform: uppercase; margin: 0 0 8px">The FLOWER, from FIRST's field CAD</h3>
    <dl class="kv">
      <dt>base ring</dt><dd>0.35 in tall, hole 3.08, front edge 2.36 in ahead of the centre</dd>
      <dt>uprights</dt><dd>forward points at x 1.18, y ±1.42; inner corners at (1.61, ±0.61)</dd>
      <dt>bracket</dt><dd>3.90 to 5.25 in up, front edge at −2.41</dd>
      <dt>pipes</dt><dd>at (±1.72, ±1.72), from 4.25 in up</dd>
    </dl></div>
    <div><h3 style="font: 600 13px var(--mono); color: var(--muted); letter-spacing: .06em; text-transform: uppercase; margin: 0 0 8px">The intake, from the 16 Oct robot CAD</h3>
    <dl class="kv">
      <dt>first roller</dt><dd>seven 48 mm Gecko wheels on one shaft, axle 2.9 in high, bottoms 1.94 in off the floor, rims flush with the face, 7.5 in wide</dd>
      <dt>opening</dt><dd>9.8 in between the uprights; second stage two 96 mm Geckos 6.3 in back</dd>
      <dt>at the FLOWER</dt><dd>face 2.5 in from the centre; blade stop 3.7 in ahead of the face; blade shaft 2.9 in ahead at 0.77 in; arm 4.4 in from the Gecko shaft</dd>
      <dt>corner wheel</dt><dd>the servo-driven 4.5 in disc at the front right, for POLLEN in the wall corner by the garden. It faces forward and spins about a fore-aft axis, so its face sweeps a ball sideways out of the corner toward the intake: 0.5 in thick, 1.2 in ahead of the face, 4.0 to 8.4 in off centre, 2.5 to 6.9 in high. It clears the FLOWER only while the robot is within about 1 in of centred on it.</dd>
      <dt>where the arms pivot</dt><dd>outboard of the drive wheels, 8.3 in off centre: the chassis is 16.0 in wide and the 18 in box allows 9, so there is room past the front omni wheels (which end at 7.1). The blade's shaft then runs 17 in across under the Gecko row (0.77 in high against the wheels' bottoms at 1.94). On the right the arm passes under the corner disc: where the disc sits, 0.7 to 1.2 in ahead of the face, the arm is 1.8 to 2.0 in high and the disc's bottom is 2.5. That is what the 3D view draws.</dd>
    </dl></div>
  </div>
</section>

<section class="block" id="blade">
  <h2>The print files</h2>
  <div class="viewer">
    <canvas id="stage" aria-label="3D model of the selected blade. Drag to turn it, scroll or pinch to zoom."></canvas>
    <div class="pick" role="group" aria-label="Which blade">
{buttons}
    </div>
    <dl class="spec" id="spec"></dl>
  </div>
  <p>The print files, as Bob will see them. v5 plain and v5 scoop are the ones to print next; the rest are here for the record.</p>
</section>

<section class="block" id="others">
  <h2>Other teams</h2>
  <div class="four">
    <div class="card"><h3>19705 WXYZ, Ri3D</h3><img src="data:image/jpeg;base64,{b64f("clips/wxyz.jpg")}" alt="A wide red ramp with a thin lip and a row of compliant wheels behind it, on pivot arms."><div class="foot">Thin lip, driven wheels right behind it, folds up.</div></div>
    <div class="card"><h3>CrossFire 9968</h3><img src="data:image/jpeg;base64,{b64f("clips/crossfire.jpg")}" alt="A pattern beam ending in a bent plate wedge under a FLOWER's stack, a string roller behind it."><div class="foot">Bent sheet wedge on a beam, string roller behind.</div></div>
    <div class="card"><h3>Hexadecimal Nibble</h3><img src="data:image/jpeg;base64,{b64f("clips/hexnibble.jpg")}" alt="A large yellow printed wedge on a pattern beam in front of a loaded FLOWER."><div class="foot">A passive wedge as tall as a ball.</div></div>
    <div class="card"><h3>Eaglebots 13532</h3><img src="data:image/jpeg;base64,{b64f("clips/eaglebots.jpg")}" alt="A plywood robot parked beside a FLOWER with a servo arm swinging a wedge through its base."><div class="foot">Parks against the wall, a servo arm sweeps sideways.</div></div>
  </div>
  <p>Every wedge meets the ball low; the roller is the idea that carries over, and it is why the Gecko sits at the FLOWER's face.</p>
</section>

<section class="block" id="next">
  <h2>Next</h2>
  <ul>
    <li><strong>Bob:</strong> print <code>stl/print_plate_v5.stl</code>, the v5 blade plain and with Carter's scoop, side by side (191 × 137 mm; or the two STLs one at a time). PETG, 5 walls, no supports. Notes in <code>PRINT_ME.md</code>.</li>
    <li><strong>Carter:</strong> the scoop is yours. We built both readings of the sketch and kept the scoop as an option, but we never had the reason for it. If it was to seat the ball the way the valley between the two blocks did, the wedge entry already does that. Tell us what you had in mind, and whether a shallow groove along the tail is closer to it.</li>
    <li><strong>17 Oct on the sled:</strong> both v5 blades, five straight pushes and one 1 in off-centre each, counting POLLEN out and timing from the side. If one wants a wiggle, tape the nose. If the plain blade seats and feeds as well as the scoop, the hollow goes.</li>
    <li><strong>Then on the robot:</strong> arms outboard of the wheels at 8.3 in, a 17 in shaft under the Gecko row, the blade's shaft 2.9 in ahead of the face at 0.77 in. If the intake grows wider and moves ahead of the wheels, the arms shorten.</li>
  </ul>
  <p style="color: var(--muted); font-size: 13px">Everything on this page is on the branch <code>spike/179-flower-stick-proto</code> in <code>cad/modules/flower-stick-proto/</code>: the blades as STL, every one a <code>divot_blade.py</code> flag set, the simulator and the 3D view in <code>page/</code>, and the search that produced v5.</p>
</section>
</div>
{js}
{drive3d}
{anim_js}
'''
out = os.path.join(S, "flower-stick.html"); open(out, "w").write(head + body); print(out, len(head + body))
