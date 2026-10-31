"""The robot for AdvantageScope, straight from the Onshape CAD: Robot_BIOBUZZ/model.glb and config.json.

    pip install numpy trimesh fast_simplification
    ONSHAPE_ACCESS_KEY=... ONSHAPE_SECRET_KEY=... python3 cad/advantagescope/from_onshape.py

It reads the Robot assembly (read-only: GET requests only, nothing in the document changes) and draws what Onshape
shows: hidden or suppressed instances are left out, and so is anything onshape_model.json's "drop" list names. So the
mentor's document stays as it is, and the cleanup is a list in git. Every run compares the assembly with the last
build (onshape_source.json) and prints what was added, removed, hidden or shown since, so a change in the CAD is
something someone decides about, not something that slips into the model unseen.

Parts are fetched once and cached (~/.cache/biobuzz-onshape): goBILDA's library parts never change, and the
document's own parts are fetched again only when the document has changed.

The frame (AdvantageScope's, as TeamCode's logs use it): metres, +X forward, +Y left, +Z up, the origin on the floor
under the middle of the four drive axles, the point Pedro tracks. It comes from the four Wheel Assembly shafts each
run, so the model follows the chassis if it moves in the assembly.
"""
import base64, hashlib, json, os, re, sys, time, urllib.error, urllib.parse, urllib.request
from concurrent.futures import ThreadPoolExecutor

import numpy as np
import trimesh

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, "Robot_BIOBUZZ")
RULES = os.path.join(HERE, "onshape_model.json")
STATE = os.path.join(HERE, "onshape_source.json")
CACHE = os.path.join(os.path.expanduser("~"), ".cache", "biobuzz-onshape")
API = "https://cad.onshape.com/api/v10"
IN = 0.0254
WHEEL_R = 0.048            # goBILDA 96 mm mecanum: the wheels aren't in the CAD, only their hubs and shafts


def get(path):
    """One read-only API call, with the keys from the environment (Basic auth)."""
    key, secret = os.environ["ONSHAPE_ACCESS_KEY"], os.environ["ONSHAPE_SECRET_KEY"]
    req = urllib.request.Request(API + path, headers={
        "Accept": "application/json;charset=UTF-8; qs=0.09",
        "Authorization": "Basic " + base64.b64encode(f"{key}:{secret}".encode()).decode()})
    for attempt in range(4):
        try:
            with urllib.request.urlopen(req, timeout=120) as r:
                return json.loads(r.read())
        except urllib.error.HTTPError as e:
            if e.code == 429 or e.code >= 500:
                time.sleep(2 ** attempt + 1)
                continue
            raise
        except (urllib.error.URLError, TimeoutError):   # a dropped connection or a timeout: try again
            time.sleep(2 ** attempt + 1)
    raise RuntimeError(f"Onshape kept refusing {path}")


def visible_parts(asm, drop):
    """Every part occurrence Onshape draws, as (path names, instance, 4x4 transform in metres).
    An occurrence is hidden when it or anything above it is hidden; suppressed instances have no occurrence."""
    root = asm["rootAssembly"]
    subs = {}
    for s in asm["subAssemblies"]:
        subs.setdefault(s["elementId"], []).append(s)
    hidden = {tuple(o["path"]) for o in root["occurrences"] if o.get("hidden")}
    out, dropped = [], set()
    for o in root["occurrences"]:
        p = o["path"]
        if any(tuple(p[:k]) in hidden for k in range(1, len(p) + 1)):
            continue
        insts, names, inst = root["instances"], [], None
        for pid in p:
            inst = next((i for i in insts if i["id"] == pid), None)
            if inst is None or inst.get("suppressed") or "type" not in inst:   # a broken reference has no type
                inst = None
                break
            names.append(inst["name"])
            if inst["type"] == "Assembly":
                cands = subs.get(inst["elementId"], [])
                same = [s for s in cands if s.get("configuration", "") == inst.get("configuration", "")]
                insts = (same or cands or [{"instances": []}])[0]["instances"]
        if inst is None or inst["type"] != "Part":
            continue
        path = " / ".join(names)
        rule = next((d for d in drop if re.search(d, path)), None)
        if rule:
            dropped.add(path)
            continue
        out.append((path, inst, np.array(o["transform"]).reshape(4, 4)))
    return out, dropped


def part_key(inst):
    wvm = ("v", inst["documentVersion"]) if inst.get("documentVersion") else ("m", inst["documentMicroversion"])
    return (inst["documentId"], wvm, inst["elementId"], inst["partId"], inst.get("configuration", ""))


LINK_DOC = None             # the robot's document: library parts are read through its links to them


