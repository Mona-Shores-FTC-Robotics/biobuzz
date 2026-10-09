"""The whole robot as drawn, in one STEP (or two halves): the mentor's Robot.step with the parts we replaced left out,
plus the unified front (cad/intake-b/), the transfer (cad/transfer/), goBILDA's odometry pods and the Limelight 3A on its
goBILDA mount. In the AdvantageScope model's frame: +X forward, +Y left, +Z up, origin on the floor under the chassis
centre (7.56 in behind the front face). Millimetres. Usage and inputs: README.md.

What comes out of the mentor's CAD is SKIP (below). His "Launcher Concept" (the goBILDA turret over two pairs of
flywheels) moves 0.8 in forward, and the old intake's 11-hole channel is raised 30 mm, as the transfer needs."""
import math, os, re, sys, time
import numpy as np
import cadquery as cq
from OCP.STEPCAFControl import STEPCAFControl_Reader
from OCP.TDocStd import TDocStd_Document
from OCP.TCollection import TCollection_ExtendedString
from OCP.XCAFDoc import XCAFDoc_DocumentTool, XCAFDoc_ColorSurf, XCAFDoc_ColorGen
from OCP.TDF import TDF_LabelSequence, TDF_Label
from OCP.TDataStd import TDataStd_Name
from OCP.TopLoc import TopLoc_Location
from OCP.Quantity import Quantity_Color
from OCP.gp import gp_Trsf, gp_Vec
from OCP.Bnd import Bnd_Box
from OCP.BRepBndLib import BRepBndLib
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "intake-b")); sys.path.insert(0, os.path.join(HERE, "..", "robot-addons"))
import importlib.util
import real_parts as RP
def load(name, path):
    spec = importlib.util.spec_from_file_location(name, path); m = importlib.util.module_from_spec(spec); spec.loader.exec_module(m); return m
IB = load("intake_b", os.path.join(HERE, "..", "intake-b", "build.py"))
TR = load("transfer", os.path.join(HERE, "..", "transfer", "build.py"))
A = IB.A
C, F, FACE, IN = A.C, A.F, A.FACE, A.IN
# Left out of the mentor's CAD: the old intake roller, its motor and mount, its guides on the towers (V-groove bearings and
# their cavities, and the roller's own bearings) and its spacers (our floating roller replaces them); his pinwheel, its
# servo, block and shaft (our FLOWER extractor replaces it); his two 3.5 in star wheels (the transfer's wheel lane
# replaces them); his staged NECTAR and POLLEN; and the launcher's front cross-channel with its two dual blocks (the
# lane's balls run through where it is; the 8-hole channel above still ties the frame).
SKIP = re.compile(r"Intake <1> / (48mm Gecko|240mm Steel|5000|5103|5203|Pattern Spacer|1201-0043|V-Groove|Cavity1|8x14x5mm Bearing|4\.5in OD|Servo|Compact Servo Block"
                  r"|3700-0145-0288|1108-0001-0002|1222-0001-0001|3312-4008-0008)"   # his intake carriage: the V-guides it rode, its plates, mount and hubs
                  r"|^3\.5in OD|Nectar|Pollen|Wheel Assembly <\d> / 72mm Steel Shaft"
                  r"|Launcher subassembly <\d> / (312rpm Motor|7x11 hole Aluminum Plate|1 Hole Lowside U-Channel|Mini Quad Block|16t HTD5 Pulley|\[LS\] HTD5 belt 68|Dual Block)"
                  r"|Launcher subassembly <2> / 96mm Steel Shaft|41T HTD5 Pulley")
# (the last line: the flywheel motors, their belts and the plates and blocks that held them under the flywheels, where the
# feeders go; cad/transfer/ draws the motors moved out and up), and the left flywheel's 96 mm shaft (cad/transfer/ draws a
# longer one that drives the feeder and carries its yoke), and his custom 41T flywheel pulleys (goBILDA 24T in their place)   # the drive wheels' shafts: our 80 mm ones (outer plates) replace them
RIGHT_TIES = re.compile(r"Launcher Concept <1> / (1301-0016-4012|5 Hole U Beam - 40mm|5 Square Beam - 40mm|32mm Steel Shaft - 12mm REX)")   # tied to the right module: they move with it
RAISE = re.compile(r"Intake <1> / 11 Hole Lowside")    # up TR.CHAN_RAISE (30 mm): a lane NECTAR passes under it
LAUNCHER = re.compile(r"^Launcher Concept|^Dual Block \(GB\)")   # the launcher, turret and the two blocks tying its frame to the chassis
FRONT_OF_LAUNCHER = re.compile(r"^Launcher Concept <1> / (10 Hole Lowside U-Channel \(GB\)|Dual Block \(GB\)|5 Hole U Beam - 40mm \(GB\))\s*$")

