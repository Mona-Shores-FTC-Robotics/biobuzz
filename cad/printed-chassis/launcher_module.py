"""Brings the mentor's launcher module (cad/modules/launcher-module on claude/robotics-meeting-notes-lq2y55: his goBILDA
Launcher Concept with cad/transfer's launcher changes) into the printed chassis, placed by the launch column.

    git show origin/claude/robotics-meeting-notes-lq2y55:cad/modules/launcher-module/launcher-module.step.gz | gunzip > /tmp/lm.step
    python3 cad/printed-chassis/launcher_module.py /tmp/lm.step     # writes cad/printed-chassis/.cache/launcher/ (not in git)

build.py loads the cache when it's there. The module is in the full robot's frame (its column at X -2.045, Y 0.158); here
it moves to this chassis's column. His own lane (the parts LANE below) is left out: this chassis has its own.
"""
import json, os, re, sys
import cadquery as cq
from OCP.STEPCAFControl import STEPCAFControl_Reader
from OCP.TDocStd import TDocStd_Document
from OCP.TCollection import TCollection_ExtendedString
from OCP.XCAFDoc import XCAFDoc_DocumentTool
from OCP.TDF import TDF_LabelSequence, TDF_Label
from OCP.TDataStd import TDataStd_Name
from OCP.TopLoc import TopLoc_Location
from OCP.BRepTools import BRepTools

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, ".cache", "launcher")
MODULE_COLUMN_IN = (-2.045, 0.158)
LANE = re.compile(r"^(Part 1|foam tube hub|Hole Lowside U-Channel \(GB\) \((5|7)\))")   # his lane: the centre channels, hubs and balls

def read(path, full=False):
    doc = TDocStd_Document(TCollection_ExtendedString("doc"))
    r = STEPCAFControl_Reader(); r.SetNameMode(True); r.ReadFile(path); r.Transfer(doc)
    st = XCAFDoc_DocumentTool.ShapeTool_s(doc.Main())
    def name(lab):
        n = TDataStd_Name()
        return n.Get().ToExtString() if lab.FindAttribute(TDataStd_Name.GetID_s(), n) else "?"
    out = []
    def walk(lab, loc, path):
        if st.IsReference_s(lab):
            r2 = TDF_Label(); st.GetReferredShape_s(lab, r2)
            walk(r2, loc.Multiplied(st.GetLocation_s(lab)), path + [name(lab)]); return
        if st.IsAssembly_s(lab):
            seq = TDF_LabelSequence(); st.GetComponents_s(lab, seq)
            for i in range(1, seq.Length() + 1): walk(seq.Value(i), loc, path)
            return
        out.append(((path or [name(lab)]) if full else (path[-1] if path else name(lab)), st.GetShape_s(lab).Moved(loc)))
    roots = TDF_LabelSequence(); st.GetFreeShapes(roots)
    for i in range(1, roots.Length() + 1): walk(roots.Value(i), TopLoc_Location(), [])
    return out

def read_groups(path):
    """The same, as {top-level group: [(name, shape)]}."""
    groups = {}
    for full, sh in read(path, full=True):
        groups.setdefault(full[0], []).append((full[-1], sh))
    return groups

if __name__ == "__main__":
    sys.path.insert(0, HERE)
    import importlib.util
    spec = importlib.util.spec_from_file_location("pc", os.path.join(HERE, "build.py")); pc = importlib.util.module_from_spec(spec)
    os.environ["NO_LAUNCHER_MODULE"] = "1"; spec.loader.exec_module(pc)
    dx, dy = (pc.COL_X - MODULE_COLUMN_IN[0]) * 25.4, (0.0 - MODULE_COLUMN_IN[1]) * 25.4
    os.makedirs(OUT, exist_ok=True)
    index, seen = [], {}
    for nm, sh in read(sys.argv[1]):
        if LANE.search(nm): continue
        s = cq.Shape.cast(sh).translate(cq.Vector(dx, dy, 0)).scale(1 / 25.4)        # into this chassis's frame, inches
        base = re.sub(r"[^A-Za-z0-9_.-]+", "_", nm)[:60]; seen[base] = seen.get(base, 0) + 1
        fn = f"{base}_{seen[base]}.brep"
        BRepTools.Write_s(s.wrapped, os.path.join(OUT, fn))
        index.append({"name": nm, "file": fn})
    json.dump({"shift_mm": [dx, dy], "parts": index}, open(os.path.join(OUT, "index.json"), "w"), indent=1)
    print(len(index), "parts written to", OUT)