def tessellation(key):
    """The part's triangles (n, 3, 3) in its own frame (metres) and its colour, from the cache or the API.
    A part the keys can't read comes back as None, so main() can stop before writing anything."""
    did, (t, v), eid, pid, cfg = key
    os.makedirs(CACHE, exist_ok=True)
    f = os.path.join(CACHE, hashlib.sha1(repr(key).encode()).hexdigest() + ".npz")
    if os.path.exists(f):
        try:
            z = np.load(f)
            return z["tris"], tuple(z["rgb"])
        except Exception:                        # a damaged cache entry: fetch the part again
            os.remove(f)
    q = {"partId": pid, "outputFaceAppearances": "false", "outputVertexNormals": "false",
         "outputFacetNormals": "false", "chordTolerance": "0.001", "angleTolerance": "0.35"}
    if cfg:
        q["configuration"] = cfg
    if LINK_DOC and did != LINK_DOC:
        q["linkDocumentId"] = LINK_DOC
    try:
        d = get(f"/partstudios/d/{did}/{t}/{v}/e/{eid}/tessellatedfaces?" + urllib.parse.urlencode(q))
    except urllib.error.HTTPError as e:
        print(f"  part {pid} in document {did} not readable ({e.code})", file=sys.stderr)
        return None, None
    tris, rgb = [], (0.7, 0.72, 0.76)
    for b in d.get("bodies") or []:
        c = (b.get("appearance") or {}).get("color")
        if c:
            rgb = tuple(x / 255 for x in base64.b64decode(c)[:3])
        for face in b["faces"]:
            for fc in face["facets"]:
                tris.append([[p["x"], p["y"], p["z"]] for p in fc["vertices"]])
    tris = np.array(tris, np.float32).reshape(-1, 3, 3)
    tmp = f + ".part"                            # written whole, then renamed: a cut-off write never looks cached
    with open(tmp, "wb") as fh:
        np.savez_compressed(fh, tris=tris, rgb=np.array(rgb))
    os.replace(tmp, f)
    return tris, rgb


def robot_frame(parts, meshes):
    """The assembly-to-robot transform, from the four drive axles (the Wheel Assembly shafts).
    In the mentor's assembly the axles run along x, forward is +y and up is -z. Checked here, so a re-oriented
    assembly fails loudly instead of drawing the robot on its side."""
    centres = []
    for path, inst, T in parts:
        if re.search(r"Wheel Assembly <\d+> / .*Shaft", path):
            p = meshes[part_key(inst)][0].reshape(-1, 3) @ T[:3, :3].T + T[:3, 3]
            ext = p.max(0) - p.min(0)
            if not ext.argmax() == 0:
                sys.exit(f"{path}: a drive axle that isn't along the assembly's x; the frame here needs updating")
            centres.append((p.min(0) + p.max(0)) / 2)
    if len(centres) != 4:
        sys.exit(f"found {len(centres)} Wheel Assembly shafts, expected 4")
    c = np.mean(centres, 0)
    floor_z = c[2] + WHEEL_R                                  # up is -z: the floor is below the axles at +z
    R = np.array([[0, 1, 0], [1, 0, 0], [0, 0, -1]], float)    # robot (fwd, left, up) = asm (y, x, -z)
    origin = np.array([c[0], c[1], floor_z])
    return lambda p: (p - origin) @ R.T


def simplify(tris, keep):
    """Fewer triangles for one part: merge its vertices, then decimate to about `keep` of them."""
    m = trimesh.Trimesh(tris.reshape(-1, 3), np.arange(len(tris) * 3).reshape(-1, 3), process=False)
    m.merge_vertices(digits_vertex=5)
    v, f = m.vertices, m.faces
    if keep < len(f):
        try:
            import fast_simplification
            v, f = fast_simplification.simplify(v.astype(np.float32), f.astype(np.int32),
                                                target_reduction=1 - keep / len(f))
        except Exception as e:
            print(f"  warning: simplification failed ({e!r}); keeping {len(f)} triangles", file=sys.stderr)
    if keep < len(f) / 1.5:
        # Composite parts (goBILDA's mecanum wheels and motors: many small bodies) barely decimate, so fall back to
        # clustering: snap vertices to a grid, coarser until the part fits, and drop the triangles that collapse.
        v, f = cluster(np.asarray(m.vertices), np.asarray(m.faces), keep)
    return v, f


def cluster(v, f, keep):
    """Vertex clustering to about `keep` triangles: crude, but it reduces anything."""
    h = np.linalg.norm(np.ptp(v, axis=0)) / 400
    for _ in range(16):
        cells, inv = np.unique(np.floor(v / h), axis=0, return_inverse=True)
        inv = inv.reshape(-1)
        nv = np.zeros((len(cells), 3))
        np.add.at(nv, inv, v)
        nv /= np.bincount(inv, minlength=len(cells))[:, None]
        nf = inv[f]
        nf = nf[(nf[:, 0] != nf[:, 1]) & (nf[:, 1] != nf[:, 2]) & (nf[:, 0] != nf[:, 2])]
        _, first = np.unique(np.sort(nf, axis=1), axis=0, return_index=True)
        nf = nf[np.sort(first)]
        if len(nf) <= keep * 1.25:
            break
        h *= 1.4
    return nv, nf