def read_team(path):
    t0 = time.time()
    doc = TDocStd_Document(TCollection_ExtendedString("doc"))
    r = STEPCAFControl_Reader(); r.SetNameMode(True); r.SetColorMode(True)
    r.ReadFile(path); r.Transfer(doc)
    st = XCAFDoc_DocumentTool.ShapeTool_s(doc.Main()); ct = XCAFDoc_DocumentTool.ColorTool_s(doc.Main())
    def name(l):
        a = TDataStd_Name()
        return a.Get().ToExtString() if l.FindAttribute(TDataStd_Name.GetID_s(), a) else "?"
    def colour(*labels):
        q = Quantity_Color()
        for l in labels:
            s = st.GetShape_s(l)
            for kind in (XCAFDoc_ColorSurf, XCAFDoc_ColorGen):
                if ct.GetColor(s, kind, q): return (q.Red(), q.Green(), q.Blue())
        return None
    leaves, base = [], {}
    def walk(l, loc, path, col):
        if st.IsReference_s(l):
            ref = TDF_Label(); st.GetReferredShape_s(l, ref)
            walk(ref, loc.Multiplied(st.GetLocation_s(l)), path + [name(l)], colour(l) or col); return
        if st.IsAssembly_s(l):
            seq = TDF_LabelSequence(); st.GetComponents_s(l, seq)
            for i in range(1, seq.Length() + 1): walk(seq.Value(i), loc, path, col)
            return
        key = l.Tag()                                     # one entry per distinct part, so repeated parts stay instances
        if key not in base: base[key] = cq.Shape.cast(st.GetShape_s(l))
        leaves.append((path, key, loc, colour(l) or col or (0.67, 0.7, 0.74)))
    roots = TDF_LabelSequence(); st.GetFreeShapes(roots)
    for i in range(1, roots.Length() + 1): walk(roots.Value(i), TopLoc_Location(), [], None)
    print("team CAD:", len(leaves), "parts,", len(base), "distinct, read in", round(time.time() - t0), "s")
    return leaves, base

def limelight_mount():
    """The stand-in mount under the Limelight 3A at config.json's camera (lens 4.0 in ahead of the centre, 14.0 in up,
    pitched 45 deg up): a beam between the front towers' tops, a plate, and a printed 45 deg wedge under the camera's
    bottom face. The real mount is the Limelight chat's to draw. Robot frame, inches."""
    lens, a = cq.Vector(4.0, 0, 14.0), math.radians(45)
    n, u = cq.Vector(math.cos(a), 0, math.sin(a)), cq.Vector(-math.sin(a), 0, math.cos(a))
    back, low = (RP.LL_LENS[1] - RP.LL_BACK) / IN, (RP.LL_LENS[2] + 2.9) / IN     # lens to the back face; lens to the bottom face
    e0, e1 = lens - u * low - n * back, lens - u * low
    wedge = cq.Workplane("XZ").polyline([(e0.x, e0.z), (e1.x, e1.z), (5.4, 12.25), (min(e0.x, e1.x), 12.25)]).close().extrude(-2.4).translate((0, -1.2, 0))
    plate = cq.Workplane("XY").box(2.7, 2.4, 0.12).translate((5.55, 0, 12.19))
    beam = cq.Workplane("XY").box(0.6, 9.7, 0.63).translate((6.6, 0, 11.815))
    return [("Limelight wedge (print, stand-in)", wedge, (0.18, 0.37, 0.62)), ("Limelight plate (stand-in)", plate, (0.18, 0.37, 0.62)),
            ("Limelight beam between the front towers (stand-in)", beam, (0.67, 0.7, 0.74))]

