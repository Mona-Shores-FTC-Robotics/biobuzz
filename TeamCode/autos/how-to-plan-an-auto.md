# How to plan an Auto

A one-page guide to planning an Autonomous in our
[Visualizer](https://mona-shores-ftc-robotics.github.io/Visualizer/). You need a laptop and a
browser. No login, no Android Studio. (Phones don't work.)

**For now you're planning only:** spots, routes and timing. The robot can't run your Auto yet. That
waits on the drive being tuned and the launcher and intake being built. Make a good plan and save
it. Exporting Java comes later.

## 1. Open the sample

Open **[the RightStartTip sample](https://mona-shores-ftc-robotics.github.io/Visualizer/#sample=right-start-tip)**.
It opens as a copy, so you can't break anything. A bar at the top says so.

What it does: start at RIGHT_START, launch the 4 preloads and watch the HIVE for up to 4 s.
- **If it tips:** drive through the HIVE to LEFT_FLOWER, collect, shoot at LEFT_SHOT, park.
- **If it times out:** collect in the GARDEN, shoot from RIGHT_SHOT, then *rejoin* the first plan
  at RIGHT_HIVE_ENTRANCE.

## 2. Find your way around

| Where | What it is |
|---|---|
| **Left: the Auto list** | The Auto, top to bottom. Each line is a **card**: ▶ a command (`LaunchAll`), ↝ a path to drive, ⏱ a wait. Click a card to edit it. |
| **Right: the field** | The paths, drawn. Clicking a path selects its card. |
| **Under the field** | The play bar, `time / 30 s`, and `worst` (the time if every wait runs out). Red means over 30 s. |
| **Above the field** | One chip per trigger (`Tip`, `IntakeFull`). They set what happens in the preview. |

Press play and watch the robot drive the plan.

## 3. See both routes

A wait that branches has a switch on its card: **✓ Tip** or **timed out**.

1. Leave it on **✓ Tip** and press play. Note the time.
2. Switch it to **timed out** and play again. Note that time too.

**Every route must finish under 30 s**, and so must `worst`. If one is close (28 s or more), it's
too close: the real robot is slower than the preview until the drive is tuned.

## 4. Move a spot

Spots are named places: `LEFT_SHOT`, `RIGHT_SHOT`, `LEFT_FLOWER`. **LEFT_SHOT and RIGHT_SHOT are
placeholders. Deciding where they really go is your first job.**

- Drag a path's end on the field. If it sits on a named spot, the spot moves, and every path that
  ends there moves with it.
- Or type numbers: **Setup** (top of the Auto list) → **Named points**.
- Click a path, then use **+ Control Point** / **- Control Point** above the field to bend it.

Spot names are as seen from where the drivers stand: `LEFT_` is the wall on your left, `RIGHT_` the
wall on your right. Positions are inches from a field corner (the field is 141.5 in), and headings
are degrees.

## 5. Build your own steps

The buttons at the top of the Auto list add a card after the one you selected:

| Button | Adds |
|---|---|
| **+ Command** | Something the robot does (`LaunchAll`). Set **Timeout (s)** so it can't hang. |
| **+ Path** | A drive from wherever the robot is. Choose where it **Ends at**. |
| **+ Wait for** | Wait for a trigger, at most N ms. The robot goes on either way. |
| **+ Decision** | A wait with two branches: one if the trigger fires, one if time runs out. |
| **+ Rejoin** | Ends a branch by joining another route at one of its stops, so you don't build the same steps twice. |

On a path card:
- **Drive through (don't stop here)**: the robot passes this spot without stopping.
- **Park path**: the drive used to park. If time runs short, the robot drops what it's doing and
  takes this path.

Use **↑ ↓** to reorder a card, **Duplicate** to copy it, **✕ Delete** to remove it.

**Commands the robot knows today:** `LaunchAll`. **Triggers:** `Tip` (our HIVE started to tip) and
`IntakeFull` (holding 4 POLLEN). There's no "intake on" command, because the robot collects on its
own whenever it isn't full or launching. Need something else? Add it in **Setup**, and tell a
mentor, since that's a request for new robot code.

## 6. Follow the rules

The editor can't check these, so you have to:

- **Start** touching the wall, entirely on our side, not in the LOADING ZONE, not touching a FLOWER.
- **Never cross the centre line** (x = 70.75) with any part of the robot.
- **Never drive through a wall or a HIVE frame leg.** Under the HIVE there's about 1.6 in to spare.
- **Don't touch the HIVE.** Only POLLEN we launch may move it.
- **Park at least partly in our LOADING ZONE** at the end (5 points), off the wall.

Draw for **RED**. The robot mirrors your Auto for BLUE on its own.

## 7. Save your work

**Save** keeps your Auto in *this browser on this laptop only*. Clear the browser or switch laptops
and it's gone. To keep it:

- **Download it:** open the file list (top left) → **Download .pp to computer**. Name it after the
  plan: `left-rush.pp`, not `auto2.pp`.
- **Share it:** **Export → Share Link** gives a link that opens a copy of your Auto. Paste it in the
  GitHub issue or send it to a mentor.

Putting the `.pp` in the repo (next to its Java) happens later, with a mentor. The
[README](README.md) here explains how.

## Stuck?

- **"Which branch am I in?"** Click the branch's header in the list. New cards go at the end of the
  branch you selected.
- **A path starts in the wrong place?** A path starts where the robot is, which is the end of the
  card before it. Check the card order.
- **A red "not registered"?** You used a command or trigger the robot doesn't know. See step 5.
- **Something weird?** Undo (Ctrl+Z), then tell a mentor what you clicked. A new user finding a bug
  is useful, so don't keep it to yourself.
