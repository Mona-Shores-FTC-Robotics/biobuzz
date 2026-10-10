"""The turret encoder kit as an add-on for the mentor's robot as he drew it: the servo drive and the two absolute
encoders from cad/transfer (its turret_* parts and their screws), moved back by cad/transfer's LAUNCHER_SHIFT, since his
launcher is not moved forward as ours is. The STEP is in his own Robot.step's frame (mm), so it drops straight into his
Onshape document at the origin: our robot CAD frame less the offset that lines his 9 Oct file up by its intake's front
uprights (cad/full-robot placed_team prints it: +0.27 mm across, +35.04 mm forward).

    python3 cad/modules/turret-kit/build.py      # writes turret-kit.step and stl/ (the printed parts) next to this file

tools/robot-cad/module_check.py turret checks it against his Robot.step.
"""
import os, re, sys, importlib.util
import cadquery as cq
HERE = os.path.dirname(os.path.abspath(__file__))
spec = importlib.util.spec_from_file_location("transfer", os.path.join(HERE, "..", "..", "transfer", "build.py"))
TR = importlib.util.module_from_spec(spec); spec.loader.exec_module(TR)
KIT = re.compile(r"^(turret_|screw_turret_|nut_turret_)")
HIS_ALIGN_MM = (0.27, 35.04)                      # (across, forward): his 9 Oct Robot.step -> our robot CAD frame
def his_frame(wp):
    return TR.to_cad(wp.translate((-TR.LAUNCHER_SHIFT, 0, 0))).translate(cq.Vector(-HIS_ALIGN_MM[0], 0, -HIS_ALIGN_MM[1]))
parts = {n: (his_frame(wp), col, kind) for n, (wp, col, kind) in TR.launcher.items() if KIT.search(n)}
if __name__ == "__main__":
    os.makedirs(os.path.join(HERE, "stl"), exist_ok=True)
    assy = cq.Assembly(name="turret encoder kit (on the mentor's robot)")
    for n, (wp, col, kind) in parts.items():
        assy.add(wp, name=n, color=cq.Color(*col))
        if kind == "print": wp.exportStl(os.path.join(HERE, "stl", n.split(" ")[0] + ".stl"), 0.05, 0.2)   # mm
    assy.save(os.path.join(HERE, "turret-kit.step"))
    print(len(parts), "parts;", sum(k == "print" for _, _, k in parts.values()), "printed")
