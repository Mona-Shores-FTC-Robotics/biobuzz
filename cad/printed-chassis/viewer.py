"""The robot as a light glTF (GLB) for a web viewer: every bought part as its real model, decimated where it's heavy,
each model meshed once and placed per copy; parts grouped by subsystem and named, each carrying what it is.

    python3 cad/printed-chassis/viewer.py        # .cache/viewer/: robot.glb, its base64, parts.json and the page (not in git)
    python3 -m http.server -d cad/printed-chassis/.cache/viewer   # then open http://localhost:8000

The GLB is in metres, Y up (glTF's frame); the robot's +X forward is the viewer's -Z. Positions are 16-bit
(KHR_mesh_quantization: each mesh's node scales them back, the same step on every axis). No normals: its vertices are
split at creases, so the viewer computes them (three.js computeVertexNormals). The page gets it gzipped, as base64.
"""
import json, math, os, struct, sys
import numpy as np
HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, ".cache", "viewer")
MAX_TRIS = 5000                         # per unique mesh, after decimation
METAL = ("aluminium", "steel")

TESS = ((0.01, 0.35), (0.02, 0.5), (0.04, 0.8), (0.08, 1.2))   # finer to coarser (linear in, angular rad)
def _mesh(sh):
    """Tessellated as finely as MAX_TRIS allows: a coarser tessellation keeps a perforated channel's holes whole, where
    decimating a fine one tears them into slivers. Only what's still over 3 x MAX_TRIS at the coarsest is decimated."""
    for tol, ang in TESS:
        v, f = sh.tessellate(tol, ang)
        if len(f) <= MAX_TRIS: break
    return np.array([(q.x, q.y, q.z) for q in v], np.float32), np.array(f, np.int64).reshape(-1, 3)

def _decimate_and_normals(v, f):
    import vtk
    from vtk.util.numpy_support import numpy_to_vtk, numpy_to_vtkIdTypeArray, vtk_to_numpy
    pts = vtk.vtkPoints(); pts.SetData(numpy_to_vtk(np.ascontiguousarray(v, np.float64), deep=True))
    cells = vtk.vtkCellArray(); cells.SetCells(len(f), numpy_to_vtkIdTypeArray(np.hstack([np.full((len(f), 1), 3), f]).ravel().astype(np.int64), deep=True))
    pd = vtk.vtkPolyData(); pd.SetPoints(pts); pd.SetPolys(cells)
    cl = vtk.vtkCleanPolyData(); cl.SetInputData(pd); cl.Update(); pd = cl.GetOutput()
    if pd.GetNumberOfPolys() > 3 * MAX_TRIS:
        d = vtk.vtkQuadricDecimation(); d.SetInputData(pd); d.SetTargetReduction(1 - 3 * MAX_TRIS / pd.GetNumberOfPolys()); d.VolumePreservationOn(); d.Update(); pd = d.GetOutput()
    n = vtk.vtkPolyDataNormals(); n.SetInputData(pd); n.SetFeatureAngle(35); n.SplittingOn(); n.ConsistencyOn(); n.AutoOrientNormalsOff(); n.Update(); pd = n.GetOutput()
    V = vtk_to_numpy(pd.GetPoints().GetData()).astype(np.float32)
    N = vtk_to_numpy(pd.GetPointData().GetNormals()).astype(np.float32)
    F = vtk_to_numpy(pd.GetPolys().GetConnectivityArray()).reshape(-1, 3)
    return V, N, F

def _matrix(loc):
    t = loc.wrapped.Transformation()
    M = np.eye(4)
    for i in range(3):
        for j in range(4): M[i, j] = t.Value(i + 1, j + 1)
    return M

def build(B):
    import cadquery as cq
    meshes, mesh_of, materials, mat_of = [], {}, [], {}
    def material(c, metal):
        key = (tuple(round(x, 3) for x in c), metal)
        if key not in mat_of:
            mat_of[key] = len(materials)
            materials.append({"pbrMetallicRoughness": {"baseColorFactor": [*(x ** 2.2 for x in key[0]), 1.0],   # glTF's colours are linear "metallicFactor": 0.6 if metal else 0.0,
                                                       "roughnessFactor": 0.45 if metal else 0.7}, "doubleSided": True})
        return mat_of[key]
    keep = []                                   # every shape meshed stays alive: the cache's key is its id(), which Python
    def mesh(sh, c, metal):                     # reuses once an object is freed (a screw's solid is made fresh, then dropped)
        key = (id(sh), tuple(c))
        if key not in mesh_of:
            keep.append(sh)
            V, N, F = _decimate_and_normals(*_mesh(sh))
            mesh_of[key] = len(meshes); meshes.append((V, N, F, material(c, metal)))
        return mesh_of[key]
    tree, parts = {}, []
    for k, p in B.PARTS.items():
        sysname = B.subsystem(k)
        metal = any(w in p["what"] for w in ("goBILDA", "steel", "aluminium")) and not any(w in p["what"] for w in ("Gecko", "servo", "Yellow Jacket", "mecanum", "belt"))
        lp = B.look_parts(k) if p.get("looks") else None
        items = [(sh, _matrix(loc), c, metal and c[0] > 0.5) for sh, loc, c in lp] if lp else [(B.solid(p["shape"]), np.eye(4), B.colour_of(k), metal)]
        node = {"name": k, "inst": [(mesh(sh, c, m), M) for sh, M, c, m in items]}
        tree.setdefault(sysname, []).append(node)
        parts.append({"key": k, "subsystem": sysname, "kind": p["kind"], "what": p["what"], "moves": B.motion(k)})
    for f in B.FASTENERS:                       # the screws, one mesh each (small)
        sysname = B.subsystem(f["a"]) if f["a"] in B.PARTS else "14 Other"
        sh = B.solid(B.screw_solids(f))
        tree.setdefault(sysname, []).append({"name": f"screw {f['size']}x{f['L']} {f['name']}", "inst": [(mesh(sh, (0.12, 0.12, 0.13), True), np.eye(4))]})
    return meshes, materials, tree, parts

