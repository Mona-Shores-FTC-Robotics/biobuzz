"""Reproduce the matched RightUnderHive LEFT-reception comparison through SimRunTest."""

import argparse
import csv
import hashlib
import json
import os
import subprocess
from pathlib import Path
from statistics import mean

ROOT = Path(__file__).resolve().parents[2]
ARMS = {"master": "LRightUnderHiveAuto", "draft": "LRightUnderHiveNectarAuto"}
ROLL = {"spot": "40.7,120.5,210", "x": 5.5, "y": 100.18, "speed": 13, "delay": 1, "earliest": 0}


def check_master_routes():
    # The mentor's other trial uses the production L class name. Never label that trial "master".
    generated = ROOT / "TeamCode/src/main/java/org/firstinspires/ftc/teamcode/opmodes/auto/generated"
    for name, expected in (("LRightUnderHiveAuto", "71c40a26fa74ecfd9f3dbcb956f35e4e5bb73c8f"),
                           ("RRightUnderHiveAuto", "e1266c8ad39f6e678e55c9e1aa73adee7f0af2d5")):
        data = (generated / (name + ".java")).read_bytes().replace(b"\r\n", b"\n")
        git_blob = b"blob " + str(len(data)).encode("ascii") + b"\0" + data
        if hashlib.sha1(git_blob).hexdigest() != expected:
            raise ValueError(f"{name} differs from the c5d65866 reference. Run the comparison on "
                             "the paired-study branch with the original master drawings.")


def cases():
    yield "nominal", {}, {"roll": ROLL}
    yield "no roll", {}, {"roll": None}
    yield "late by 5 s", {}, {"roll": dict(ROLL, delay=6)}
    yield "roll misses wide", {}, {"roll": dict(ROLL, aimBiasDeg=20)}
    yield "variable roll", {}, {"roll": dict(ROLL, speedSpread=2, aimSpreadDeg=2)}
    yield "L loses hold", {}, {"roll": ROLL, "loseCatchAim": True}
    for volley, tip in [(2, 2), (3, 3)]:
        yield f"one R pollen misses TIP {tip}", {}, {"roll": ROLL, "missFirstPollenOfRVolley": volley}
    for label, fail in [("R preloads", {"oursFails": "misses preloads"}),
                        ("L preloads", {"fails": "misses preloads"}),
                        ("R garden", {"oursFails": "misses volley 2"}),
                        ("L wall flower", {"fails": "misses volley 2"}),
                        ("R scoring flower", {"oursFails": "misses volley 3"})]:
        yield label + " miss", fail, {"roll": ROLL}
    yield "R slow", {"ourSpeed": 40, "timing": {"speed": 50}}, {"roll": ROLL}
    yield "L slow", {"timing": {"speed": 40}}, {"roll": ROLL}


def prepare(out, nominal, stress):
    for profile in ("fruitport", "best"):
        entries = []
        for name, failure, study in cases():
            count = nominal if name == "nominal" and profile == "fruitport" else stress
            for arm, partner in ARMS.items():
                entries.append(dict(name=f"{arm} / {name}", partner=partner,
                                    seeds=list(range(2, 2 * count + 1, 2)), nectarStudy=study, **failure))
        request = {"spec": "RRightUnderHiveAuto", "pp": "TeamCode/autos/r-right-under-hive.pp",
                   "design": "real robot", "partnerDesign": "real robot", "alliance": "RED",
                   "calibration": "fruitport", "seeds": [1], "partners": entries}
        if profile == "best":
            request["scenario"] = "best"
        dest = out / profile
        dest.mkdir(parents=True, exist_ok=True)
        (dest / "request.json").write_text(json.dumps(request, indent=2) + "\n", encoding="utf8")


def outside_floor(row):
    return (row["autoTips"] < 2 or not (row["parked"] and row["partnerParked"])
            or any(row[k] is not None for k in ("robotsCollidedAt", "crossedAt", "partnerCrossedAt")))


