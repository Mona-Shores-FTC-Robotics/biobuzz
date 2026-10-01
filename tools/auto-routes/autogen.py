"""Writes Auto Builder .pp files from a compact route description, then exports them.

The .pp stays the source of truth (open it in the Auto Builder to see or change a route); this is
a faster way to try many routes. AUTO_BUILDER_DIR is the Auto Builder checkout (default:
../visualizer next to this repository); it needs node.
"""
import json, math, os, subprocess, copy

REPO = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
PP_DIR = f"{REPO}/TeamCode/src/test/resources/auto-builder"
GEN_DIR = f"{REPO}/TeamCode/src/test/java/org/firstinspires/ftc/teamcode/opmodes/auto/generated"
VIS = os.environ.get("AUTO_BUILDER_DIR", os.path.join(REPO, "..", "visualizer"))
TEMPLATE = json.load(open(f"{PP_DIR}/spill-three-tip.pp"))
TYPICAL = {"LaunchOne": 0.5, "LaunchAll": 2.0, "ShootAll": 2.0, "CollectSeen": 2.0, "SpinUp": 0.1, "SpinDown": 0.1,
           "IntakeOn": 0.1, "IntakeOff": 0.1}

class Route:
    def __init__(self, name, start, speed=50, folder=PP_DIR):
        self.name, self.speed, self.folder = name, speed, folder
        self.points = {"START": list(start)}
        self.at = "START"
        self.lines, self.cards, self.path_ends = [], [], {}
        self.n = 0
        self.actions, self.conds = [], []

    def _id(self, p):
        self.n += 1
        return f"{p}-{self.n}"

    def pt(self, name, x, y, h):
        self.points[name] = [x, y, h]
        return self

    # cards ---------------------------------------------------------------------------------------
    def action(self, name):
        if name not in self.actions: self.actions.append(name)
        return {"id": self._id("a"), "kind": "action", "name": name}

    def wait(self, label, when=None, ms=None, alongside=None, yes=None, no=None, yes_label=None, no_label=None):
        rows = []
        if when:
            for c in when:
                if c not in self.conds: self.conds.append(c)
            row = {"when": list(when), "cards": yes or []}
            if yes_label: row["label"] = yes_label
            rows.append(row)
        if ms is not None:
            row = {"afterMs": int(ms), "cards": no or []}
            if no_label: row["label"] = no_label
            rows.append(row)
        card = {"id": self._id("w"), "kind": "firstOf", "label": label, "rows": rows}
        if alongside:
            card["alongside"] = alongside
            if alongside not in self.actions: self.actions.append(alongside)
        return card

    def go(self, to, ctrl=(), heading="linear", park=False):
        a, b = self.points[self.at], self.points[to]
        lid = f"to-{to.lower().replace('_', '-')}-{len(self.lines) + 1}"
        if heading == "linear":
            hd = {"type": "linear", "startDeg": a[2], "endDeg": b[2]}
        elif heading == "tangent":
            hd = {"type": "tangent"}
        else:
            hd = {"type": "constant", "degrees": heading}
        self.lines.append({"id": lid, "color": "#3cc8e4", "name": f"{self.at} to {to}", "waitBeforeMs": 0, "waitAfterMs": 0,
                           "waitBeforeName": "", "waitAfterName": "", "kind": "atomic",
                           "endPoint": {"x": b[0], "y": b[1]}, "controlPoints": [{"x": x, "y": y} for x, y in ctrl],
                           "heading": hd})
        self.path_ends[lid] = to
        self.at = to
        return {"id": self._id("p"), "kind": "path", "lineId": lid, "park": park}

    def add(self, *cards):
        self.cards.extend(cards)
        return self

    # output ----------------------------------------------------------------------------------------
    def doc(self):
        d = copy.deepcopy(TEMPLATE)
        s = self.points["START"]
        d["startPoint"] = {"x": s[0], "y": s[1], "name": "START", "headingDeg": s[2]}
        d["lines"] = self.lines
        d["sequence"] = [{"kind": "path", "lineId": l["id"]} for l in self.lines]
        d["settings"]["maxVelocity"] = self.speed
        d["settings"]["maxAcceleration"] = self.speed * 0.9
        d["settings"]["maxDeceleration"] = self.speed * 0.9
        d["auto"] = {"version": 1, "drawnFor": "RED", "exportName": self.name,
                     "registry": {"actions": self.actions, "conditions": self.conds,
                                  "typicalS": {a: TYPICAL.get(a, 1.0) for a in self.actions},
                                  "events": [c for c in self.conds if c == "Tip"]},
                     "points": self.points, "pathEnds": self.path_ends, "startAt": "START", "cards": self.cards}
        return d

    def write(self):
        path = f"{self.folder}/{self.name}.pp"
        json.dump(self.doc(), open(path, "w"), indent=2)
        out = subprocess.run(["node", "scripts/export-auto.mjs", path, GEN_DIR], cwd=VIS, capture_output=True, text=True)
        if out.returncode != 0:
            raise RuntimeError(out.stdout + out.stderr)
        for line in (out.stdout + out.stderr).splitlines():
            if "warning" in line or "load:" in line: print(line)
        return path

def study(specs, designs="spring hood", runs=10, extra_env=None):
    env = dict(os.environ, BIOBUZZ_AUTO_STUDY=specs, BIOBUZZ_AUTO_DESIGNS=designs,
               BIOBUZZ_AUTO_RUNS=str(runs))
    env.update(extra_env or {})
    if "ANDROID_HOME" not in env and os.path.isdir("/tmp/claude-0/android/sdk"):
        env["ANDROID_HOME"] = "/tmp/claude-0/android/sdk"  # this machine's SDK; set ANDROID_HOME elsewhere
    out = subprocess.run(["./gradlew", "-q", ":TeamCode:testDebugUnitTest", "--tests", "*AutoStudyTest*", "-i"], cwd=REPO, env=env,
                         capture_output=True, text=True)
    lines = [l.strip() for l in out.stdout.splitlines() if "STUDY" in l or "error:" in l or "FAILED" in l]
    if out.returncode != 0 and not lines:
        print(out.stdout[-3000:], out.stderr[-3000:])
    print("\n".join(lines))
    return lines

def branch(r, label, rows, ms=None, timeout_cards=None):
    """firstOf with several condition rows: rows = [(label, [conditions], [cards])]."""
    out = []
    for lab, conds, cards in rows:
        for c in conds:
            if c not in r.conds: r.conds.append(c)
        out.append({"when": list(conds), "label": lab, "cards": cards})
    if ms is not None:
        out.append({"afterMs": int(ms), "label": "Neither", "cards": timeout_cards or []})
    return {"id": r._id("d"), "kind": "firstOf", "label": label, "rows": out}
