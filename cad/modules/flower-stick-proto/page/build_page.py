"""The FLOWER stick as an engineering-portfolio page: the problem, each step we took, what we learned, the tool we built,
what is next. Reuses make_page3's head, viewer, simulator and 3D scripts."""
import os, json
S = os.path.dirname(os.path.abspath(__file__))
src3 = open(os.path.join(S, "make_page3.py")).read()
exec(src3[:src3.index("body = f'''")])        # head, b64, b64f, theory_svg, MOD, MODELS, js, anim_js, drive3d, buttons, V5, v5_button, v4_pressed
MODELS[:] = [m for m in MODELS if m[0] != "v5s"]
MODELS[:] = [m for m in MODELS if m[0] not in ("v5",)]
MODELS.insert(0, ("v7l", "v7_lane", "v7 with Carter's lane", "the shape that works: v5, Carter's lane, the hole cut for the real shaft", 6.3, 13.1))
MODELS.insert(0, ("v8", "v8", "v8 plain", "the V nose without the lane: the control", 6.3, 19.1))
MODELS.insert(0, ("v8l", "v8_lane", "v8 with Carter's lane", "on the plate: v7 with a V nose that seats on the posts and reaches 0.3 in deeper", 6.3, 19.1))
MODELS.insert(1, ("v9l", "v9_lane", "v9 with Carter's lane", "on the plate: a lower peak, 1.0 in, for headroom under the collar", 5.6, 9.9))
exec(src3[src3.index("stl_js = "):src3.index("rows_js = ")])
rows["v7l"] = [["Profile", "60° nose from a 0.60 in edge, peak 1.10 in at 0.29 behind the lip, 12° tail down to the back edge over the ring: the v5 shape, which emptied the FLOWER on October 19"], ["Lane", "one curve across the tail (12.3 in radius), 0.3 in up at both edges, from 0.9 in behind the lip, full depth after 1.6 in. Seen from above its back end looks scalloped: the tail is thinnest in the middle, so it runs out sooner there"], ["Shaft", "goBILDA 8 mm REX is 7.0 mm across the flats, so the hole is 7.15 mm across the flats, flats top and bottom, 0.53 in behind the lip and 0.77 in high, with 2.2 mm of plastic over and under it"], ["Size", "137 × 77 × 15 mm"]]
rows["v7"] = [["Profile", "as v7 with the lane"], ["Lane", "none: the control"], ["Size", "137 × 77 × 15 mm"]]
rows["v8l"] = [["Profile", "v7's, measured from a V-shaped lip: the middle 1.7 in of the front edge is 0.3 in deeper than the sides, and the slants lie on the FLOWER's posts, so the blade centres itself and the peak sits 0.84 in past the pinned ball instead of 0.54"], ["Lane", "as v7"], ["Shaft", "as v7, 0.75 in behind the sides' lip; 1.4 mm of plastic over it at the outer edges, 2.8 under"], ["Size", "137 × 83 × 15 mm"]]
rows["v8"] = [["Profile", "as v8 with the lane"], ["Lane", "none: the control"], ["Size", "137 × 83 × 15 mm"]]
rows["v9l"] = [["Profile", "60° nose from a 0.60 in edge, peak 1.00 in at 0.23 behind the lip, 10° tail to the back edge over the ring; straight lip"], ["Why", "the ball's top is 0.1 in lower going over the peak, in case the collar above is what holds it"], ["Lane", "as v7"], ["Shaft", "7.15 mm hex at 0.39 behind the lip, 0.74 in high; 1.6 mm of plastic over it, 2.0 under"], ["Size", "137 × 74 × 12 mm"]]
rows["v5l"] = [["Profile", "as v5 plain on the centreline"], ["Lane", "one curve across the whole tail (12.3 in radius), tangent to the tail at the centre and 0.3 in up at both edges; it starts 0.9 in behind the lip and reaches full depth over 1.6 in"], ["Size", "137 × 77 × 15 mm"]]
exec(src3[src3.index("rows_js = "):src3.index("flower_section = ")])
# the simulator's panel: the side view with the plan under it, the 3D view, the blade presets, then the sliders in four rows
anim_js = anim_js.replace('choose("v5");', 'choose("v5l");')
anim_js = anim_js.replace('    v5: { name: "v5", shape: ', '    v7l: { name: "v7 with Carter\'s lane", shape: ' + json.dumps(V5["shape"]) + ', lane: { R: 12.3, rise: 0.3, start: 0.9, oval: 0 }, shaftBack: 0.53, onRobot: true },\n    v7: { name: "v7 plain", shape: ' + json.dumps(V5["shape"]) + ', lane: null, shaftBack: 0.53, onRobot: true },\n    v5: { name: "v5", shape: ')
anim_js = anim_js.replace('    v5: { name: "v5", shape: ', '    v5l: { name: "v5 with Carter\'s lane", shape: ' + json.dumps(V5["shape"]) + ', lane: { R: 12.5, rise: 0.3, start: 0.9 }, shaftBack: 0.77, onRobot: true },\n    v5: { name: "v5 plain", lane: null, shape: ')
import re as _re
anim_js = _re.sub(r'\n    v5: \{ name: "v5', lambda m: '\n    v8l: { name: "v8 with Carter\'s lane", shape: ' + json.dumps(V5["shape"]) + ', lane: { R: 12.3, rise: 0.3, start: 0.9, oval: 0 }, extraDepth: 0.3, shaftBack: 0.75, onRobot: true },' + m.group(0), anim_js, count=1)
anim_js = anim_js.replace('choose("v5l");', 'choose("v8l");')
assert 'v8l: {' in anim_js and 'choose("v8l")' in anim_js
panel = f'''  <div class="drive">
    <div class="views">
      <div><svg id="sim-svg" viewBox="0 0 697 700" role="img" aria-label="Side view and plan view of the blade driving into the FLOWER and the POLLEN rolling out into the intake."></svg><span class="viewlbl">side view, and from above</span></div>
      <div><canvas id="drive3d" aria-label="3D view of the same run: the robot's intake, the blade on its arms, the FLOWER and the POLLEN. Drag to orbit."></canvas><span class="viewlbl">3D, drag to orbit</span></div>
    </div>
    <div class="bar">
      <label>Blade</label>
      <span class="seg"><button data-preset="v8l" aria-pressed="true">v8: V nose + lane</button><button data-preset="v7l" aria-pressed="false">v7 + Carter's lane</button><button data-preset="v7" aria-pressed="false">v7 plain</button><button data-preset="v4" aria-pressed="false">v4 carry</button><button data-preset="v3" aria-pressed="false">v3 mountain</button><button data-preset="v2" aria-pressed="false">v2 lane</button><button data-preset="blockA" aria-pressed="false">Block A</button></span>
      <button class="btn" id="sim-play">Play</button>
      <button class="btn" id="sim-slow" aria-pressed="false">Slow</button>
      <input type="range" id="sim-scrub" min="0" max="1" value="0" aria-label="Scrub through the run">
      <b id="sim-clock" style="font: 500 13px var(--mono); color: var(--muted)">0.00 s</b>
      <button class="btn" id="sim-run" title="The sliders run on their own; this replays">Run</button>
    </div>
    <div class="sliders"><div class="grp">The blade's shape</div>
      <label>edge height <input type="range" id="s-edge" min="0.52" max="0.70" step="0.01" value="0.53"><span id="v-edge"></span> in</label>
      <label>nose angle <input type="range" id="s-nose" min="35" max="70" step="1" value="55"><span id="v-nose"></span>°</label>
      <label>peak height <input type="range" id="s-peak" min="0.9" max="1.6" step="0.02" value="1.28"><span id="v-peak"></span> in</label>
      <label>tail angle <input type="range" id="s-tail" min="8" max="45" step="1" value="14"><span id="v-tail"></span>°</label>
    </div>
    <div class="sliders"><div class="grp">Carter's lane on the tail</div>
      <label>lane depth at the edges <input type="range" id="s-laneRise" min="0" max="0.6" step="0.05" value="0.3"><span id="v-laneRise"></span> in</label>
      <label>lane radius <input type="range" id="s-laneR" min="2" max="14" step="0.5" value="12.5"><span id="v-laneR"></span> in</label>
      <label>lane starts <input type="range" id="s-laneStart" min="0.3" max="1.6" step="0.1" value="0.4"><span id="v-laneStart"></span> in behind the lip</label>
      <label>oval front <input type="range" id="s-laneOval" min="0" max="2" step="0.1" value="1.2"><span id="v-laneOval"></span> in further back at the edges</label>
      <label>intake mouth <input type="range" id="s-mouth" min="3" max="7.5" step="0.5" value="7.5"><span id="v-mouth"></span> in wide</label>
    </div>
    <div class="sliders"><div class="grp">The run</div>
      <label>drive speed <input type="range" id="s-drive" min="2" max="12" step="1" value="4"><span id="v-drive"></span> in/s</label>
      <label>friction μ <input type="range" id="s-mu" min="0.2" max="0.5" step="0.05" value="0.4"><span id="v-mu"></span></label>
      <label>stop error <input type="range" id="s-stopErr" min="-0.4" max="0.3" step="0.05" value="0"><span id="v-stopErr"></span> in</label>
    </div>
    <div class="sliders"><div class="grp">What can be off</div>
      <label>robot off-centre <input type="range" id="s-offset" min="-2" max="2" step="0.1" value="0"><span id="v-offset"></span> in</label>
      <label>robot turned <input type="range" id="s-yaw" min="0" max="12" step="1" value="0"><span id="v-yaw"></span>°</label>
      <label>stack leaning <input type="range" id="s-lean" min="0" max="0.8" step="0.1" value="0"><span id="v-lean"></span> in at the top</label>
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
      <dt>push to lift the stack</dt><dd id="ro-push"></dd>
      <dt>tail ends at</dt><dd id="ro-tail"></dd>
      <dt>blade width</dt><dd id="ro-width"></dd>
      <dt>the lane</dt><dd id="ro-lane"></dd>
    </dl>
    <div class="status"><span id="sim-result">…</span></div>
  </div>
'''
# the sideways sweep, as a table of what fed all four
import json as _json
SW = [_json.loads(l) for l in open(os.path.join(S, "lat_sweep.jsonl"))]
def sweep_table():
    cols = [(o, l) for o in (0, 0.8, 1.3) for l in (0, 0.3, 0.6)]
    kinds = [("plain tail", lambda g: g is None), ("Carter's lane: one curve, 0.3 in at the edges", lambda g: g and g["R"] == 12.3 and g["start"] == 0.9 and g["lead"] == 1.6),
             ("deeper curve, 0.5 in", lambda g: g and g["R"] == 7.5), ("lane with flat shoulders", lambda g: g and g["R"] == 4),
             ("the curve, short lead-in", lambda g: g and g["lead"] == 0.4), ("the curve, starting near the nose", lambda g: g and g["start"] == 0.5)]
    out = ['<table class="sweep"><thead><tr><th rowspan="2">wheels</th><th rowspan="2">tail</th><th colspan="3">robot centred</th><th colspan="3">robot 0.8 in off</th><th colspan="3">robot 1.3 in off</th></tr><tr>' + "".join('<th>stack straight</th><th>leans 0.3</th><th>leans 0.6</th>' for _ in range(3)) + '</tr></thead><tbody>']
    for mouth in (7.5, 3):
        for i, (name, pick) in enumerate(kinds):
            cells = []
            for o, l in cols:
                rr = [r for r in SW if pick(r["gutter"]) and r["mouth"] == mouth and r["offset"] == o and r["lean"] == l]
                ok = sum(1 for r in rr if r["fed"] == 4); stuck = sum(1 for r in rr if r["fed"] == 0 and r["missed"] == 0)
                cls = "ok" if ok == len(rr) else "stuck" if stuck == len(rr) else "miss"
                txt = "✓" if cls == "ok" else "stuck" if cls == "stuck" else "missed"
                cells.append(f'<td class="{cls}">{txt}</td>')
            out.append(f'<tr>{"<th rowspan=\"6\">" + ("7.5 in row, as drawn" if mouth == 7.5 else "3 in mouth, if the intake were narrow") + "</th>" if i == 0 else ""}<td class="k">{name}</td>{"".join(cells)}</tr>')
    out.append("</tbody></table>")
    return "\n".join(out)