def summary(rows):
    tips = [row["autoTips"] for row in rows]
    receipts = [row["human"] for row in rows]
    def phase(row, shot, target):
        # Preserve the four preload shots on RIGHT for TIP 1. Later RIGHT attempts
        # belong to TIP 3 only after the LEFT CELL tips back up on the RIGHT.
        clock = row["tipsAt"]
        return (shot["left"] == (target == 2) and len(clock) >= target - 1
                and shot["t"] >= clock[target - 2]
                and (len(clock) < target or shot["t"] < clock[target - 1]))
    def full_attempts(row, target):
        shots = [s for s in row["shots"] if phase(row, s, target)]
        return sum(s["pollen"] for s in shots) >= 8
    def human_at_left(row):
        h = row["human"]
        return (h["launchRobot"] == 2 and any(s["humanTip"] == 1 and phase(row, s, 2)
                and s["scoreCell"] == "RED SCORING" for s in row["shots"]))
    def settled_time(n):
        times = [r["tipsAt"][n-1] for r in rows if len(r["tipsAt"]) >= n]
        return round(mean(times), 2) if times else ""
    def count(flag):
        return sum(bool(flag(r)) for r in rows)
    return {"runs": len(rows), "three": sum(i >= 3 for i in tips), "twoPlus": sum(i >= 2 for i in tips),
            "Rpark": count(lambda r: r["parked"]), "Lpark": count(lambda r: r["partnerParked"]),
            "bothPark": count(lambda r: r["parked"] and r["partnerParked"]),
            "contact": count(lambda r: r["robotsCollidedAt"] is not None),
            "crossed": count(lambda r: r["crossedAt"] is not None or r["partnerCrossedAt"] is not None),
            "hive": count(lambda r: r["hitHiveAt"] is not None or r["partnerHitHiveAt"] is not None),
            "wall": count(lambda r: r["RWall"] is not None or r["LWall"] is not None),
            "flower": count(lambda r: r["RFlower"] is not None or r["LFlower"] is not None),
            "g409": count(lambda r: r["g409"]), "floorMiss": count(outside_floor),
            "threeAndFloor": count(lambda r: r["autoTips"] >= 3 and not outside_floor(r)),
            "overCapacity": count(lambda r: max(r["RMaxHeld"], r["LMaxHeld"]) > 4),
            "N1released": sum(h["release"] is not None for h in receipts),
            "N1Lcaptured": sum(h["captureRobot"] == 2 for h in receipts),
            "N1Lfired": sum(h["launchRobot"] == 2 for h in receipts),
            "N1scoredForTip2": count(human_at_left),
            "N1extraScoredAtLeft": count(lambda r: any(s["extra"] and s["humanTip"] == 1
                 and s["scoreCell"] == "RED SCORING" and phase(r, s, 2) for s in r["shots"])),
            "anyExtraFeed": count(lambda r: r["extraFeedStart"] is not None),
            "anyExtraShot": count(lambda r: r["extraShotAt"] is not None),
            "N1carriedToRight": count(lambda r: any(s["humanTip"] == 1 and not s["left"] for s in r["shots"])),
            "N1retainedTip2": count(lambda r: r["human"]["retainedAtTip"] == 2),
            "8PplusN1InTip2": count(lambda r: len(r["settledLoads"]) >= 2 and r["settledLoads"][1]["pollen"] >= 8
                                     and r["settledLoads"][1]["human"]),
            "eightPollenFiredTip2": count(lambda r: full_attempts(r, 2)),
            "eightPollenFiredTip3": count(lambda r: full_attempts(r, 3)),
            "eightPlusHumanTip2": count(lambda r: full_attempts(r, 2) and human_at_left(r)),
            "tip2SettledMean": settled_time(2), "tip3SettledMean": settled_time(3),
            "pointsMean": round(mean(r["points"] for r in rows), 2)}


