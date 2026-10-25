"""The FLOWER stick page, clean pass (25 Oct): the story, a simulator with four choices and no sliders, the last three
blades in the parts viewer. The FLOWER stick as an engineering-portfolio page: the problem, each step we took, what we learned, the tool we built,
what is next. Reuses make_page3's head, viewer, simulator and 3D scripts."""
import os, json
S = os.path.dirname(os.path.abspath(__file__))
src3 = open(os.path.join(S, "make_page3.py")).read()
exec(src3[:src3.index("body = f'''")])        # head, b64, b64f, theory_svg, MOD, MODELS, js, anim_js, drive3d, buttons, V5, v5_button, v4_pressed
MODELS[:] = [
    ("v8l", "v8_lane", "the V-nose blade", "on the plate: the mountain blade with a V nose that reaches 0.3 in deeper (file v8_lane)", 6.3, 19.1),
    ("v7l", "v7_lane", "the mountain blade", "the shape that works: a 60° nose, a 1.1 in peak, Carter's lane across the tail, the snug hex (file v7_lane)", 6.3, 13.1),
]
exec(src3[src3.index("stl_js = "):src3.index("rows_js = ")])
rows["v7l"] = [["Profile", "60° nose from a 0.60 in edge, peak 1.10 in at 0.29 behind the lip, 12° tail down to the back edge over the ring: the v5 shape, which emptied the FLOWER on October 19"], ["Lane", "one curve across the tail (12.3 in radius), 0.3 in up at both edges, from 0.9 in behind the lip, full depth after 1.6 in. Seen from above its back end looks scalloped: the tail is thinnest in the middle, so it runs out sooner there"], ["Shaft", "goBILDA 8 mm REX is 7.0 mm across the flats, so the hole is 7.15 mm across the flats, flats top and bottom, 0.53 in behind the lip and 0.77 in high, with 2.2 mm of plastic over and under it"], ["Size", "137 × 77 × 15 mm"]]
rows["v10"] = [["Body", "v7 with Carter's lane, cut 0.32 in behind the lip; ears at the sides carry a 4 mm pin 0.96 in up"], ["Nose piece", "the lip and the 60° face across the full width, a hub on the pin in the middle, a heel that rests on the body"], ["How it trips", "the posts stop the lip below the pin; the robot's push turns the nose up to 54° as the body drives 0.22 in further; it falls flat when the robot backs out"], ["Size", "the same 137 × 77 × 15 mm, in two prints"]]
rows["v7"] = [["Profile", "as v7 with the lane"], ["Lane", "none: the control"], ["Size", "137 × 77 × 15 mm"]]
rows["v8l"] = [["Profile", "v7's, measured from a V-shaped lip: the middle 1.7 in of the front edge is 0.3 in deeper than the sides, and the slants lie on the FLOWER's posts, so the blade centres itself and the peak sits 0.84 in past the pinned ball instead of 0.54"], ["Lane", "as v7"], ["Shaft", "as v7, 0.45 in behind the sides' lip and 0.75 behind the middle's; 1.4 mm of plastic over it at the outer edges, 2.8 under"], ["Size", "137 × 83 × 15 mm"]]
rows["v8"] = [["Profile", "as v8 with the lane"], ["Lane", "none: the control"], ["Size", "137 × 83 × 15 mm"]]
rows["v9l"] = [["Profile", "60° nose from a 0.60 in edge, peak 1.00 in at 0.23 behind the lip, 10° tail to the back edge over the ring; straight lip"], ["Why", "the ball's top is 0.1 in lower going over the peak, in case the collar above is what holds it"], ["Lane", "as v7"], ["Shaft", "7.15 mm hex at 0.39 behind the lip, 0.74 in high; 1.6 mm of plastic over it, 2.0 under"], ["Size", "137 × 74 × 12 mm"]]
rows["v5l"] = [["Profile", "as v5 plain on the centreline"], ["Lane", "one curve across the whole tail (12.3 in radius), tangent to the tail at the centre and 0.3 in up at both edges; it starts 0.9 in behind the lip and reaches full depth over 1.6 in"], ["Size", "137 × 77 × 15 mm"]]
exec(src3[src3.index("rows_js = "):src3.index("flower_section = ")])
# the simulator's panel: the side view with the plan under it, the 3D view, the blade presets, then the sliders in four rows
anim_js = open(os.path.join(S, "sim.js")).read() + open(os.path.join(S, "anim3.js")).read().replace("__FLOWER__", json.dumps(flower_section, separators=(",", ":"))).replace("__ROBOT__", json.dumps(robot_section, separators=(",", ":")))
panel = f'''  <div class="drive">
    <div class="views">
      <div><svg id="sim-svg" viewBox="0 0 697 700" role="img" aria-label="Side view and plan view of the blade driving into the FLOWER and the POLLEN rolling out into the intake."></svg><span class="viewlbl">side view, and from above</span></div>
      <div><canvas id="drive3d" aria-label="3D view of the same run: the robot's intake, the blade on its arms, the FLOWER and the POLLEN. Drag to orbit."></canvas><span class="viewlbl">3D, drag to orbit</span></div>
    </div>
    <div class="bar">
      <label>Design</label>
      <span class="seg"><button data-preset="blockA" aria-pressed="false">1. the first block</button><button data-preset="v7" aria-pressed="true">2. the mountain blade</button><button data-preset="v8" aria-pressed="false">3. the V-nose blade</button><button data-preset="final" aria-pressed="false">4. the mountain blade + the poker</button></span>
      <label>Stack</label>
      <span class="seg"><button data-stack="fb" aria-pressed="true">front to back</button><button data-stack="straight" aria-pressed="false">straight</button><button data-stack="ss" aria-pressed="false">side to side</button></span>
    </div>
    <div class="bar" style="border-top: 0">
      <button class="btn" id="sim-play">Play</button>
      <button class="btn" id="sim-slow" aria-pressed="false">Slow</button>
      <input type="range" id="sim-scrub" min="0" max="1" value="0" aria-label="Scrub through the run">
      <b id="sim-clock" style="font: 500 13px var(--mono); color: var(--muted)">0.00 s</b>
    </div>
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

v10anim = open(os.path.join(S, "v10anim.js")).read()
body = f'''
<div class="wrap">
<header class="hero">
  <span class="eyebrow">Mona Shores FTC 19429 and 20245 · BIOBUZZ 2026 · engineering notebook · October 25</span>
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
    <div class="step">
      <div>
        <h3>The stack that locks itself</h3>
        <p>Then we looked at the photos of the stuck stacks side by side. When the four balls are staggered side to side, they come
        out one after another. When they are staggered front to back, the bottom ball rides up onto the peak and stays there, even
        with the sled pushed in as far as it goes. The model had every stack coming out, so we went back to FIRST's field CAD and
        measured the FLOWER again, part by part, instead of trusting what we had drawn.</p>
        <p>The posts were right: two square tubes standing on their corners, 1.18 in behind the ball's centre at their front corners,
        with faces that slant inward to 1.66 in, pinning a lifted ball 0.33 in back. The collar was not. The plate at 3.90 in has a
        round opening 1.93 in in radius, with a ridge on top of it, 1.71 in in radius, from 4.10 to 4.25 in where the pipes start. We
        had drawn the whole thing as a wall. It is a ledge with a corner a ball can rest on. With the ledge in the model, this
        happens: the second ball leans toward the robot until it rests against the ridge. When the blade lifts the bottom ball onto
        the peak, the second ball rides up onto the ridge's corner and sits across the gap, on the ridge on one side and on the
        bottom ball on the other. The third sits back against the posts, the fourth against the front pipes. The bottom ball cannot
        roll off the peak toward the robot without lifting the one above it, and the ridge holds that one. The stack has locked
        itself, in the same positions as our photos.</p>
        <p>A stack leaning side to side never does this, in the model or on the field. Its upper balls lean into the gaps between the
        pipes, where there is no ledge, so nothing loads the bottom ball from the robot's side. That is why left-right stacks fall
        easily and front-back stacks stick. With the measured collar the model locks most of the time, as the field does, where
        before it locked only now and then.</p>
        <p>Two more things it says. The ball can only sit <i>on</i> the peak if the peak is under it. With the lip at the posts' corners
        v7's peak would be half an inch past the pinned ball, so in the photos the blade stopped about a third of an inch short of
        the posts' corners: something touches first. And the lock is a matter of timing: a fast push gets the bottom ball over before
        the stack settles into its arch. Pushed at 12 in/s nothing locks; at 3 to 6 in/s most things do.</p>
        <table class="sweep loads"><thead><tr><th style="width:68%">Blade</th><th>Cases that feed</th></tr></thead><tbody>
          <tr><td class="k">v7, the blade that works</td><td class="stuck">9 of 16</td></tr>
          <tr><td class="k">v8, the V nose 0.3 in deeper</td><td class="stuck">12 of 16</td></tr>
          <tr><td class="k">v9, the 1.0 in peak</td><td class="stuck">6 of 16</td></tr>
          <tr><td class="k">v7 reaching 0.6 in deeper</td><td class="stuck">13 of 16</td></tr>
          <tr><td class="k">v7 reaching 0.9 in deeper</td><td class="stuck">14 of 16</td></tr>
          <tr><td class="k">v7 reaching 1.2 in deeper</td><td class="ok">16 of 16</td></tr>
          <tr><td class="k">a 0.9 in peak</td><td class="stuck">7 of 16</td></tr>
          <tr><td class="k">v7 with a 55° or 65° nose</td><td class="stuck">6 and 5 of 16</td></tr>
          <tr><td class="k">v7 tilted 6° nose-up after it stops</td><td class="stuck">10 of 16</td></tr>
          <tr><td class="k">v7 pushed at 12 in/s</td><td class="ok">16 of 16</td></tr>
          <tr><td class="k">v7, the collar 0.3 in higher (not ours to change)</td><td class="stuck">14 of 16</td></tr>
        </tbody></table><div class="cap">Sixteen runs each: a straight stack and one settled front to back against the collar, friction 0.3 and 0.45, pushed at 3 and 6 in/s, the sled at the posts' corners and a third of an inch short. The collar row shows the ledge is what makes the lock. Reach helps, but it takes more than an inch of it; a lower peak and a different nose angle do not help.</div>
        <div class="learn"><b>What we learned</b>Measure the real thing, part by part. The collar was a ledge we had drawn as a wall, and it turned out to be the whole story. And a push that is fast enough beats the lock: the stack needs time to settle into its arch.</div>
        <div class="ask"><b>Open question</b>The robot's intake wheels pull on the ball; the hand sled does not. Does that break the arch? And what stops the sled a third of an inch short of the posts' corners? Put a mark on the sled, push it in, and measure where the lip really is.</div>
      </div>
      <figure class="pic"><img src="data:image/png;base64,{b64f(MOD + "stuck_state.png")}" alt="A side-view drawing of the model's locked stack: the bottom ball on the blade's peak against the posts, the second ball resting on the collar's front bar, the third against the front pipes, the fourth on top."><figcaption>The model's locked stack with v7, pushed at 4 in/s and stopped 0.35 in short. Ball 1 on the peak, ball 2 on the collar's ridge, ball 3 back on the posts, ball 4 on the front pipes. Compare it with the October 23 photos.</figcaption></figure>
    </div>
    <div class="step">
      <div>
        <h3>A lifter the posts trip</h3>
        <p>If gravity alone will not break the arch, something has to push the bottom ball toward the robot. The idea from the
        team: a piece that swings up when the blade drives into the posts, so the drive motors do the lifting instead of
        gravity. We tried two versions in the model.</p>
        <p>A flap on the tail, hinged at the peak and swinging up behind the ball on the robot's side, makes everything worse: it
        pushes the ball toward the wall, into the posts, and nothing feeds. The ball has to be pushed from the wall's side. So the
        second version is a toe: a plate the blade's own thickness, 1.2 in wide, lying ahead of the lip under the ball between the
        posts' inner corners, hinged at the lip's foot. When the blade stops, the toe swings up and lifts the back of the ball,
        rolling it over the peak toward the robot. In the model a 1.3 in toe swinging up 75° feeds every case, including the ones
        that lock everything else. Swinging only 60° it never reaches the ball and changes nothing.</p>
        <table class="sweep loads"><thead><tr><th style="width:68%">Lifter</th><th>Cases that feed</th></tr></thead><tbody>
          <tr><td class="k">v7, no lifter</td><td class="stuck">9 of 16</td></tr>
          <tr><td class="k">flap on the tail, 1.0 in, swings up 45°</td><td class="stuck">0 of 16</td></tr>
          <tr><td class="k">toe ahead of the lip, 1.0 in, swings up 60°</td><td class="stuck">9 of 16</td></tr>
          <tr><td class="k">toe 1.0 in, swings up 75°</td><td class="stuck">14 of 16</td></tr>
          <tr><td class="k">toe 1.0 in, swings up 90°</td><td class="ok">16 of 16</td></tr>
          <tr><td class="k">toe 1.3 in, swings up 75°</td><td class="ok">16 of 16</td></tr>
        </tbody></table><div class="cap">The same sixteen runs as above. The toe lies between the posts' inner corners, which are 1.2 in apart, so it is 1.2 in wide; it touches the ball's back after about 60° of swing and needs 75° to roll it over. That much swing has to be driven: by the robot's own travel through a lever, or a servo.</div>
        <p>How it could be driven: the blade keeps pushing on the posts after it stops, so the arms could carry a spring and a
        link that turns the extra travel into the toe's swing; or a servo on the blade, told to swing when the drive motors
        stall. The model only says the motion works. It does not know how much force the toe needs, or whether a 1.2 in wide
        plate lifting a soft ball from one side will roll it or just dent it. That is a test with a hand lever on the sled before
        anything is built.</p>
        <div class="learn"><b>What we learned</b>Push the ball from the wall's side, not the robot's. A lifter on the tail is the obvious place and the wrong one.</div>
        <div class="ask"><b>Open question</b>Can the toe be passive, driven by the sled's last third of an inch of travel, or does it need a servo? And does a 1.2 in plate roll a POLLEN or dent it?</div>
      </div>
      <figure class="pic"><img src="data:image/png;base64,{b64f(MOD + "toe_lifter.png")}" alt="Side view of the model with the toe lifter swung up ahead of the blade's lip, pushing the bottom ball over the peak toward the robot while the stack above drops."><figcaption>The toe swung up to 75° in the model: it lifts the back of the bottom ball and rolls it over the peak.</figcaption></figure>
    </div>
    <div class="step">
      <div>
        <h3>The nose that trips itself, and the check that caught it</h3>
        <p>A toe with springs, slides and levers is a lot of hardware for a half-inch motion, and a toe wide enough to lift the
        ball has to thread the 1.2 in gap between the posts' inner corners. So we tried letting the posts do the work: split the
        blade at a hinge just behind the peak, a 4 mm pin 0.32 in behind the lip and 0.96 in up, with the lip and the 60° face on
        the front piece. When the lip lands on the posts and the robot keeps pushing, the push comes in below the pin and turns the
        piece up by itself, until the lip's corner rises to the pin's height at 54°. No spring, no slide, no lever. We drew it,
        and in the model it fed all 16 cases.</p>
        <p>Then we drew the moment it is supposed to work, by hand: the lip on the post, the body 0.22 in further in, the nose
        tipped the 54° the posts can give, the ball locked on the peak. Nothing on the nose piece is within 0.4 in of the ball.
        A toe on that hinge, of any length from 0.4 to 2 in, needs 72° or more of swing before it touches the ball, and no pin
        that fits inside the nose can self-limit past about 52°. The model's balls came out anyway because turning the piece
        shifts the peak corner under the ball by a tenth of an inch, and the model's lock is fragile enough that a nudge that
        small breaks it. On the field the lock is not that fragile. The score was real in the model and meaningless for the
        robot.</p>
        <p>What stands: a lifter that reaches the ball, the toe of the previous step swung to 75°, touches it after 61° and does
        push it over. The posts alone cannot swing anything that far. That lift needs the robot's own travel through a lever, or
        a servo. The v10 parts stay in the files as the record of a wrong turn.</p>
        <div class="learn"><b>What we learned</b>A simulation score is not a mechanism. Before trusting a number, draw the part at the moment it is supposed to work and check that it touches what it is supposed to push. We did that one step late.</div>
        <div class="ask"><b>Open question</b>Is the fragile lock the model's weakness, or is the real lock fragile too? Tap the blade with the stack locked on the sled: if a tap frees it, the model was right and a nudge is all it takes.</div>
      </div>
      <figure class="pic"><img src="data:image/png;base64,{b64f(MOD + "v10.png")}" alt="Two renders of v10 beside the FLOWER's posts: flat, with the orange nose piece ahead of the blue body; and tripped, with the nose piece turned up 54 degrees."><figcaption>v10 flat (above) and tipped (below), beside the posts. Tipped the 54° the posts can give, nothing on the orange piece comes within 0.4 in of the ball above it.</figcaption></figure>
    </div>
    <div class="step">
      <div>
        <h3>A poker on the robot</h3>
        <p>Go back to what the lock is: ball 2 leaning out over the collar's ridge, sitting on ball 1 from the robot's side.
        Nothing on the blade can reach ball 2. The robot can. A plate on the robot's frame, separate from the blade, 1.2 in ahead
        of the intake face and 5.3 to 7.5 in above the tile, three quarters of an inch wide, meets ball 2 just as the lip meets the posts and pushes it back onto
        the stack. With nothing leaning on the ridge there is no arch, and ball 1 rolls over the peak the way it always did when
        the stack was straight.</p>
        <p>We checked that it touches before we trusted the score, this time. With the sled a third of an inch short, the plate
        sinks 0.36 in into ball 2 and moves it back to a quarter inch off centre, where its surface clears the ridge. It sits
        between the front pipes and above their sockets, and it passes over the collar. It is fixed: no hinge, no spring, no
        servo. A strip of polycarbonate on two standoffs.</p>
        <p>How wide? The pipes are 2.39 in apart at that height, so the poker's edge hits a pipe once the robot is off-centre by
        more than 1.2 in minus half its width. Thinner buys aim. But a thin plate hitting a ball off-centre shoves it partly
        sideways, so we gave the model's poker a real width and parked the robot off-centre: 1 in wide still works 1 in off, 0.75
        in works to about 0.8 in off, 0.5 in starts missing at 1 in off. Three quarters of an inch it is: it clears the pipes to
        0.8 in either side and does its job out to there, which is close to the blade's own 1.3 in.</p>
        <table class="sweep loads"><thead><tr><th style="width:52%">Poker</th><th>3 in/s</th><th>6 in/s</th><th>8 in/s</th><th>10 in/s</th></tr></thead><tbody>
          <tr><td class="k">none, the mountain blade</td><td class="stuck">16 of 24</td><td class="stuck">18 of 24</td><td></td><td class="ok">24 of 24</td></tr>
          <tr><td class="k">1.1 in ahead, 5.3 to 6.5 in up</td><td class="ok">24</td><td class="ok">24</td><td class="ok">24</td><td class="ok">24</td></tr>
          <tr><td class="k">1.2 in ahead, 5.3 to 6.5 in up</td><td class="ok">24</td><td class="ok">24</td><td class="stuck">22</td><td class="stuck">20</td></tr>
          <tr><td class="k">1.2 in ahead, 5.3 to 7.5 in up</td><td class="ok">24</td><td class="ok">24</td><td class="ok">24</td><td class="ok">24</td></tr>
          <tr><td class="k">1.3 in ahead, 5.3 to 6.5 in up</td><td class="ok">24</td><td class="stuck">20</td><td class="stuck">18</td><td class="stuck">20</td></tr>
        </tbody></table><div class="cap">Twenty-four runs per cell: six stacks (straight, three ways of leaning front to back, leaning toward the wall, leaning side to side), friction 0.3 and 0.45, the sled at the posts and a third of an inch short. Too long a poker shoves the stack into the back of the FLOWER at speed; the tall one copes with that.</div>
        <div class="learn"><b>What we learned</b>When a lock is made of two balls, the second ball is a target too. The blade was the wrong place to look for the fix.</div>
        <p>Where it mounts, from the robot's October 25 CAD: the intake's top channel runs across the front at the face line, and the
        poker is a plate bolted to that channel's front, 0.75 in wide, rising to 7.5 in and reaching 1.2 in forward. Nothing else
        is in that zone. The side towers would also carry it, but an arm from them has to cross behind the face above the intake
        motor and drop a finger at the centre, which is more metal for the same poke. One catch: the CAD has the intake's wheel row
        4.7 in up, on vertical slides, and the model has it at 2.9 in, where it can take a ball off the blade. The poker's heights
        are measured from the tile, so if the intake comes down for the FLOWER the plate rides down with it and has to be sized
        for the lowered position. If the intake does not come down, that is a bigger problem than the poker.</p>
        <div class="ask"><b>Open question</b>Does the intake lower for the FLOWER, and to what height? The poker is sized for a wheel axle 2.9 in up. Where does the Limelight go on the tower, and does the poker's finger stay out of its view?</div>
      </div>
      <figure class="pic"><img src="data:image/png;base64,{b64f(MOD + "poker.png")}" alt="Four side views from the model: the stack leaning on the collar's ridge, the poker meeting ball 2 as the lip meets the posts, ball 2 pushed back, and ball 1 rolling over the peak."><figcaption>The poker in the model, four moments of one run with v7 pushed at 4 in/s and stopped a third of an inch short. Pick "the mountain blade + the poker" in the simulator to watch it.</figcaption><img src="data:image/png;base64,{b64f(MOD + "poker_mount.png")}" alt="Side and front views of the poker mounted on the intake's top channel, between the front pipes and above their sockets, with the ball pushed back onto the stack."><figcaption>Where it mounts: on the front of the intake's top channel, drawn from the robot's October 25 CAD with the intake in its lowered position.</figcaption></figure>
    </div>
  </div>