head = head.replace("<title>The FLOWER Stick</title>", "<title>The FLOWER Stick</title>").replace("</style>", """
body { font-size: 16px }
.lede { font-size: 17px }
.steps { display: grid; gap: 14px; counter-reset: step }
.step { background: var(--panel); border: 1px solid var(--line); border-radius: 12px; padding: 16px 18px; display: grid; grid-template-columns: minmax(0, 1.1fr) minmax(0, 1fr); gap: 16px 22px; align-items: start }
@media (max-width: 820px) { .step { grid-template-columns: minmax(0, 1fr) } }
.step h3 { margin: 0 0 6px; font: 600 22px/1.1 var(--display); letter-spacing: .02em }
.step h3::before { counter-increment: step; content: counter(step); display: inline-grid; place-items: center; width: 30px; height: 30px; margin-right: 10px; border-radius: 50%; background: var(--petg); color: #fff; font: 700 16px var(--display); vertical-align: 2px }
.step p { margin: 0 0 8px; max-width: 60ch }
.step .pic { display: grid; gap: 8px }
.step .pic img { width: 100%; height: auto; border-radius: 8px; border: 1px solid var(--line); display: block }
.step .pic figcaption, .step .pic .cap { font-size: 13px; color: var(--muted) }
.learn { border-left: 4px solid var(--petg); background: color-mix(in srgb, var(--petg) 8%, var(--panel)); padding: 8px 12px; border-radius: 0 8px 8px 0; margin-top: 8px; max-width: 60ch }
.learn b { font: 500 12px var(--mono); letter-spacing: .06em; text-transform: uppercase; color: var(--petg); display: block; margin-bottom: 2px }
.ask { border-left: 4px solid #2b6cb0; background: color-mix(in srgb, #2b6cb0 8%, var(--panel)); padding: 8px 12px; border-radius: 0 8px 8px 0; margin-top: 8px; max-width: 60ch }
.ask b { font: 500 12px var(--mono); letter-spacing: .06em; text-transform: uppercase; color: #2b6cb0; display: block; margin-bottom: 2px }
.decide { display: grid; gap: 12px }
.decide .card { padding: 14px 16px; display: block } .decide .card h3 { margin-bottom: 6px } .decide .card p { margin: 0 0 6px; font-size: 15px } .decide .card p:last-child { margin: 0 }
.big { font: 700 34px/1 var(--display); color: var(--petg) }
.sliders .grp { grid-column: 1 / -1; font: 500 11px var(--mono); letter-spacing: .06em; text-transform: uppercase; color: var(--muted); margin: 2px 0 -4px }
.sliders + .sliders { padding-top: 0 }
.step .wide { grid-column: 1 / -1; min-width: 0; overflow-x: auto }
table.sweep { border-collapse: collapse; width: 100%; font-size: 12.5px; margin: 8px 0; table-layout: fixed }
table.sweep td.k { width: 150px } table.loads td.k { width: auto } table.loads td { text-align: left } table.sweep th:first-child { width: 72px }
table.sweep th, table.sweep td { border: 1px solid var(--line); padding: 5px 6px; text-align: center }
table.sweep th { font: 500 11.5px var(--mono); color: var(--muted); letter-spacing: .03em }
table.sweep td.k { text-align: left; font-size: 12.5px }
table.sweep td.ok { background: color-mix(in srgb, #2f8f4e 22%, var(--panel)) }
table.sweep td.miss { background: color-mix(in srgb, var(--petg) 28%, var(--panel)) }
table.sweep td.stuck { background: color-mix(in srgb, #b03030 30%, var(--panel)) }
.facts { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 10px }
.facts .card { padding: 12px 14px; display: block } .facts .card .big { display: block; margin-bottom: 4px } .facts .card span { font-size: 13.5px; color: var(--muted) }
.credits { font-size: 14px; color: var(--muted); max-width: 70ch }
.step .facts { grid-template-columns: 1fr }
.step .pic img.tall { max-height: 330px; object-fit: cover; object-position: center 40% }
.step .pic .stack { display: grid; gap: 6px }
#stage { aspect-ratio: 2 / 1 }
</style>""")
NEXT_BLADE = "v5" if V5 else "v4"