def write_glb(meshes, materials, tree, path):
    S = 0.0254                                  # inches to metres
    blob, views, accessors, gmeshes = bytearray(), [], [], []
    def add(arr, target, comp, typ, mm=None, stride=None):
        while len(blob) % 4: blob.append(0)
        off = len(blob); b = arr.tobytes(); blob.extend(b)
        views.append({"buffer": 0, "byteOffset": off, "byteLength": len(b), **({"target": target} if target else {}), **({"byteStride": stride} if stride else {})})
        a = {"bufferView": len(views) - 1, "componentType": comp, "count": int(arr.shape[0]), "type": typ}
        if mm is not None: a["min"], a["max"] = mm
        accessors.append(a); return len(accessors) - 1
    deq = []                                    # per mesh: the matrix that turns its 16-bit positions back into metres
    for V, N, F, mat in meshes:
        Vm = V.astype(np.float64) * S; mn = Vm.min(0); step = max(float((Vm.max(0) - mn).max()), 1e-6) / 65534
        q = np.clip(np.round((Vm - mn) / step) - 32767, -32767, 32767).astype(np.int16)   # one step on every axis: normals stay true
        qp = np.zeros((len(q), 4), np.int16); qp[:, :3] = q                                # padded to 8 bytes a vertex
        pv = add(qp, 34962, 5122, "VEC3", (q.min(0).tolist(), q.max(0).tolist()), stride=8)
        accessors[pv]["count"] = int(len(q))
        deq.append([step, 0, 0, 0, 0, step, 0, 0, 0, 0, step, 0, *(mn + 32767 * step).tolist(), 1])
        idx = F.astype(np.uint16 if len(V) < 65536 else np.uint32).ravel()
        iv = add(idx.reshape(-1, 1), 34963, 5123 if idx.dtype == np.uint16 else 5125, "SCALAR")
        gmeshes.append({"primitives": [{"attributes": {"POSITION": pv}, "indices": iv, "material": mat}]})
    nodes = [{"name": "robot", "rotation": [-math.sqrt(0.5), 0, 0, math.sqrt(0.5)], "children": []}]   # Z up to Y up
    for sysname in sorted(tree):
        si = len(nodes); nodes.append({"name": sysname, "children": []}); nodes[0]["children"].append(si)
        for part in tree[sysname]:
            pi = len(nodes); nodes.append({"name": part["name"], "children": []}); nodes[si]["children"].append(pi)
            for m, M in part["inst"]:
                M2 = M.copy(); M2[:3, 3] *= S
                n = {"children": [len(nodes) + 1]}
                if not np.allclose(M2, np.eye(4)): n["matrix"] = M2.T.ravel().tolist()
                nodes[pi]["children"].append(len(nodes)); nodes.append(n); nodes.append({"mesh": m, "matrix": deq[m]})
    gltf = {"asset": {"version": "2.0", "generator": "biobuzz cad/printed-chassis/viewer.py"}, "scene": 0, "scenes": [{"nodes": [0]}],
            "extensionsUsed": ["KHR_mesh_quantization"], "extensionsRequired": ["KHR_mesh_quantization"],
            "nodes": nodes, "meshes": gmeshes, "materials": materials, "accessors": accessors, "bufferViews": views, "buffers": [{"byteLength": len(blob)}]}
    js = json.dumps(gltf, separators=(",", ":")).encode()
    while len(js) % 4: js += b" "
    while len(blob) % 4: blob.append(0)
    with open(path, "wb") as f:
        f.write(struct.pack("<III", 0x46546C67, 2, 12 + 8 + len(js) + 8 + len(blob)))
        f.write(struct.pack("<II", len(js), 0x4E4F534A)); f.write(js)
        f.write(struct.pack("<II", len(blob), 0x004E4942)); f.write(bytes(blob))

if __name__ == "__main__":
    sys.path.insert(0, HERE)
    import build as B
    B.cut_holes(); B.cut_bores(); B.finish_edges()
    meshes, materials, tree, parts = build(B)
    os.makedirs(OUT, exist_ok=True)
    write_glb(meshes, materials, tree, os.path.join(OUT, "robot.glb"))
    json.dump(parts, open(os.path.join(OUT, "parts.json"), "w"), indent=0)
    import base64, gzip, shutil                 # the page loads the GLB gzipped, as base64 text (artifact hosts serve text, not .glb)
    open(os.path.join(OUT, "robot.glb.gz.txt"), "w").write(base64.b64encode(gzip.compress(open(os.path.join(OUT, "robot.glb"), "rb").read(), 9)).decode())
    shutil.copy(os.path.join(HERE, "viewer", "index.html"), OUT)
    print(f"{len(meshes)} meshes, {sum(len(m[2]) for m in meshes)} triangles, {os.path.getsize(os.path.join(OUT, 'robot.glb')) / 1e6:.1f} MB")
