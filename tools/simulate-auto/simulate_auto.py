#!/usr/bin/env python3
"""Run the existing Java simulator on saved checkout files, without pushing to GitHub."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import sys
import uuid

ROOT = Path(__file__).resolve().parents[2]
AUTOS = ROOT / "TeamCode/autos"
GENERATED = ROOT / "TeamCode/src/main/java/org/firstinspires/ftc/teamcode/opmodes/auto/generated"


def auto_source(name, partner=False):
    name = name.removesuffix(".pp") + ".pp"
    if partner and not name.startswith("partners/"):
        name = "partners/" + name
    path = (AUTOS / name).resolve()
    if not path.is_relative_to(AUTOS) or not path.is_file():
        raise ValueError(f"No saved auto at TeamCode/autos/{name}")
    matches = []
    for java in GENERATED.glob("*.java"):
        source = re.search(r'\bSOURCE\s*=\s*"([^"]+)"', java.read_text(encoding="utf-8"))
        if source and source[1] == path.name:
            matches.append(java)
    if len(matches) != 1:
        raise ValueError(f"Save {name} from Visualizer first; expected one matching generated Java class, found {len(matches)}.")
    return path, matches[0]


def git(*args):
    try:
        result = subprocess.run(["git", "-C", str(ROOT), *args], capture_output=True, text=True)
    except OSError:
        return "unknown"
    return result.stdout.strip() if result.returncode == 0 else "unknown"


def fingerprint():
    """Detect edits while Gradle builds/runs so a result cannot claim newer inputs."""
    paths = list(AUTOS.rglob("*.pp")) + list(AUTOS.glob("*.json"))
    paths += list((ROOT / "TeamCode/src").rglob("*.java"))
    digest = hashlib.sha256()
    for path in sorted(paths):
        digest.update(str(path.relative_to(ROOT)).encode())
        digest.update(path.read_bytes())
    return digest.hexdigest()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("auto", help="Saved auto name, e.g. r-solo or drafts/r-solo-push")
    parser.add_argument("--partner", help="One saved partner, e.g. partner-still")
    parser.add_argument("--last-shot", type=float, help="Partner's last preload shot in match seconds")
    parser.add_argument("--study", action="store_true", help="Use this auto's curated partner matrix")
    parser.add_argument("--seeds", type=int, default=1, help="Seeds per scenario (default: one quick run)")
    parser.add_argument("--prepare-only", action="store_true", help="Write the request without running Gradle")
    args = parser.parse_args()
    if args.seeds < 1 or args.seeds > 100:
        parser.error("--seeds must be between 1 and 100")
    if args.study and args.partner:
        parser.error("Choose --partner or --study")
    if args.last_shot is not None and (not args.partner or not 0 <= args.last_shot <= 30):
        parser.error("--last-shot needs --partner and a time between 0 and 30 seconds")
    if (ROOT / "TeamCode/.auto-workspace/pending.json").exists():
        raise ValueError("Visualizer has an interrupted save. Reopen its workspace to recover before simulating.")

    pp, java = auto_source(args.auto)
    request_path = ROOT / "TeamCode/sim-requests" / (pp.stem + ".json")
    request = json.loads(request_path.read_text(encoding="utf-8")) if request_path.exists() else {}
    old_profile = str(request.get("spec", "")).partition("@")[2]
    request["spec"] = java.stem + ("@" + old_profile if old_profile else "")
    request["pp"] = pp.relative_to(ROOT).as_posix()
    curated = request.get("partners", [])
    request["partners"] = curated if args.study else []
    if args.partner:
        _, partner = auto_source(args.partner, partner=True)
        entry = next((dict(p) for p in curated if p.get("partner") == partner.stem), {})
        entry.update(name=args.partner, partner=partner.stem)
        if args.last_shot is not None:
            entry["lastShotS"] = args.last_shot
        request["partners"] = [entry]
    request["seeds"] = list(range(1, args.seeds + 1))
    request["partnerSeeds"] = request["seeds"]
    for entry in request["partners"]:
        entry.pop("seeds", None)
    stamp = uuid.uuid4().hex[:12]
    output = ROOT / "TeamCode/build/local-sim" / pp.stem / stamp
    output.mkdir(parents=True)
    before = fingerprint()
    dirty = git("status", "--porcelain")
    request.setdefault("metadata", {}).update(
        SourceBranch=git("branch", "--show-current"),
        SourceCommit=git("rev-parse", "HEAD"),
        SourceDirty="unknown" if dirty == "unknown" else str(bool(dirty)).lower(),
        SourcePath=request["pp"],
        LocalInputsSHA256=before,
    )
    (output / "request.json").write_text(json.dumps(request, indent=2), encoding="utf-8")
    (output / "saved-source.pp").write_bytes(pp.read_bytes())
    (output / java.name).write_bytes(java.read_bytes())
    print(f"Simulating saved checkout files: {request['pp']}", flush=True)
    print(f"Results and AdvantageScope logs: {output}", flush=True)
    if args.prepare_only:
        return 0
    env = dict(os.environ, BIOBUZZ_SIM_RUN=str(output))
    command = [str(ROOT / ("gradlew.bat" if os.name == "nt" else "gradlew")), ":TeamCode:testDebugUnitTest", "--tests", "org.firstinspires.ftc.teamcode.logging.SimRunTest", "--rerun", "--console=plain"]
    if os.name != "nt":
        command.insert(0, "sh")
    status = subprocess.run(command, cwd=ROOT, env=env).returncode
    if fingerprint() != before:
        (output / "INPUTS_CHANGED.txt").write_text("Checkout files changed during this run. Save and rerun before using these results.\n", encoding="utf-8")
        print("Checkout files changed during simulation. These results are stale; save and rerun.", file=sys.stderr)
        return 1
    result = output / "result.json"
    if status != 0 or not result.exists() or json.loads(result.read_text()).get("error"):
        print("Simulation failed. Check the Gradle output and result.json above.", file=sys.stderr)
        return status or 1
    for entry in json.loads(result.read_text()).get("entries", []):
        rows = entry.get("rows", [])
        collisions = [row["robotsCollidedAt"] for row in rows if row.get("robotsCollidedAt") is not None]
        contact = f"first collision at {min(collisions):.2f}s" if collisions else "no robot collision"
        print(f"{entry['name']}: {entry.get('meanPoints', '?')} mean points; {contact}")
    print(f"Completed. Open the .wpilog files under {output} in AdvantageScope.")
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (ValueError, OSError) as error:
        print(str(error), file=sys.stderr)
        sys.exit(1)
