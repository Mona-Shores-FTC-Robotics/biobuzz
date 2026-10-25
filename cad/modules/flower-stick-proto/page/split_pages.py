"""Split the built flower-stick.html into two pages: the notes (the story) and the simulator (the tool), each linking to
the other. Usage: python3 split_pages.py <sim url> <notes url>"""
import re, sys
SIM_URL, NOTES_URL = sys.argv[1], sys.argv[2]
h = open("flower-stick.html").read()
head, body = h.split("<div class=\"wrap\">", 1)
body = "<div class=\"wrap\">" + body
def section(id_):
    m = re.search(r'<section class="block" id="%s">.*?</section>\n' % id_, body, flags=re.S); return m.group(0)
hero = re.search(r'<header class="hero">.*?</header>\n', body, flags=re.S).group(0)
scripts = body[body.index("</div>\n", body.rindex("</section>")) + len("</div>\n"):]
problem, steps, drive, survive, nxt, blade = [section(i) for i in ("problem", "steps", "drive", "survive", "next", "blade")]
link_card = lambda url, title, text: f'<section class="block"><div class="card" style="padding:16px 18px"><h3>{title}</h3><p>{text}</p><p><a href="{url}" style="font-weight:600">{url}</a></p></div></section>\n'
# the notes page
hero_notes = hero.replace("<h1>The FLOWER stick</h1>", "<h1>The FLOWER stick: our notes</h1>")
hero_notes = re.sub(r'<p class="lede">.*?</p>', '<p class="lede">A blade that pulls the POLLEN out of a FLOWER. These are the team\'s working notes: the problem, every design we tried in order, what each one taught us, and what is next. The simulator we built to test the designs is its own page, linked at the end.</p>', hero_notes, flags=re.S)
notes = head + "<div class=\"wrap\">\n" + hero_notes + problem + steps + survive + nxt + link_card(SIM_URL, "Try the simulator", "The model we tested every design in, with the FLOWER drawn from the field CAD and the robot's front: pick a design and a stack and watch. It has its own page so it can go in a portfolio on its own.") + "</div>\n"
open("flower-stick-notes.html", "w").write(notes)
# the simulator page
hero_sim = hero.replace("<h1>The FLOWER stick</h1>", "<h1>The FLOWER stick simulator</h1>")
hero_sim = re.sub(r'<p class="lede">.*?</p>', '<p class="lede">The computer model we tested every FLOWER stick design in: the FLOWER drawn from FIRST\'s field CAD, the front of our robot, four POLLEN with friction and bounce, and the blade driven in until the posts stop it. Pick a design from the sequence we went through, pick how the stack leans, and watch. The story behind the designs is in our notes, linked at the end.</p>', hero_sim, flags=re.S)
sim = head.replace("<title>The FLOWER Stick</title>", "<title>FLOWER Stick Simulator</title>") + "<div class=\"wrap\">\n" + hero_sim + drive + blade + link_card(NOTES_URL, "Our notes", "The problem, every design in order, what each taught us, and what is next.") + "</div>\n" + scripts
open("flower-stick-sim.html", "w").write(sim)
print("notes", len(notes) // 1000, "KB; sim", len(sim) // 1000, "KB")