def placed_team(robot_step):
    """The mentor's parts we keep, lined up and edited: [(path, base shape, TopLoc_Location, colour)]."""
    leaves, base = read_team(robot_step)
    def bbox(key, loc):
        b = Bnd_Box(); BRepBndLib.Add_s(base[key].wrapped.Moved(loc), b); return b.Get()
    # the mentor's exports move his assembly's origin: line it up by the drive rails (front ends at FACE, right one at C+136 mm)
    rails = [bbox(k, l) for pth, k, l, c in leaves if pth and pth[-1].startswith("1107-0015-0384")]
    dz, dx = FACE - max(b[5] for b in rails), (C + 136.0) - max(b[3] for b in rails)
    print(f"aligned by the rails: +{dx:.2f} mm across, +{dz:.2f} mm forward")
    placed_team.align = (dx, dz)
    tr = gp_Trsf(); tr.SetTranslation(gp_Vec(dx, 0, dz)); align = TopLoc_Location(tr)
    def moved(v): t = gp_Trsf(); t.SetTranslation(gp_Vec(*v)); return TopLoc_Location(t)
    out = []
    lean = os.environ.get("LEAN")                     # LEAN=1: leave out fasteners, for a single small download
    for path, key, loc, col in leaves:
        p = " / ".join(path)
        if SKIP.search(p): continue
        if lean and re.search(r"[Ss]crew|Nut|[Ww]asher|2800-|2802-|2812-|2806-|2829-|CAGE|e-clip|shim|_9\d{4}A\d{3}| text", p): continue
        loc = align.Multiplied(loc)
        if RAISE.search(p): loc = moved((0, TR.CHAN_RAISE * IN, 0)).Multiplied(loc)
        if FRONT_OF_LAUNCHER.search(p):
            b = bbox(key, loc); x_model = ((b[2] + b[5]) / 2 - (FACE - 7.56 * IN)) / IN; y_model = ((b[0] + b[3]) / 2 - C) / IN
            if x_model > -2.0:
                continue                                  # the front cross-channel, its two dual blocks and the two U-beams under it
        if re.search(r"Launcher subassembly <\d> / (64mm Standoff|27mm Standoff|\d+\.?\d*mm Spacer)", p):
            b = bbox(key, loc)
            if (b[1] + b[4]) / 2 - F < 4.3 * IN: continue   # the old motor's standoffs and spacers, where the feeders go
        if LAUNCHER.search(p): loc = moved((0, 0, TR.LAUNCHER_SHIFT * IN)).Multiplied(loc)   # forward: the lane's length sets the 3 NECTAR / 4 POLLEN limit
        side = 1 if "Launcher subassembly <2>" in p else -1 if "Launcher subassembly <1>" in p else 0
        if not side and RIGHT_TIES.search(p):
            b = bbox(key, loc); side = -1 if (b[0] + b[3]) / 2 - C < 0 else 0
        if side: loc = moved((-side * TR.FLY_IN * IN, 0, 0)).Multiplied(loc)   # the flywheel modules move in, 145 mm apart
        out.append((path, base[key], loc, col, key))
    return out

def write_mesh(robot_step, out_pkl):
    """The kept, placed parts as meshes for cad/advantagescope/build_model.py: [(path, vertices (CAD inches), faces, colour)],
    the format tools/robot-cad/slim.py writes. Fasteners and tiny parts are left out, as slim.py does."""
    import pickle
    rows, cache = [], {}
    for path, shp, loc, col, key in placed_team(robot_step):
        p = " / ".join(path)
        if re.search(r"text|e-clip|shim|Screw|screw|Bearing|pins|Nut|nut|Washer|washer|Cavity|2800-|2802-|2829-|CAGE", p): continue
        if key not in cache:
            v, f = shp.tessellate(0.4, 0.5); cache[key] = (np.array([(q.x, q.y, q.z) for q in v]), np.array(f))
        v, f = cache[key]
        if not len(f): continue
        T = loc.Transformation(); M = np.array([[T.Value(i, j) for j in range(1, 5)] for i in range(1, 4)])
        vv = (v @ M[:, :3].T + M[:, 3]) / IN
        if np.linalg.norm(vv.max(0) - vv.min(0)) < 0.35: continue
        rows.append((p, vv, f, col))
    pickle.dump(rows, open(out_pkl, "wb")); print(len(rows), "meshes ->", out_pkl)