def summarize(out):
    result = []
    seeds = []
    for profile in ("fruitport", "best"):
        raw = json.loads((out / profile / "result.json").read_text(encoding="utf8"))
        if "error" in raw:
            raise ValueError(raw["error"])
        by_name = {e["name"]: e["rows"] for e in raw["entries"]}
        for name, _, _ in cases():
            pair = [by_name[f"{arm} / {name}"] for arm in ARMS]
            if [r["seed"] for r in pair[0]] != [r["seed"] for r in pair[1]]:
                raise ValueError(f"unmatched seeds in {profile} {name}")
            for arm, rows in zip(ARMS, pair):
                if any(s["t"] > 30.000001 for r in rows for s in r["shots"]):
                    raise ValueError("an AUTO shot left after 30 s")
                for r in rows:
                    if sum(s["extra"] for s in r["shots"]) > 1:
                        raise ValueError(f"multiple optional shots in {profile} {name} seed {r['seed']}")
                    if name == "L loses hold" and r["extraFeedStart"] is not None:
                        raise ValueError("lost hold opened the optional gate")
                    if r["extraFeedStart"] is not None and (r["extraFeedStart"] < 12.5 - 1e-6
                            or r["extraFeedEnd"] is None or r["extraFeedEnd"] > 13.5 + 0.021):
                        raise ValueError(f"optional gate outside its bounds in {profile} {name} seed {r['seed']}")
                    times = r["tipsAt"] + [None] * (3 - len(r["tipsAt"]))
                    receipt = r["human"]
                    seeds.append({"profile": profile, "case": name, "arm": arm, "seed": r["seed"],
                                  "points": r["points"], "tips": r["autoTips"],
                                  "t1": times[0], "t2": times[1], "t3": times[2],
                                  "lastR": r["RLastShot"], "lastL": r["LLastShot"],
                                  "parkR": r["parked"], "parkL": r["partnerParked"],
                                  "robotContact": r["robotsCollidedAt"], "overR": r["crossedAt"],
                                  "overL": r["partnerCrossedAt"], "hiveR": r["hitHiveAt"], "hiveL": r["partnerHitHiveAt"],
                                  "g409": r["g409"], "floorMiss": outside_floor(r),
                                  "released": receipt["release"], "captured": receipt["capture"],
                                  "fired": receipt["launch"], "cellEntry": receipt["score"],
                                  "cell": receipt["scoreCell"], "retainedAtTip": receipt["retainedAtTip"],
                                  "feedOpen": r["extraFeedStart"], "extraShot": r["extraShotAt"], "feedEnd": r["extraFeedEnd"],
                                  "loads": json.dumps(r["settledLoads"], separators=(",", ":"))})
                row = dict(profile=profile, case=name, arm=arm, **summary(rows))
                row["newThreeVsMaster"] = sum(b["autoTips"] >= 3 > a["autoTips"] for a,b in zip(pair[0], rows))
                row["lostThreeVsMaster"] = sum(a["autoTips"] >= 3 > b["autoTips"] for a,b in zip(pair[0], rows))
                row["newFloorMissVsMaster"] = sum(outside_floor(b) and not outside_floor(a) for a,b in zip(pair[0], rows))
                result.append(row)
    for filename, rows in [("summary.csv", result), ("seeds.csv", seeds)]:
        with (out / filename).open("w", encoding="utf8", newline="") as f:
            writer = csv.DictWriter(f, fieldnames=rows[0])
            writer.writeheader()
            writer.writerows(rows)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("out", type=Path, help="destination for exact requests, results, logs and summary.csv")
    parser.add_argument("--mode", choices=["run", "prepare", "summarize"], default="run")
    parser.add_argument("--nominal", type=int, default=60)
    parser.add_argument("--stress", type=int, default=10)
    args = parser.parse_args()
    if min(args.nominal, args.stress) < 1:
        parser.error("seed counts must be positive")
    out = args.out.resolve()
    if args.mode == "run":
        check_master_routes()
    if args.mode in ("run", "prepare"):
        prepare(out, args.nominal, args.stress)
    if args.mode == "run":
        for profile in ("fruitport", "best"):
            wrapper = ROOT / ("gradlew.bat" if os.name == "nt" else "gradlew")
            subprocess.run([str(wrapper), ":TeamCode:testDebugUnitTest", "--tests",
                            "org.firstinspires.ftc.teamcode.logging.SimRunTest", "--rerun", "--console=plain"],
                           check=True, cwd=ROOT, env=dict(os.environ, BIOBUZZ_SIM_RUN=str(out / profile)))
    if args.mode in ("run", "summarize"):
        summarize(out)


if __name__ == "__main__":
    main()
