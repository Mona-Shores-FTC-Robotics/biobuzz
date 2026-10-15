"""The Autos' wait limits, from TeamCode/autos/limits.json (one place to change them; TeamCode/autos/README.md).

    from limits import ms
    r.wait("The far FLOWER's 4", when=["IntakeFull"], ms=ms("flowerS"))
"""
import json
import os

_FILE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "TeamCode", "autos", "limits.json")
LIMITS = {k: v["value"] for k, v in json.load(open(_FILE))["limits"].items()}


def ms(name):
    """A limit in milliseconds, as the Route cards take it."""
    return int(round(LIMITS[name] * 1000))
