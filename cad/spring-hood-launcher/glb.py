"""Minimal GLB writer: one mesh and node per part, coloured, tessellated at a chosen tolerance."""
import json, struct
import numpy as np

def cluster(v, t, cell):
    """Vertex-clustering decimation: snap to a grid of `cell` mm, merge, drop collapsed triangles."""
    if cell <= 0:
        return v, t
    keys = np.floor(v / cell).astype(np.int64)
    uniq, inv = np.unique(keys, axis=0, return_inverse=True)
    inv = inv.reshape(-1)
    sums = np.zeros((len(uniq), 3)); np.add.at(sums, inv, v)
    cnt = np.bincount(inv, minlength=len(uniq))[:, None]
    nv = (sums / cnt).astype(np.float32)
    nt = inv[t]
    keep = (nt[:, 0] != nt[:, 1]) & (nt[:, 1] != nt[:, 2]) & (nt[:, 0] != nt[:, 2])
    nt = nt[keep]
    nt = np.unique(np.sort(nt, axis=1), axis=0, return_index=True)[1]
    nt = inv[t][keep][np.sort(nt)]
    return nv, nt.astype(np.uint32)

def write_glb(path, parts, tolerance=0.25, angular=0.35, cell=0.0):
    """parts: [(name, cadquery Workplane or Shape, (r, g, b))] or 4-tuples with a per-part cell size.
    Units stay millimetres; the viewer knows."""
    bin_ = bytearray()
    accessors, views, meshes, nodes, materials = [], [], [], [], []
    mat_index = {}
    def view(data, target):
        while len(bin_) % 4:
            bin_.append(0)
        views.append({"buffer": 0, "byteOffset": len(bin_), "byteLength": len(data), "target": target})
        bin_.extend(data)
        return len(views) - 1
    for part in parts:
        name, w, col = part[:3]
        c = part[3] if len(part) > 3 else cell
        shape = w.val() if hasattr(w, "val") else w
        verts, tris = shape.tessellate(tolerance, angular)
        if not tris:
            continue
        v = np.array([(p.x, p.y, p.z) for p in verts], dtype=np.float64)
        t = np.array(tris, dtype=np.int64)
        v, t = cluster(v, t, c)
        if not len(t):
            continue
        used = np.unique(t)
        remap = np.full(len(v), -1, dtype=np.int64); remap[used] = np.arange(len(used))
        v = v[used].astype(np.float32); t = remap[t].astype(np.uint32)
        pos = v.tobytes(); idx = t.tobytes()
        tris = t
        accessors.append({"bufferView": view(pos, 34962), "componentType": 5126, "count": len(v), "type": "VEC3",
                          "min": v.min(axis=0).tolist(), "max": v.max(axis=0).tolist()})
        pa = len(accessors) - 1
        accessors.append({"bufferView": view(idx, 34963), "componentType": 5125, "count": 3 * len(tris), "type": "SCALAR"})
        ia = len(accessors) - 1
        key = tuple(round(c, 3) for c in col)
        if key not in mat_index:
            mat_index[key] = len(materials)
            materials.append({"pbrMetallicRoughness": {"baseColorFactor": [*key, 1.0], "metallicFactor": 0.1, "roughnessFactor": 0.6},
                              "doubleSided": True})
        meshes.append({"name": name, "primitives": [{"attributes": {"POSITION": pa}, "indices": ia, "material": mat_index[key]}]})
        nodes.append({"name": name, "mesh": len(meshes) - 1, "extras": {"part": name}})
    while len(bin_) % 4:
        bin_.append(0)
    doc = {"asset": {"version": "2.0", "generator": "spring-hood-launcher"}, "scene": 0,
           "scenes": [{"nodes": list(range(len(nodes)))}], "nodes": nodes, "meshes": meshes, "materials": materials,
           "accessors": accessors, "bufferViews": views, "buffers": [{"byteLength": len(bin_)}]}
    js = json.dumps(doc, separators=(",", ":")).encode()
    while len(js) % 4:
        js += b" "
    with open(path, "wb") as f:
        f.write(struct.pack("<III", 0x46546C67, 2, 12 + 8 + len(js) + 8 + len(bin_)))
        f.write(struct.pack("<II", len(js), 0x4E4F534A)); f.write(js)
        f.write(struct.pack("<II", len(bin_), 0x004E4942)); f.write(bin_)
