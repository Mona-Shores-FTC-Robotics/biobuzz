"""The FLOWER stick as an engineering-portfolio page: the problem, each step we took, what we learned, the tool we built,
what is next. Reuses make_page3's head, viewer, simulator and 3D scripts."""
import os, json
S = os.path.dirname(os.path.abspath(__file__))
src3 = open(os.path.join(S, "make_page3.py")).read()
exec(src3[:src3.index("body = f'''")])        # head, b64, b64f, theory_svg, MOD, MODELS, js, anim_js, drive3d, buttons, V5, v5_button, v4_pressed
MODELS[:] = [m for m in MODELS if m[0] != "v5s"]
MODELS.insert(1, ("v5l", "v5_lane", "v5 with Carter's lane", "the same blade, a rounded lane down the tail between raised shoulders", 6.3, 13.1))
rows["v5l"] = [["Profile", "as v5 plain on the centreline"], ["Lane", "a trough of 3 in radius down the tail, tangent to it at the centre; shoulders 0.3 in up from 0.9 in behind the lip, full height after 1.6 in; catches a ball up to ±1.3 in off centre"], ["Size", "137 × 90 × 15 mm"]]
exec(src3[src3.index("stl_js = "):src3.index("flower_section = ")])
# the simulator's panel: the side view with the plan under it, the 3D view, the blade presets, then the sliders in four rows
panel = f'''  <div class="drive">
    <div class="views">
      <div><svg id="sim-svg" viewBox="0 0 697 700" role="img" aria-label="Side view and plan view of the blade driving into the FLOWER and the POLLEN rolling out into the intake."></svg><span class="viewlbl">side view, and from above</span></div>
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
    <div class="sliders"><div class="grp">The blade's shape</div>
      <label>edge height <input type="range" id="s-edge" min="0.52" max="0.70" step="0.01" value="0.53"><span id="v-edge"></span> in</label>
      <label>nose angle <input type="range" id="s-nose" min="35" max="70" step="1" value="55"><span id="v-nose"></span>°</label>
      <label>peak height <input type="range" id="s-peak" min="0.9" max="1.6" step="0.02" value="1.28"><span id="v-peak"></span> in</label>
      <label>tail angle <input type="range" id="s-tail" min="8" max="45" step="1" value="14"><span id="v-tail"></span>°</label>
    </div>
    <div class="sliders"><div class="grp">Carter's lane on the tail</div>
      <label>shoulders' height <input type="range" id="s-laneRise" min="0" max="0.6" step="0.05" value="0"><span id="v-laneRise"></span> in</label>
      <label>lane radius <input type="range" id="s-laneR" min="2" max="6" step="0.5" value="3"><span id="v-laneR"></span> in</label>
      <label>lane starts <input type="range" id="s-laneStart" min="0.3" max="1.6" step="0.1" value="0.9"><span id="v-laneStart"></span> in behind the lip</label>
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
    kinds = [("plain tail", lambda g: g is None), ("gentle lane", lambda g: g and g["rise"] == 0.3 and g["start"] == 0.9),
             ("taller lane", lambda g: g and g["rise"] == 0.4), ("short lead-in", lambda g: g and g["start"] == 1.1), ("starts near the nose", lambda g: g and g["start"] == 0.5)]
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
            out.append(f'<tr>{"<th rowspan=\"5\">" + ("7.5 in row, as drawn" if mouth == 7.5 else "3 in mouth, if the intake were narrow") + "</th>" if i == 0 else ""}<td class="k">{name}</td>{"".join(cells)}</tr>')
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
table.sweep td.k { width: 120px } table.sweep th:first-child { width: 72px }
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
      </div>
      <figure class="pic"><img class="tall" src="data:image/jpeg;base64,{b64("ph")}" alt="Two blue printed blocks side by side on a stick at the base of a loaded FLOWER."><figcaption>The two blocks on the stick at the FLOWER, October 15.</figcaption><img src="data:image/jpeg;base64,{b64("frames")}" alt="Three frames from the test video: approaching, under the ball, four balls out."><figcaption>One run: driving in, under, and out.</figcaption></figure>
    </div>

    <div class="step">
      <div>
        <h3>Carter's idea: one blade with a lane</h3>
        <p>After the test, Carter sketched a better part. Instead of two blocks, one wide blade with the shaft running through it.
        The front edge that goes into the FLOWER stays plain. On the ramp side, where the ball rolls out toward the intake, there is
        a rounded lane down the middle, like the valley the two blocks made by accident, but smooth, with no sharp edges for the ball
        to catch on.</p>
        <p>At first we read the sketch as a scoop cut into the front edge, and built that. Carter set us straight: the curve is on the
        ramp, not on the side that slides toward the wall.</p>
        <div class="learn"><b>What we learned</b>One blade is simpler than two blocks, and the shaft should go through it. Ask the person who drew it before building it.</div>
        <div class="ask"><b>Open question</b>Does the lane keep the balls on track into the intake, and does it cost anything on the way in? Step 7 puts it in the model.</div>
      </div>
      <figure class="pic"><img src="data:image/jpeg;base64,{b64("sk")}" alt="Carter's pencil sketch: the two blocks with a ball between them, then one long blade with a divot, with its front and end views."><figcaption>Carter's sketch. Left: the two blocks with a ball between them. Middle: one blade with the lane. Right: the front view on the shaft, and the end view with the hex hole.</figcaption>
      <div class="stack"><img src="data:image/png;base64,{b64f(MOD + "v5_lane.png")}" alt="A rendering of the v5 blade with a rounded lane down its tail between raised shoulders."></div><div class="cap">Carter's lane on the v5 blade: a plain front edge, and a rounded trough down the ramp between two low shoulders.</div></figure>
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
        there is almost no room for a lane to act. A lane that starts near the nose, or rises too fast, wedges the ball when the robot
        is off-centre, because the FLOWER is still holding the ball on its own line. And a leaning stack squirts the bottom ball
        sideways harder than any low lane can hold; it is the wide wheel row that catches it, not the blade.</p>
        <div class="learn"><b>What we learned</b>With a 7.5 in wheel row right at the FLOWER, the plain tail feeds every ball the model threw at it. A gentle lane does no harm; a tall or early one does.</div>
        <div class="ask"><b>Open question</b>The model assumes the intake wheels sit at the FLOWER's face, 7.5 in wide. If the intake ends up narrower or further back, the lane starts to matter. Which is it?</div>
      </div>
      <div class="pic"><div class="cap">The lanes we tried. Gentle lane: shoulders 0.3 in high, starting 0.9 in behind the lip and reaching full height over 1.6 in. Taller: 0.4 in. Short lead-in: full height in 0.4 in. Near the nose: starting 0.5 in behind the lip.</div></div>
      <div class="wide"><div class="cap">What fed all four balls in the model, two runs per cell (robot straight, and turned 8°):</div>{sweep_table()}<div class="cap">Green: all four reached the wheels. Orange: a ball rolled past the wheels to the side. Red: a ball stuck on the blade.</div></div>
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

<section class="block" id="next">
  <h2>Where to go next</h2>
  <p>These are the choices that are still open. The simulator above shows what each one does. Pick, test, and write down
  what happened.</p>
  <div class="decide">
    <div class="card"><h3>Plain or Carter's lane?</h3><p>Both are on one print plate and can be tested on the stick the same day. The model says the plain tail is enough on our robot and a gentle lane does no harm. The stick test on the floor is where a lane would show: watch whether the balls roll out straight.</p><p>Try the lane sliders in the simulator first.</p></div>
    <div class="card"><h3>How wide?</h3><p>As wide as the two posts (5.4 in), as wide as the intake wheels (7.5 in), or wider. A wider blade means the robot does not have to line up as carefully.</p><p>Try the width buttons in the simulator.</p></div>
    <div class="card"><h3>How does it mount on the robot?</h3><p>On the intake's own uprights, on one arm outside the wheels, or on two arms. The corner disc on the right side is in the way of a right-hand arm.</p><p>Try the arm buttons in the simulator.</p></div>
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
  <p>Every version printed or modelled, newest first. The part files, the simulator, and the search that chose v5 are all in the team GitHub under <code>cad/modules/flower-stick-proto</code>.</p>
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
