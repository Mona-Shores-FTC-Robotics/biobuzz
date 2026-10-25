"""The Limelight as it sits in the flowerextractordown export, moved to the planned camera spot in THAT assembly's own
coordinates, written as a STEP to insert at the origin of the Fresh CAD assembly (no mates needed)."""
import sys, importlib.util, cadquery as cq
from OCP.gp import gp_Trsf, gp_Vec
from OCP.TopLoc import TopLoc_Location
from OCP.TopoDS import TopoDS_Compound
from OCP.BRep import BRep_Builder
sys.path.insert(0, "/home/user/biobuzz/cad/full-robot")
s = importlib.util.spec_from_file_location("fr", "/home/user/biobuzz/cad/full-robot/build.py"); FR = importlib.util.module_from_spec(s); s.loader.exec_module(FR)
DX, DZ = -115.62, 71.73                                   # placed_team's alignment for this export (list.txt): aligned = raw + (DX, 0, DZ)
SHIFT = (-0.32 * 25.4, 2.14 * 25.4, 0.12 * 25.4)          # raw mm: x ahead, y up, z = -left. Measured: the camera sits 0.32 fwd, 2.14 low, 0.12 left of the plan
b = BRep_Builder(); comp = TopoDS_Compound(); b.MakeCompound(comp); n = 0
for path, shp, loc, col, key in FR.placed_team(sys.argv[1], raw=True):
    if "Limelight" not in path[0]: continue
    t = gp_Trsf(); t.SetTranslation(gp_Vec(-DX + SHIFT[0], SHIFT[1], -DZ + SHIFT[2]))
    b.Add(comp, shp.wrapped.Moved(loc).Moved(TopLoc_Location(t))); n += 1
out = sys.argv[2]; cq.exporters.export(cq.Shape.cast(comp), out)
bb = cq.Shape.cast(comp).BoundingBox(); print(n, "bodies ->", out, "bounds raw mm: x %.1f..%.1f y %.1f..%.1f z %.1f..%.1f" % (bb.xmin, bb.xmax, bb.ymin, bb.ymax, bb.zmin, bb.zmax))