# What moves, for Onshape: the top level is FRAME (fix it) and one group per moving body, so the only mates left to add
# are the ones that make it move. Each name says the mate; cad/full-robot/ONSHAPE.md says where to click.
MOVING = [
    ("carriage", "MOVES 1 - roller carriage (Slider, straight up 0 to 1.3 in)"),
    ("roller", "MOVES 2 - intake roller (Revolute on the carriage)"),
    ("extractor", "MOVES 3 - FLOWER extractor (Revolute, 0 down to 146 deg stowed)"),
    ("servo_gear", "MOVES 4 - extractor servo gear (Revolute; Gear relation 1:1 with MOVES 3, opposite way)"),
    ("turret", "MOVES 5 - turret ring (Revolute about vertical)"),
    ("fly_L", "MOVES 6 - flywheel left (Revolute)"),
    ("fly_R", "MOVES 7 - flywheel right (Revolute)"),
    ("feeder", "MOVES 8 - feeder (Revolute)"),
    ("pad", "MOVES 9 - sprung pad (Revolute at its hinge, swings out for a NECTAR)"),
    ("turret_gear", "MOVES 10 - turret drive gear (Revolute; Gear relation with MOVES 5)"),
    ("feeder_yoke", "MOVES 11 - feeder yoke (Revolute about the left flywheel's shaft: the gate servo swings it 10 deg)"),
    ("turret_servo", "MOVES 12 - turret servo's hub and pulley (Revolute; Belt relation 1:1 with MOVES 10)"),
    ("turret_enc_B", "MOVES 13 - encoder B gear (Revolute; Gear relation with MOVES 5, 176:48)"),
]
FLY_L_SPINS = re.compile(r"^flywheel_(shaft_L|spacers_L|feeder_pulley|shaft_eclip_L|pulley_L)")   # ours, on the left flywheel's shaft
TURRET_GEAR_TURNS = re.compile(r"^turret_gear_(shaft|pulley|spacers|eclip)|^turret_enc_A_magnet")
TURRET_SERVO_TURNS = re.compile(r"^turret_servo_(hub|pulley)")
ENC_B_TURNS = re.compile(r"^turret_enc_B_(shaft|gear|spacers|eclip|magnet)")             # ours, on the turret drive gear's shaft
TURRET_TURNS = re.compile(r"1628-0105-0001-Inner-Race|1600-0001-0120:1 <1> / IR:|2325-0105-0176")
TURRET_GEAR = re.compile(r"2302-0014-0064")              # the gear beside the ring that drives it
TURRET_AXIS = (-2.045, 0.155)                          # model X, Y (the launcher moved forward)
FASTENER = re.compile(r"[Ss]crew|Nut|2800-|2802-|2812-|2829-")
FLYWHEELS = {"Launcher subassembly <2>": ("fly_L", TR.FLY_Y[0]), "Launcher subassembly <1>": ("fly_R", TR.FLY_Y[1])}
ROLLER_SPINS = re.compile(r"^roller_shaft|^roller_centre_wheel|^roller_vector|^roller_lane_pulley|^roller_pulley|^roller_collar|^roller_eclip")

def mentor_motion(path, shp, loc):
    """Which moving group a part of the mentor's goes in, or None (the frame)."""
    p = " / ".join(path)
    if not p.startswith("Launcher Concept"): return None
    if TURRET_TURNS.search(p): return "turret"
    if TURRET_GEAR.search(p): return "turret_gear"
    if FASTENER.search(p):                             # the screws that hold the ring gear to the inner race turn with them
        b = Bnd_Box(); BRepBndLib.Add_s(shp.wrapped.Moved(loc), b); x0, y0, z0, x1, y1, z1 = b.Get()
        X, Y, Z = ((z0 + z1) / 2 - (FACE - 7.56 * IN)) / IN, ((x0 + x1) / 2 - C) / IN, ((y0 + y1) / 2 - F) / IN
        if math.hypot(X - TURRET_AXIS[0], Y - TURRET_AXIS[1]) < 2.5 and 8.6 < Z < 10.3: return "turret"
        return None
    sub = next((k for k in FLYWHEELS if k in p), None)
    if sub and not re.search(r"Bearing|belt|Channel|Block|Motor|motor|Spacer", p):
        b = Bnd_Box(); BRepBndLib.Add_s(shp.wrapped.Moved(loc), b); x0, y0, z0, x1, y1, z1 = b.Get()
        yc, zc = ((x0 + x1) / 2 - C) / IN, ((y0 + y1) / 2 - F) / IN       # the part's centre, model Y and z
        if abs(yc - FLYWHEELS[sub][1]) < 0.35 and abs(zc - 6.646) < 0.35: return FLYWHEELS[sub][0]
    return None