def axis_of(points, kind):
    """A part's axis: the line through its bounding box's centre along its thinnest direction (a bearing race, a
    plate) or its longest (a shaft). Pointed up if it is near vertical, else along +x, +y, so that a positive angle
    is counter-clockwise looking down the axis toward the origin, as TeamCode's angles are."""
    centre = (points.min(0) + points.max(0)) / 2
    _, vecs = np.linalg.eigh(np.cov((points - points.mean(0)).T))
    d = vecs[:, 0] if kind == "thinnest" else vecs[:, 2]
    if d[np.argmax(np.abs(d))] < 0:
        d = -d
    return centre, d / np.linalg.norm(d)


def mesh_of(tris, rgb, T, keep, to_robot):
    v, f = simplify(tris, keep)
    v = to_robot(np.asarray(v) @ T[:3, :3].T + T[:3, 3])
    mesh = trimesh.Trimesh(v, f, process=False)
    mesh.visual = trimesh.visual.TextureVisuals(material=trimesh.visual.material.PBRMaterial(
        baseColorFactor=[int(255 * x) for x in rgb] + [255], metallicFactor=0.2, roughnessFactor=0.6,
        doubleSided=True))
    return mesh


def main():
    global LINK_DOC
    rules = json.load(open(RULES))
    D, W, E = rules["document"], rules["workspace"], rules["element"]
    LINK_DOC = D
    meta = get(f"/documents/{D}")
    mv = get(f"/documents/d/{D}/w/{W}/currentmicroversion")["microversion"]
    asm = get(f"/assemblies/d/{D}/m/{mv}/e/{E}?includeMateFeatures=false&includeNonSolids=true")   # the version recorded; NonSolids brings composite parts (goBILDA wheels)
    parts, dropped = visible_parts(asm, rules["drop"])

    # What changed in the CAD since the last build, by top-level instance
    tops = {}
    for path, _, _ in parts:
        top = path.split(" / ")[0]
        tops[top] = tops.get(top, 0) + 1
    old = json.load(open(STATE)) if os.path.exists(STATE) else {}
    before = old.get("visible", {})
    added = sorted(set(tops) - set(before))
    gone = sorted(set(before) - set(tops))
    changed = sorted(k for k in tops if k in before and tops[k] != before[k])
    print(f"{meta['name']}: workspace microversion {mv} (last build: {old.get('microversion', 'none')})")
    for label, items in (("new or shown", added), ("removed or hidden", gone), ("parts count changed", changed)):
        for k in items:
            print(f"  {label}: {k}" + (f" ({before.get(k)} -> {tops.get(k)} parts)" if label.startswith("parts") else ""))
    if old and not (added or gone or changed):
        print("  the visible top-level instances are as last time")

    keys = sorted({part_key(inst) for _, inst, _ in parts})
    with ThreadPoolExecutor(6) as ex:
        meshes = dict(zip(keys, ex.map(tessellation, keys)))
    unreadable = sorted(p for p, inst, _ in parts if meshes[part_key(inst)][0] is None)
    if unreadable:
        sys.exit(f"{len(unreadable)} visible part(s) could not be read; nothing written:\n  " + "\n  ".join(unreadable))
    to_robot = robot_frame(parts, meshes)

    # The moving components: each part goes to the first component whose patterns match it, else the fixed model
    comps = rules.get("components", [])
    which = [next((c for c, comp in enumerate(comps) if any(re.search(r, path) for r in comp["parts"])), None)
             for path, _, _ in parts]
    for c, comp in enumerate(comps):
        if c not in which:
            sys.exit(f"component {c} ({comp['name']}) matches no visible part: check its patterns against the CAD")

    total = sum(len(meshes[part_key(i)][0]) for _, i, _ in parts)
    ratio = min(1.0, rules["triangle_budget"] / max(total, 1))
    scenes = [trimesh.Scene() for _ in range(len(comps) + 1)]          # [fixed, component 0, component 1, ...]
    for n, ((path, inst, T), c) in enumerate(zip(parts, which)):
        tris, rgb = meshes[part_key(inst)]
        if not len(tris):
            continue
        keep = max(min(len(tris), 300), int(len(tris) * ratio))
        scenes[0 if c is None else c + 1].add_geometry(mesh_of(tris, rgb, T, keep, to_robot),
                                                       geom_name=f"p{n}", node_name=path[:120])

    described = []
    for c, comp in enumerate(comps):
        pts = [to_robot(meshes[part_key(inst)][0].reshape(-1, 3) @ T[:3, :3].T + T[:3, 3])
               for (path, inst, T), w in zip(parts, which) if w == c and re.search(comp["axis_part"], path)]
        if len(pts) != 1:
            sys.exit(f"component {c} ({comp['name']}): axis_part matches {len(pts)} of its parts, expected 1")
        point, direction = axis_of(pts[0], comp["axis"])
        # The model file is always drawn at model_at_deg, whatever angle the CAD was left at (drawn_at_deg): a log
        # stores poses relative to the model file, so a model drawn at another angle would show every older log's
        # part turned by the difference.
        model_at = comp.get("model_at_deg", comp["drawn_at_deg"])
        turn = np.radians(model_at - comp["drawn_at_deg"])
        if turn:
            scenes[c + 1].apply_transform(trimesh.transformations.rotation_matrix(turn, direction, point))
        described.append({"index": c, "name": comp["name"], "angle": comp["angle"],
                          "axis_point_m": [round(float(x), 5) for x in point],
                          "axis_direction": [round(float(x), 5) for x in direction],
                          "drawn_at_deg": model_at})
        print(f"  component {c} ({comp['name']}): {which.count(c)} parts, turns about "
              f"({', '.join(f'{x / IN:.2f}' for x in point)}) in along ({', '.join(f'{x:.3f}' for x in direction)})")

    os.makedirs(OUT, exist_ok=True)
    # AdvantageScope's loader drops any mesh without normals, so without include_normals nothing draws.
    scenes[0].export(os.path.join(OUT, "model.glb"), include_normals=True)
    for c in range(len(comps)):
        scenes[c + 1].export(os.path.join(OUT, f"model_{c}.glb"), include_normals=True)
    for old_file in os.listdir(OUT):                                   # a component that went from the list
        m = re.fullmatch(r"model_(\d+)\.glb", old_file)
        if m and int(m.group(1)) >= len(comps):
            os.remove(os.path.join(OUT, old_file))
    with open(os.path.join(OUT, "components.json.tmp"), "w") as fh:
        json.dump({"about": "Robot_BIOBUZZ's components, in config.json's order. Frame: metres, +X forward, +Y left, "
                            "+Z up, origin on the floor under the middle of the drive axles. Component c is drawn in "
                            "model_c.glb as the CAD has it, at drawn_at_deg. To show it at angle a (degrees, the "
                            "'angle' named here), rotate it by t = a - drawn_at_deg about axis_direction through "
                            "axis_point_m: the AdvantageScope pose is translation p - R(t) p, rotation quaternion "
                            "(cos t/2, sin t/2 * axis_direction).",
                   "components": described}, fh, indent=1)
    os.replace(os.path.join(OUT, "components.json.tmp"), os.path.join(OUT, "components.json"))
    scene = scenes[0]

    cfg_path = os.path.join(OUT, "config.json")
    cfg = json.load(open(cfg_path)) if os.path.exists(cfg_path) else {}
    cfg.update({"name": "BIOBUZZ Robot", "isFTC": True, "disableSimplification": True, "rotations": [],
                "position": [0, 0, 0],
                "components": [{"zeroedRotations": [], "zeroedPosition": [0, 0, 0]} for _ in comps]})
    with open(cfg_path + ".tmp", "w") as fh:
        json.dump(cfg, fh, indent=2)
    os.replace(cfg_path + ".tmp", cfg_path)

    faces = sum(len(g.faces) for sc in scenes for g in sc.geometry.values())
    lo = np.min([sc.bounds[0] for sc in scenes if sc.geometry], 0)
    hi = np.max([sc.bounds[1] for sc in scenes if sc.geometry], 0)
    json.dump({"document": meta["name"], "microversion": mv, "built": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
               "visible": dict(sorted(tops.items())), "dropped": sorted(dropped)}, open(STATE, "w"), indent=1)
    size = sum(os.path.getsize(os.path.join(OUT, f)) for f in os.listdir(OUT) if f.endswith(".glb"))
    print(f"model.glb and {len(comps)} component(s): {len(parts)} parts, {faces} triangles (from {total}), "
          f"{size / 1e6:.1f} MB")
    print(f"  extent in inches: X {lo[0] / IN:.2f}..{hi[0] / IN:.2f}  Y {lo[1] / IN:.2f}..{hi[1] / IN:.2f}  "
          f"Z {lo[2] / IN:.2f}..{hi[2] / IN:.2f}")
    if dropped:
        print(f"  left out by onshape_model.json: {len(dropped)} parts")


if __name__ == "__main__":
    main()