body = f'''
<div class="wrap">
<header class="hero">
  <span class="eyebrow">Mona Shores FTC 19429 and 20245 · BIOBUZZ 2026 · engineering notebook · October 16</span>
  <h1>The FLOWER stick</h1>
  <p class="lede">A blade that pulls the POLLEN out of a FLOWER. This page is where the project stands right now: what has
  been tried, what we found out, and the questions that are still open. Read the steps, try the simulator, and then decide
  what to test next. What you decide, and what the tests show, becomes the next part of this page.</p>
</header>

<section class="block" id="problem">
  <h2>The problem</h2>
  <p>A FLOWER holds four POLLEN balls stacked in a cup on the floor. The bottom ball sits inside a hole in a low ring, against two
  posts at the back. We want the robot to drive in, slide a blade under the bottom ball, and have all four balls roll out into our
  intake. It has to work in one push, every time, in about two seconds.</p>
  <div class="card"><h3>What we want to happen</h3><img src="data:image/png;base64,{b64f(MOD + "stack_sequence.png")}" alt="Six drawings of the blade driving in, lifting the bottom ball, and the four balls rolling out one after another."><div class="foot">The blade drives in until the FLOWER stops it. The bottom ball rides up over the blade and rolls out toward the robot. The stack drops one and the next ball follows.</div></div>
</section>

<section class="block" id="steps">
  <h2>Our design steps</h2>
  <div class="steps">

    <div class="step">
      <div>
        <h3>Our first block</h3>
        <p>We printed a small block with a steep face on one side and a ramp on the other. It rides on a stick with little wheels,
        so a person (or the robot) can push it into the FLOWER. The steep face goes in first, and the ramp faces the robot.</p>
        <p>On October 15 we tested it on the practice field. It did empty the FLOWER, but only after wiggling it back and forth for 4
        to 6 seconds. One block alone could not get under the ball. Two blocks side by side could.</p>
        <div class="learn"><b>What we learned</b>Getting under the ball is the hard part. Once the blade is under, the balls come out fast.</div>
        <div class="ask"><b>Open question</b>The blocks turned on the hex shaft. Was the printed hole a little too big, or did the push crush its corners? A hex shaft in a hex hole 0.3 mm bigger can rock about 4° either way even when it is perfect. The 36 mm blocks had only 2 mm of plastic under the hole; the new blade bears on 137 mm of shaft.</div>
      </div>
      <figure class="pic"><img class="tall" src="data:image/jpeg;base64,{b64("ph")}" alt="Two blue printed blocks side by side on a stick at the base of a loaded FLOWER."><figcaption>The two blocks on the stick at the FLOWER, October 15.</figcaption><img src="data:image/jpeg;base64,{b64("frames")}" alt="Three frames from the test video: approaching, under the ball, four balls out."><figcaption>One run: driving in, under, and out.</figcaption></figure>
    </div>

    <div class="step">
      <div>
        <h3>Carter's idea: one blade with a lane</h3>
        <p>After the test, Carter sketched a better part. Instead of two blocks, one wide blade with the shaft running through it.
        The front edge that goes into the FLOWER stays plain. On the ramp side, where the ball rolls out toward the intake, the
        whole ramp curves gently down toward the middle, like the valley the two blocks made by accident, but smooth, with no flats
        and no sharp edges for the ball to catch on.</p>
        <p>At first we read the sketch as a scoop cut into the front edge, and built that. Carter set us straight: the curve is on the
        ramp, not on the side that slides toward the wall.</p>
        <div class="learn"><b>What we learned</b>One blade is simpler than two blocks, and the shaft should go through it. Ask the person who drew it before building it.</div>
        <div class="ask"><b>Open question</b>Does the lane keep the balls on track into the intake, and does it cost anything on the way in? Step 7 puts it in the model.</div>
      </div>
      <figure class="pic"><img src="data:image/jpeg;base64,{b64("sk")}" alt="Carter's pencil sketch: the two blocks with a ball between them, then one long blade with a divot, with its front and end views."><figcaption>Carter's sketch. Left: the two blocks with a ball between them. Middle: one blade with the lane. Right: the front view on the shaft, and the end view with the hex hole.</figcaption>
      <div class="stack"><img src="data:image/png;base64,{b64f(MOD + "v5_lane.png")}" alt="A rendering of the v5 blade with a rounded lane down its tail between raised shoulders."></div><div class="cap">Carter's lane on the v5 blade: a plain front edge, and one shallow curve across the ramp, lowest in the middle.</div></figure>
    </div>

    <div class="step">
      <div>
        <h3>Why the block got stuck</h3>
        <p>We worked out the physics. When the block pushes, the bottom ball gets squeezed between the block's face and the posts
        behind it. Friction at both places tries to hold the ball down. The ball can only rise if the block pushes <em>up</em> on it
        hard enough to beat the friction.</p>
        <p>Our block's face was almost straight up and down. It leaned back only 17 degrees, so it pushed the ball almost straight
        into the posts and barely lifted it at all. Pushing harder did not help. That is why it only moved when we jiggled it.</p>
        <div class="learn"><b>What we learned</b>The front of the blade has to be a wedge, not a wall. A steeper slope lifts the ball instead of jamming it.</div>
      </div>
      <div class="pic theory">{theory_svg()}<div class="cap">The ball squeezed between the block and the post. The orange arrow is the push; it points up only 17 degrees.</div></div>
    </div>

    <div class="step">
      <div>
        <h3>Measuring the real FLOWER</h3>
        <p>We had been guessing the FLOWER's size from a picture in the game manual. FIRST publishes the real CAD files, so we
        measured the FLOWER from them. Three numbers turned out to matter.</p>
        <div class="facts">
          <div class="card"><span class="big">1.18 in</span><span>The posts' front corners sit this far behind the bottom ball's centre. The blade hits them and stops there, the same every time.</span></div>
          <div class="card"><span class="big">0.35 in</span><span>When the ball lifts, the posts pin it this far back. The blade's peak must be past that point, or the ball just sits on top.</span></div>
          <div class="card"><span class="big">3.9 in</span><span>The bracket above the opening. A ball leaving the FLOWER can only be lifted about 1.1 in or it hits the bracket.</span></div>
        </div>
        <div class="learn"><b>What we learned</b>Measure, do not guess. The real FLOWER gave us a hard stop and a rule for where the peak goes.</div>
        <div class="ask"><b>Open question</b>The CAD is the FLOWER as designed. Does the practice-field FLOWER match it? A ruler at the posts and the ring would tell us.</div>
      </div>
      <figure class="pic"><img src="data:image/jpeg;base64,{b64("seated")}" alt="Side and top views of the blade seated against the FLOWER's posts, drawn from the official CAD."><figcaption>The blade seated in the FLOWER, drawn from FIRST's CAD. The dashed circle is the lifted ball, pinned by the posts.</figcaption></figure>
    </div>

    <div class="step">
      <div>
        <h3>The mountain, and the tail</h3>
        <p>So we redesigned the blade as a mountain: a steep front, a peak just past where the ball gets pinned, and a slope down the
        back. One shape all the way across, so it does not matter if the robot is a little off to the left or right.</p>
        <p>Then the computer model showed a new problem. After the ball rolled over the peak it dropped into the hole and had to
        climb the ring to get out. So we added a long, gentle tail that reaches over the ring. The ball rides the blade all the
        way to the intake wheels and never touches the floor.</p>
        <div class="learn"><b>What we learned</b>Fix one thing and look again. The mountain fixed the jam; the tail fixed the drop.</div>
        <div class="ask"><b>Open question</b>The tail only works if the intake wheels are right at the FLOWER's face. Is that where our intake will be?</div>
      </div>
      <figure class="pic"><img src="data:image/png;base64,{b64f(MOD + "profiles_compare.png")}" alt="Four side views: the first block, the mountain, the mountain with a tail, and the computer-chosen shape, each with the ball resting on it."><figcaption>Four shapes with the ball resting on each: the first block, the mountain, the mountain with a tail, and the final shape.</figcaption></figure>
    </div>

    <div class="step">
      <div>
        <h3>Letting the computer search</h3>
        <p>We wrote a computer model of the FLOWER, the blade and the balls, with friction and bounce. Then we let it try 882
        different blade shapes. Each shape had to work in 12 situations: slippery and sticky balls, slow and fast driving, and the
        robot stopping a little short.</p>
        <p>Only 89 shapes worked every time, and only 4 of those left enough plastic around the shaft to print. All 4 were the same
        kind of shape: a 60 degree front, a lower peak with more room past the pinned ball, and a shallow tail. We call the best
        one v5.</p>
        <div class="learn"><b>What we learned</b>A higher peak does not help. Room past the pinned ball is what makes the blade forgiving.</div>
        <div class="ask"><b>Open question</b>The model is only as good as what it knows. What would you test on the real field to check that it is right?</div>
      </div>
      <div class="facts">
        <div class="card"><span class="big">882</span><span>shapes tried</span></div>
        <div class="card"><span class="big">12</span><span>situations each</span></div>
        <div class="card"><span class="big">4</span><span>worked every time and can be printed</span></div>
        <div class="card"><span class="big">2.6 s</span><span>worst case to get all four balls into the intake</span></div>
      </div>
    </div>

    <div class="step">
      <div>
        <h3>Putting Carter's lane in the model</h3>
        <p>The model only knew up and down and forward. To test a lane we taught it sideways: the ring and the posts hold the bottom
        ball on the FLOWER's centreline, the four pipes hold the stack, and a ball in the lane gets pushed toward its middle. Then we
        ran the plain tail and four lanes through the ways a real run can be off: the robot parked off-centre, the robot turned, and
        the stack leaning in its pipes.</p>
        <p>Three things came out. The wheels grab a ball about an inch and a half after the FLOWER lets go of it, so on our robot
        there is almost no room for a lane to act. A lane with flat shoulders wedged the ball when the robot was off-centre: the
        FLOWER was still holding the ball on its own line, so the ball met the shoulder's edge head-on. One smooth curve across
        the whole width never did that, wherever it started. And a leaning stack squirts the bottom ball sideways harder than any
        shallow curve can hold; it is the wide wheel row that catches it, not the blade.</p>
        <div class="learn"><b>What we learned</b>With a 7.5 in wheel row right at the FLOWER, the plain tail and Carter's curve both feed every ball the model threw at them. Flat shoulders are the thing to avoid: one curve all the way across is safe.</div>
        <div class="ask"><b>Open question</b>The model assumes the intake wheels sit at the FLOWER's face, 7.5 in wide. If the intake ends up narrower or further back, the lane starts to matter. Which is it?</div>
      </div>
      <div class="pic"><div class="cap">The lanes we tried. Carter's lane: one curve across the whole blade, 0.3 in up at the edges, starting 0.9 in behind the lip and reaching full depth over 1.6 in. Deeper: 0.5 in at the edges. Flat shoulders: a 4 in radius trough with flat shoulders 0.4 in high outside it. Short lead-in: full depth in 0.4 in. Near the nose: starting 0.5 in behind the lip.</div></div>
      <div class="wide"><div class="cap">What fed all four balls in the model, two runs per cell (robot straight, and turned 8°):</div>{sweep_table()}<div class="cap">Green: all four reached the wheels. Orange: a ball rolled past the wheels to the side. Red: a ball stuck on the blade.</div></div>
    </div>
    <div class="step">
      <div>
        <h3>Monday: it works, but it pushes too hard</h3>
        <p>On October 19 the team ran the printed blades on the stick. They emptied the FLOWER, but it took a hard push to get under
        the ball, and the blade was seen twisting on its shaft.</p>
        <p>The twisting turned out to be the key. Our shaft is goBILDA's "8 mm REX": a round 8 mm shaft with six flats, 7 mm across
        the flats. Every hole we had printed was an 8 mm hex, 8.2 mm across the flats. The shaft never touched the flats, so the
        blade sat in what was really a round bearing and could spin. Pushed into the ball, it tilted nose-down until its underside
        hit the ring. The blade we tested was never at 60°.</p>
        <p>That matters because the push depends on the face angle, and near the limit it depends on it a lot. The ball is squeezed
        between the face and the posts; a steep face shoves it into the posts more than it lifts it, and friction on the posts fights
        the lift. Held firm, 60° needs 5.6 times the stack's weight. Tilted 4°, 11 times. Tilted 8°, it stalls. So two things were
        true at once: the loose shaft made the push much worse, and 60° was always close to the edge.</p>
        <p>On October 22 the team tried the 50° blades. They did not work: the ball sat on the peak instead of rolling past it.
        The team's reading is that the shallower nose put the peak too far back from the lip, and the arithmetic agrees: the peak
        sits further back the shallower the nose is, 0.29 in for v5 and 0.34 to 0.42 in for the 50° blades. The field says the
        margin matters more than the model thought. The v5 blades had emptied the FLOWER even while spinning on the shaft, so the v5
        shape with the hole fixed is the blade now. We call it v7, so there is one name for the one that works.</p>
        <div class="learn"><b>What we learned</b>Measure the shaft, not the catalogue name. And the push and the margin pull against each other through the nose angle; the field says the margin wins. A lighter push has to come from somewhere else.</div>
        <div class="ask"><b>Open question</b>With the hole cut for the real shaft, how hard does v7 push at a true 60°? If it is still too hard, the levers left are a lower, sharper edge, a smoother face so the ball rolls instead of slides, and a lower peak, not a shallower nose.</div>
      </div>
      <figure class="pic"><img src="data:image/png;base64,{b64f(MOD + "v7_lane.png")}" alt="A rendering of the v7 blade: the v5 shape with Carter's lane and the hex hole cut for the REX shaft."><figcaption>v7 with Carter's lane: the v5 shape, the lane, the hole cut for the real shaft. The 50° blades are retired.</figcaption>
      <table class="sweep loads"><thead><tr><th>face angle</th><th>push to lift the stack</th></tr></thead><tbody>
        <tr><td class="k">60°, held firm (v5)</td><td class="miss">5.6 ×</td></tr>
        <tr><td class="k">62°, v5 tilted 2° on a loose shaft</td><td class="miss">7.6 ×</td></tr>
        <tr><td class="k">64°, tilted 4°</td><td class="stuck">11 ×</td></tr>
        <tr><td class="k">68°, tilted 8°</td><td class="stuck">stalls</td></tr>
        <tr><td class="k">50°, held firm (v6)</td><td class="ok">2.3 ×</td></tr>
        <tr><td class="k">56°, v6 tilted 6°</td><td class="ok">3.7 ×</td></tr>
      </tbody></table><div class="cap">Push as a multiple of the stack's weight, friction 0.4 on the posts, the ball rolling on the face. The "push to lift the stack" readout in the simulator computes this for any shape. v6 is v5 with only the nose changed: 50° costs 0.13 in of margin past the pinned ball, and in the model it misses only when the robot creeps in at 3 in/s and stops a third of an inch before the posts.</div></figure>
    </div>
    <div class="step">
      <div>
        <h3>The sled stops short, so the nose becomes a V</h3>
        <p>On October 23 the team looked closely at where the blade ends up. The sled stops with the ball balanced on the peak,
        pinned against the post, instead of past it. Pushed any higher, the ball's top meets the front bar of the FLOWER's collar.
        The blade needs to get further under the ball.</p>
        <p>The team's idea was notches or a V in the front edge so the blade can drive further in. That turns out to fit the FLOWER
        exactly: the two posts have slanted faces that run inward and deeper. A front edge cut to the same slant seats on both faces
        at once, centres itself, and lets the middle of the blade reach 0.3 in deeper than the sides. Nothing else changes: same
        peak height, same push, same shaft. In the model v8 feeds every case, even when the sled stops half an inch short.</p>
        <div class="learn"><b>What we learned</b>Look at where things actually stop. The margin past the peak is the number that keeps failing, and the V buys it without touching the peak.</div>
        <div class="ask"><b>Open question</b>Is it the front edge on the posts that stops the sled, or the wheels on the wall or the base plate? The V only helps in the first case. Lift the wheels and push the blade in another half inch by hand: if the ball tips over, the V is the answer.</div>
      </div>
      <figure class="pic"><img src="data:image/png;base64,{b64f(MOD + "v8_lane.png")}" alt="A rendering of the v8 blade: v7 with a V-shaped front edge whose middle reaches deeper than the sides."><figcaption>v8 with Carter's lane: v7 with a V nose. The middle of the front edge is 0.3 in deeper; the slants sit on the posts.</figcaption></figure>
    </div>
  </div>
</section>

<section class="block" id="drive">
  <h2>Try our simulator</h2>
  <p>This is the computer model we used. Pick a blade, or move a slider, and watch what happens. The left side is a side view with
  the real FLOWER shape and the front of our robot, with a view from above under it. The right side is the same run in 3D; drag it
  to look around. The lane sliders put Carter's lane on the tail. The "what can be off" sliders park the robot off-centre, turn
  it, or lean the stack. The width and arm choices show how the blade could mount on the robot.</p>
{panel}
  <p>What the model knows: the balls have friction and bounce, the blade drives in until the posts stop it, the FLOWER holds the
  balls on its centreline until they leave it, and the intake's soft wheels grab any ball that reaches them within the mouth.
  What it does not know: how a real printed edge wears, and exactly how a real stack settles in its pipes. That is why we still
  test on the field.</p>
</section>

<section class="block" id="survive">
  <h2>Will it survive the robot?</h2>
  <p>The plastic around the shaft looks thin: about 2 mm under the hex hole and 4 mm over it. We cannot make it thicker, because
  the blade's bottom has to clear the ring and its peak height is tuned. It holds anyway, because the blade is 5.4 in wide and
  every hit spreads across that whole width. The numbers:</p>
  <table class="sweep loads"><thead><tr><th>What happens</th><th>What takes the load</th><th>How hard it is pushed</th></tr></thead><tbody>
    <tr><td class="k">The robot drives into the FLOWER and the posts stop the nose (about 25 lb before the wheels slip)</td><td>half an inch of plastic between the nose and the shaft, across the full width</td><td class="ok">tiny, under 1 % of what PETG takes</td></tr>
    <tr><td class="k">Something hits the tail tip, 20 lb, 2 in behind the shaft</td><td>the plastic above and below the hole, across the full width</td><td class="ok">about 3 % of what PETG takes</td></tr>
    <tr><td class="k">The ball is pinched at the nose and twists the blade on the shaft</td><td>the hex corners, over 5.4 in of shaft</td><td class="ok">a few inch-pounds, tiny</td></tr>
    <tr><td class="k">The same 25 lb push, with the shaft spanning 17 in between arms outside the wheels</td><td>the 8 mm steel shaft, bending</td><td class="stuck">at the steel's limit: it would take a permanent bend</td></tr>
    <tr><td class="k">The same push with the arms on the intake uprights, 10 in apart</td><td>the 8 mm steel shaft, bending</td><td class="ok">about 60 % of the limit, fine</td></tr>
  </tbody></table>
  <div class="learn"><b>What we learned</b>Width is strength. The blade's plastic is fine; the shaft between the arms is the part to design for. Keep the arms close to the blade, or add a bearing block on each side of it.</div>
  <p>What wears out is the hex hole's corners and the nose edge. The coupon fit and the long hex engagement handle the corners.
  The nose edge is a consumable: print two sets, look at the edge after practice, swap. Once the shape is proven, the
  production blade can be printed in nylon or carbon-fibre PETG from the same file.</p>
</section>

<section class="block" id="next">
  <h2>Where to go next</h2>
  <p>These are the choices that are still open. The simulator above shows what each one does. Pick, test, and write down
  what happened.</p>
  <div class="decide">
    <div class="card"><h3>Carter's lane stays</h3><p>The model says the curve never costs a ball, and it may keep them rolling straight on the floor and in a narrower intake. So the lane blade is the one we test first, and the plain blade is the control printed beside it. If the lane blade does as well or better on the stick, it stays.</p><p>The curve runs across the whole width, 0.3 in up at the edges, with no flat anywhere. Try a lane with flat shoulders in the simulator (lane radius 4) to see why that matters.</p></div>
    <div class="card"><h3>How wide?</h3><p>As wide as the two posts (5.4 in), as wide as the intake wheels (7.5 in), or wider. A wider blade means the robot does not have to line up as carefully.</p><p>Try the width buttons in the simulator.</p></div>
    <div class="card"><h3>How does it mount on the robot?</h3><p>On the intake's own uprights, on one arm outside the wheels, or on two arms. The corner disc on the right side is in the way of a right-hand arm.</p><p>Try the arm buttons in the simulator.</p></div>
    <div class="card"><h3>What Dean prints: v8 and v9, one of each idea</h3><p>With the sled fully seated the ball still sits on the peak, which means it is not resting on the posts but is held further forward, where only the collar is. So the plate carries one blade for each idea: v8, the V nose that reaches 0.3 in deeper, and v9, a lower blade with a 1.0 in peak so the ball's top is 0.1 in lower going over. One plate, 166 × 157 mm, both with the lane, plus the hex coupon. Holes 7.15 mm across the flats. The collar's height above the tile is still the number to measure: the drawing says 3.90 in.</p><img src="data:image/png;base64,{b64f(MOD + "print_job_dean_hedge.png")}" alt="The print plate from above: the two v6 blades side by side and the small coupon bar under them." style="width:100%;max-width:520px;border:1px solid var(--line);border-radius:8px;margin-top:6px"><p>Test it the same way: five straight pushes and one off-centre per blade, count the balls, time it, and say how hard each pushed.</p></div>
    <div class="card"><h3>A test to start from</h3><p>Five straight pushes and one off-centre push for each blade. Count the balls that come out and time each push. Film it so we can see where it sticks.</p></div>
  </div>
</section>

<section class="block" id="blade">
  <h2>The parts</h2>
  <div class="viewer">
    <canvas id="stage" aria-label="3D model of the selected blade. Drag to turn it, scroll or pinch to zoom."></canvas>
    <div class="pick" role="group" aria-label="Which blade">
{buttons}
    </div>
    <dl class="spec" id="spec"></dl>
  </div>
  <p>Every version printed or modelled, the one we are printing first. The part files, the simulator, and the search that chose v5 are all in the team GitHub under <code>cad/modules/flower-stick-proto</code>.</p>
</section>

<section class="block" id="others">
  <h2>Ideas from other teams</h2>
  <div class="four">
    <div class="card"><h3>19705 WXYZ</h3><img src="data:image/jpeg;base64,{b64f("clips/wxyz.jpg")}" alt="A wide red ramp with a thin lip and a row of compliant wheels behind it."><div class="foot">A thin ramp with spinning wheels right behind it.</div></div>
    <div class="card"><h3>CrossFire 9968</h3><img src="data:image/jpeg;base64,{b64f("clips/crossfire.jpg")}" alt="A bent plate wedge on a beam under a FLOWER's stack, a string roller behind it."><div class="foot">A bent metal wedge with a string roller behind.</div></div>
    <div class="card"><h3>Hexadecimal Nibble</h3><img src="data:image/jpeg;base64,{b64f("clips/hexnibble.jpg")}" alt="A large yellow printed wedge in front of a loaded FLOWER."><div class="foot">A big wedge the ball climbs over.</div></div>
    <div class="card"><h3>Eaglebots 13532</h3><img src="data:image/jpeg;base64,{b64f("clips/eaglebots.jpg")}" alt="A robot parked beside a FLOWER with a servo arm sweeping a wedge through its base."><div class="foot">An arm that sweeps the balls out sideways.</div></div>
  </div>
  <p>We looked at what other teams built. Every wedge meets the ball low, and most have spinning wheels right behind it. That is
  why our intake wheels sit right at the FLOWER's face.</p>
</section>

</div>
{js}
{drive3d}
{anim_js}
'''
out = os.path.join(S, "flower-stick.html"); open(out, "w").write(head + body); print(out, len(head + body))
