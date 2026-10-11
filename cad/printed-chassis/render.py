"""Shaded views of the robot as built: every bought part as its real model (vendor.py), printed parts in their
materials' colours. VTK, off screen (it starts its own Xvfb when there's no display).

    python3 cad/printed-chassis/render.py                    # views/*.png: three-quarter, front, side, top, rear-under
    python3 cad/printed-chassis/render.py --near X Y Z R     # views/near.png: everything within R in of a point, four sides

build.py calls render(out_dir) after a full build.
"""
import math, os, shutil, subprocess, sys, time
HERE = os.path.dirname(os.path.abspath(__file__))

# Printed parts by material, read from the part's description; bought parts keep their models' colours
MATERIAL = (("TPU", (0.96, 0.70, 0.16)), ("PETG-CF", (0.20, 0.22, 0.25)), ("polycarbonate", (0.80, 0.88, 0.95)),
            ("aluminium", (0.80, 0.81, 0.83)), ("PETG", (0.16, 0.42, 0.85)))
ENVELOPE = (0.62, 0.62, 0.66)           # a bought part with no model: its envelope

def _display():
    if os.environ.get("DISPLAY") or not shutil.which("Xvfb"): return None
    d = ":%d" % (90 + os.getpid() % 9)
    p = subprocess.Popen(["Xvfb", d, "-screen", "0", "2400x1800x24"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    time.sleep(1.0); os.environ["DISPLAY"] = d
    return p

def colour(B, k, p):
    if p["kind"] != "print": return ENVELOPE if p["kind"] == "buy" else (0.80, 0.81, 0.83)
    for word, c in MATERIAL:
        if word in p["what"]: return c
    return MATERIAL[-1][1]

def scene(B, keep=lambda b: True):
    """[(vtkPolyData, colour, opacity)]: tessellated once per shape, placed by each look's location."""
    import vtk
    from vtk.util.numpy_support import numpy_to_vtk, numpy_to_vtkIdTypeArray
    import numpy as np
    cache, out = {}, []
    def mesh(sh, tol):
        key = (id(sh), tol)
        if key not in cache:
            v, f = sh.tessellate(tol, 0.25)
            cache[key] = (np.array([(q.x, q.y, q.z) for q in v], float), np.array(f, np.int64).reshape(-1, 3), sh)
        return cache[key]
    def poly(v, f, T=None):
        if T is not None: v = v @ T[:3, :3].T + T[:3, 3]
        pts = vtk.vtkPoints(); pts.SetData(numpy_to_vtk(np.ascontiguousarray(v), deep=True))
        cells = vtk.vtkCellArray()
        conn = np.hstack([np.full((len(f), 1), 3, np.int64), f]).ravel()
        cells.SetCells(len(f), numpy_to_vtkIdTypeArray(conn, deep=True))
        pd = vtk.vtkPolyData(); pd.SetPoints(pts); pd.SetPolys(cells)
        n = vtk.vtkPolyDataNormals(); n.SetInputData(pd); n.SetFeatureAngle(40); n.SplittingOn(); n.Update()
        return n.GetOutput()
    def T_of(loc):
        t = loc.wrapped.Transformation(); M = np.eye(4)
        for i in range(3):
            for j in range(4): M[i, j] = t.Value(i + 1, j + 1)
        return M
    for k, p in B.PARTS.items():
        b = B.bb(p["shape"])
        if not keep(b): continue
        lp = B.look_parts(k)
        if lp:
            for sh, loc, c in lp:
                v, f, _ = mesh(sh, 0.01)
                out.append((poly(v, f, T_of(loc)), c, 1.0))
        else:
            v, f, _ = mesh(B.solid(p["shape"]), 0.01)
            out.append((poly(v, f), colour(B, k, p), 1.0))
    for f in getattr(B, "FASTENERS", []):
        try:
            sh = B.solid(B.screw_solids(f))
        except Exception: continue
        if not keep(B.bb(sh)): continue
        v, fc, _ = mesh(sh, 0.01); out.append((poly(v, fc), (0.12, 0.12, 0.13), 1.0))
    return out

VIEWS = {   # name: (camera direction toward the viewer, view up, title)
    "three-quarter": ((0.62, -0.62, 0.48), (0, 0, 1), "Front right, from above"),
    "front": ((1, 0, 0), (0, 0, 1), "Front"),
    "side": ((0, 1, 0), (0, 0, 1), "Left side"),
    "top": ((0, 0, 1), (1, 0, 0), "From above (front up)"),
    "rear-under": ((-0.6, 0.55, -0.45), (0, 0, 1), "Rear left, from below"),
}

def _renderer(parts, cam, up, title, vp=(0, 0, 1, 1), focus=None, parallel=False):
    import vtk
    actors = _actors(parts)                 # fresh actors per window: VTK's GL resources don't survive a window
    r = vtk.vtkRenderer(); r.SetViewport(*vp)
    r.SetBackground(0.97, 0.97, 0.98); r.SetBackground2(0.82, 0.85, 0.90); r.GradientBackgroundOn()
    for a in actors: r.AddActor(a)
    r.SetUseFXAA(True)
    kit = vtk.vtkLightKit(); kit.SetKeyLightIntensity(1.0); kit.AddLightsToRenderer(r)
    c = r.GetActiveCamera(); f = focus or (0, 0, 5)
    c.SetFocalPoint(*f); c.SetPosition(*(f[i] + 60 * cam[i] for i in range(3))); c.SetViewUp(*up)
    c.SetViewAngle(18); c.SetParallelProjection(parallel)
    r.ResetCamera(); c.Zoom(1.15 if not parallel else 1.0)
    t = vtk.vtkTextActor(); t.SetInput(title); tp = t.GetTextProperty(); tp.SetColor(0.15, 0.15, 0.18); tp.SetFontSize(22); tp.SetFontFamilyToArial()
    t.SetDisplayPosition(14, 10); r.AddActor2D(t)
    return r

def _actors(parts):
    import vtk
    out = []
    for pd, c, op in parts:
        m = vtk.vtkPolyDataMapper(); m.SetInputData(pd); m.ScalarVisibilityOff()
        a = vtk.vtkActor(); a.SetMapper(m); pr = a.GetProperty()
        pr.SetColor(*c); pr.SetOpacity(op); pr.SetAmbient(0.18); pr.SetDiffuse(0.78); pr.SetSpecular(0.25); pr.SetSpecularPower(30)
        out.append(a)
    return out

def _shot(r, size):
    """One renderer to an RGB array."""
    import vtk
    from vtk.util.numpy_support import vtk_to_numpy
    win = vtk.vtkRenderWindow(); win.SetOffScreenRendering(1); win.SetSize(*size); win.SetMultiSamples(0); win.AddRenderer(r); win.Render()
    f = vtk.vtkWindowToImageFilter(); f.SetInput(win); f.SetInputBufferTypeToRGB(); f.ReadFrontBufferOff(); f.Update()
    im = f.GetOutput(); w, h, _ = im.GetDimensions()
    a = vtk_to_numpy(im.GetPointData().GetScalars()).reshape(h, w, 3)[::-1]
    win.Finalize(); return a

def _save(a, path):
    from PIL import Image
    Image.fromarray(a).save(path)

def render(B, out_dir, size=(1800, 1350)):
    import vtk
    xv = _display()
    try:
        acts = scene(B)
        os.makedirs(out_dir, exist_ok=True)
        for name, (cam, up, title) in VIEWS.items():
            _save(_shot(_renderer(acts, cam, up, title, parallel=name in ("front", "side", "top")), size), os.path.join(out_dir, f"{name}.png"))
        return list(VIEWS)
    finally:
        if xv: xv.terminate()

def near(B, point, radius, path):
    import vtk
    xv = _display()
    try:
        keep = lambda b: all(b[2 * i] < point[i] + radius and point[i] - radius < b[2 * i + 1] for i in range(3))
        acts = scene(B, keep)
        import numpy as np
        shots = [_shot(_renderer(acts, cam, up, f"{title}: within {radius} in of {tuple(round(v, 2) for v in point)}", focus=point), (900, 675))
                 for cam, up, title in (((1, -0.8, 0.6), (0, 0, 1), "front right"), ((-1, -0.8, 0.6), (0, 0, 1), "rear right"),
                                        ((1, 0.8, -0.5), (0, 0, 1), "front left, below"), ((0, 0, 1), (1, 0, 0), "above"))]
        _save(np.vstack([np.hstack(shots[:2]), np.hstack(shots[2:])]), path)
    finally:
        if xv: xv.terminate()

if __name__ == "__main__":
    sys.path.insert(0, HERE)
    import build as B
    if "--near" in sys.argv:
        i = sys.argv.index("--near"); x, y, z, r = (float(v) for v in sys.argv[i + 1:i + 5])
        out = sys.argv[i + 5] if len(sys.argv) > i + 5 else os.path.join(HERE, "views", "near.png")
        near(B, (x, y, z), r, out); print("wrote", out)
    else:
        print("wrote", render(B, os.path.join(HERE, "views")))