JUNK = re.compile(r"^(NAUO\d*|=>\[[\d:]+\]|PATCH(:\d+)?|SOLID|Body\d*|Part\d*|\d+|COMPOUND|Open CASCADE STEP translator.*|)$", re.I)
def part_name(raw, owner=None):
    """A readable instance name: the mentor's part names without CAD suffixes (":1__Body1", "rev1"), and for a vendor
    model's pieces with meaningless names (NAUO1, =>[0:1:1:4]), the part they belong to."""
    n = re.sub(r"(:\d+)?(__Body\d+)?$", "", re.sub(r"^\d+ ", "", str(raw))).strip()
    n = re.sub(r"\s+(rev\d+)$", "", n)
    if JUNK.match(n) or JUNK.match(n.split(".")[0]): n = (owner.split(" (")[0] if owner else "part") + " piece"
    return n[:110]
def unique(assy, name):
    """cadquery needs unique names within an assembly: the second "x" becomes "x (2)"."""
    taken = {c.name for c in assy.children}
    if name not in taken: return name
    k = 2
    while f"{name} ({k})" in taken: k += 1
    return f"{name} ({k})"

def main(robot_step, out, additions=False, only=None):
    """The whole robot in the model frame, as FRAME and the moving groups; or, with additions, only our parts, in the
    frame of the mentor's Robot.step, so they drop into his Onshape assembly at its origin. only="mentor" or "ours"
    writes half of the whole robot (same frame, same groups), for two files that each stay a manageable size."""
    if additions:
        dx, dz = (placed_team(robot_step), placed_team.align)[1]; t = gp_Trsf(); t.SetTranslation(gp_Vec(-dx, 0, -dz))
        top = cq.Assembly(name="BIOBUZZ additions (the frame of the mentor's Robot.step: insert at the origin)"); where = cq.Location(TopLoc_Location(t))
    else:
        top = cq.Assembly(name={"mentor": "BIOBUZZ 1 - the mentor's robot", "ours": "BIOBUZZ 2 - our parts"}.get(only, "BIOBUZZ robot")
                          + " (model frame: +X forward, +Y left, +Z up, mm)")
        where = cq.Location(RP.to_model(C, F, FACE))     # on each group, so the file has no wrapper layer
    frame = cq.Assembly(name={"mentor": "FRAME - the mentor's robot (right-click, Fix)", "ours": "FRAME - our parts (right-click, Fix)"}.get(only, "FRAME - everything that doesn't move (right-click, Fix)"))
    moving = {k: cq.Assembly(name=t) for k, t in MOVING}
    pods = RP.pods_inst(os.environ["EXAMPLE_STEP"], A.POD_MOVE) if os.environ.get("EXAMPLE_STEP") else {}
    if not additions and only != "ours":
        team = cq.Assembly(name="team robot CAD (the mentor's Robot.step, 7 Oct), replaced parts left out")
        kept = 0
        for i, (path, shp, loc, col, key) in enumerate(placed_team(robot_step)):
            g = mentor_motion(path, shp, loc)
            dest = moving[g] if g else team
            dest.add(shp, name=unique(dest, part_name(path[-1] if path else "part")), loc=cq.Location(loc), color=cq.Color(*col)); kept += 1
        print("kept", kept)
        frame.add(team)
    vdir = os.environ.get("VENDOR_DIR")                  # goBILDA's and WCP's STEPs: the real parts in place of drawn envelopes
    ours = only != "mentor"
    def add_part(sub, n, shp, col, others):
        real = RP.fastener_parts(n, vdir) if vdir else None
        if real is None: real = RP.vendor_parts(n, shp, others, vdir) if vdir else None
        if real is None: real = RP.servo_parts(n, shp, IB, vdir) if vdir else None
        if real is None: sub.add(shp, name=n, color=cq.Color(*col)); return
        va = cq.Assembly(name=n)
        for k, (pn, vs, loc, vc) in enumerate(real): va.add(vs, name=unique(va, part_name(pn, n)), loc=cq.Location(loc), color=cq.Color(*vc))
        sub.add(va)
    for title, g, d in IB.GROUPS if ours else ():
        sub = cq.Assembly(name=f"front: {title}")
        shapes = {n: (wp.val() if hasattr(wp, "val") else wp) for n, (wp, col, kind) in d.items()}
        for n, (wp, col, kind) in d.items():
            if "STAND-IN" in n and pods: continue
            dest = (moving["roller"] if ROLLER_SPINS.search(n) else moving["carriage"]) if g == "float" else \
                   moving["extractor"] if g == "hook" else moving["servo_gear"] if n.startswith("servo_gear") else sub
            add_part(dest, n, shapes[n], col, shapes)
        if title.startswith("chassis"):
            for n, parts in pods.items():
                pod = cq.Assembly(name=n)
                for k, (pn, shp, loc, col) in enumerate(parts): pod.add(shp, name=unique(pod, part_name(pn, n)), loc=cq.Location(loc), color=cq.Color(*col))
                sub.add(pod)
        frame.add(sub)
    for title, g, d in TR.GROUPS if ours else ():
        sub = cq.Assembly(name=f"transfer: {title}")
        shapes = {n: TR.to_cad(wp) for n, (wp, col, kind) in d.items()}
        for n, (wp, col, kind) in d.items():
            dest = moving["feeder"] if re.match(TR.FEEDER_SPINS, n) else moving["pad"] if re.match(TR.PAD_SWINGS, n) else \
                   moving["feeder_yoke"] if re.match(TR.FEEDER_SWINGS, n) else moving["fly_L"] if FLY_L_SPINS.match(n) else moving["fly_R"] if n.startswith("flywheel_pulley_R") else \
                   moving["turret_gear"] if TURRET_GEAR_TURNS.match(n) else moving["turret_servo"] if TURRET_SERVO_TURNS.match(n) else moving["turret_enc_B"] if ENC_B_TURNS.match(n) else sub
            add_part(dest, n, shapes[n], col, shapes)
        frame.add(sub)
    ll = cq.Assembly(name="Limelight 3A on its goBILDA mount")
    mount = RP.limelight_mount_in_cad(vdir, C, F, FACE) if vdir else []
    for k, (pn, shp, loc, col) in enumerate(mount): ll.add(shp, name=unique(ll, part_name(pn, "Limelight mount")), loc=cq.Location(loc), color=cq.Color(*col))
    if not mount:
        for n, wp, col in limelight_mount(): ll.add(TR.to_cad(wp), name=n, color=cq.Color(*col))
    else:
        llf = RP.limelight_fasteners({}, C, F, FACE)
        for n, (wp, col, kind) in llf.items(): add_part(ll, n, wp.val() if hasattr(wp, "val") else wp, col, {})
    if os.environ.get("LL_STEP"):
        cam = cq.Assembly(name="Limelight 3A (LIMELIGHT3ACAD_STEP.stp)")
        for k, (pn, shp, col) in enumerate(RP.limelight_in_cad(os.environ["LL_STEP"], C, F, FACE)):
            cam.add(shp, name=unique(cam, part_name(pn, "Limelight 3A")), color=cq.Color(*col))
        ll.add(cam)
    if ours: frame.add(ll)
    top.add(frame, loc=where)
    for k, t in MOVING:
        if moving[k].children or moving[k].shapes: top.add(moving[k], loc=where)
        elif not only: print("empty:", t)
    t0 = time.time(); top.save(out); print("wrote", out, os.path.getsize(out) // 1_000_000, "MB in", round(time.time() - t0), "s")

if __name__ == "__main__":
    if sys.argv[1] == "--mesh": write_mesh(*sys.argv[2:4])
    elif sys.argv[1] == "--additions": main(*sys.argv[2:4], additions=True)
    elif sys.argv[1] in ("--mentor", "--ours"): main(*sys.argv[2:4], only=sys.argv[1][2:])
    else: main(*sys.argv[1:3])
