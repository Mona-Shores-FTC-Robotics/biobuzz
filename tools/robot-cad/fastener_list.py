"""The fasteners the CAD draws, as Markdown: every joint (what it holds, its screws, how many) and the totals to buy.

    python3 tools/robot-cad/fastener_list.py      # the front (cad/intake-b), the transfer (cad/transfer) and the Limelight's mount

It reads cad/fasteners.py's records after building the front, the transfer and the mount, so the list is always the drawing's."""
import os, sys, re, collections, importlib.util
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, '..', '..', 'cad', 'full-robot'))
def load(n, p):
    s = importlib.util.spec_from_file_location(n, p); m = importlib.util.module_from_spec(s); s.loader.exec_module(m); return m
FR = load('fr', os.path.join(HERE, '..', '..', 'cad', 'full-robot', 'build.py'))
FR.RP.limelight_fasteners({}, FR.C, FR.F, FR.FACE)
import fasteners as FA
print("| Joint | Holds | Fastener | Count | Service |\n|---|---|---|---|---|")
for joint, holds, what, n, service in FA.JOINTS:
    print(f"| `{joint}` | {holds} | {what} | {n} | {service or ''} |")
buy = collections.Counter()
for name in FA.INFO:
    m = re.search(r"\((goBILDA [\d-]+), (M\d x \d+) (socket|flat) head\)", name)
    buy[f"{m.group(1)}, {m.group(2)} {m.group(3)} head screw" if m else re.search(r"\((.*)\)", name).group(1)] += 1
buy["goBILDA 2812-0004-0007, M4 nylon-insert lock nut"] = sum(1 for n in FA.NUTS)
inserts = sum(1 for I in FA.INFO.values() if I['into'] and re.search(r"carriage|bracket|ceiling_post|feeder_bridge|pad_", I['into']))
buy["M4 heat-set insert (printed parts)"] = inserts
buy["M4 large washer, 12 mm OD"] = sum(1 for d in (FR.IB.hook, FR.IB.fixed, FR.IB.flt) for n in d if "_washer_" in n)
buy["M4 spacer, 6 mm long, 7 mm OD (under the Limelight)"] = 2
print("\n| To buy | Count |\n|---|---|")
for k, v in sorted(buy.items()): print(f"| {k} | {v} |")