</section>

<section class="block" id="drive">
  <h2>Try our simulator</h2>
  <p>Pick a design from the sequence we went through, and a stack, and watch. The left side is a side view with the real FLOWER
  and the front of our robot, with a view from above under it. The right side is the same run in 3D, drag to look around.
  Every run is the worst case: a slow push at 4 in/s, which is the one that locks, with the sled driven all the way to the
  posts. It opens on the mountain blade with the stack leaning front to back, the stuck case from our photos. Step through the designs to see
  what each one fixed and what it did not: the first block stalls, the mountain blade locks on a front-to-back stack, the V nose
  does not change that, and the poker clears it.</p>
{panel}
  <p>What the model knows: the balls have friction and bounce, and rub on each other; the blade drives in until the posts stop
  it; the FLOWER's ring, posts, collar bar and pipes hold the stack the way the field drawing says they do; and the intake's soft
  wheels grab any ball that reaches them within the mouth.
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
    <div class="card"><h3>What to print: three blades, one change each</h3><p>With the sled fully seated the ball still sits on the peak, so something other than the posts is holding it. Rather than guess, the plate carries three blades that each change one thing from the blade that works: v7 with the snug hole, the baseline; v8, the V nose that juts 0.3 in further between the posts; and v9, a lower peak so the ball rides 0.1 in lower. All three with Carter's lane, holes 7.15 mm. One plate, 168 × 219 mm, or the three parts one at a time on a small bed.</p><img src="data:image/png;base64,{b64f(MOD + "print_job_three.png")}" alt="The print plate from above: v7 and v8 side by side, v9 turned sideways below them." style="width:100%;max-width:520px;border:1px solid var(--line);border-radius:8px;margin-top:6px"><p>How to read the test: if v8 goes over and v7 does not, it was depth. If v9 goes over and v7 does not, it was the collar. If v7 goes over too, it was the loose shaft all along. The collar's height above the tile is still worth a tape measure: the drawing says 3.90 in.</p><p>The model's prediction, with the stack leaning front to back and the sled a third of an inch short: v7 locks, v8 locks, v9 locks. v8 is better than v7 over all the cases, but not enough to count on. If the field agrees, reach alone is not the answer: it takes more than an inch, which only a narrow tongue between the posts' inner corners could get. The thing to try next is the poker on the robot from the last step: a plate 1.2 in ahead of the intake face, 5.3 to 7.5 in up, 1.4 in wide, on two standoffs. On the sled, a strip of plywood clamped at that height does for the first test. The self-tripping nose turned out not to reach the ball; step 12 says why.</p></div>
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
  <p>The two blades on the plate. The names: the mountain blade is the 60° nose, 1.1 in peak and 12° tail with Carter's lane (our files call it v7), the V-nose blade is the same with its middle reaching 0.3 in further (v8); the low-peak blade (v9) is also on the plate. The part files, the simulator and everything behind this page are in the team GitHub under <code>cad/modules/flower-stick-proto</code>.</p>
</section>


</div>
{js}
{drive3d}
{anim_js}
'''
out = os.path.join(S, "flower-stick.html"); open(out, "w").write(head + body); print(out, len(head + body))
