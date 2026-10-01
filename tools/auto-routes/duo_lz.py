from autogen import *
from helpers import waits, fire, flower, flower_points, leave_flower, FAR_FLOWER_AT, WALL_FLOWER_AT

def right(speed=50, name="duo-lz-right", garden=True):
    r = Route(name, (59, 9.5, 90), speed=speed)
    r.pt("SLIDE_SL", 41, 9.5, 90).pt("SLIDE_SR", 60, 9.5, 90).pt("HOME_R", 59, 10, 90).pt("GARDEN_IN", 8.5, 22, 270).pt("GARDEN", 8.5, 11, 270).pt("PARK_R", 14, 89, 330).pt("PARK_R2", 14, 90, 330)
    r.add(r.action("LaunchOne"), r.action("LaunchOne"), r.action("LaunchOne"),
          r.wait("Tip 1", when=["LeftCellUp"], ms=2500),
          r.wait("Spill rolls in", when=["IntakeFull"], ms=1800),
          *waits(r, "Our CELL up", "RightCellUp", 7.5),
          fire(r, "Fire", "Empty"),
          *([r.go("GARDEN_IN", ctrl=[(30, 22)]), r.go("GARDEN"),
             r.wait("Collect in the GARDEN", when=["IntakeFull"], ms=1200),
             r.go("SLIDE_SL", ctrl=[(20, 18)], heading=90)] if garden else [r.go("SLIDE_SL", heading=90)]),
          r.go("SLIDE_SR", heading=90),
          r.wait("Sweep", when=["IntakeFull"], ms=300),
          fire(r, "Fire until it tips (TIP 3)", "LeftCellUp"),
          fire(r, "Fire until it tips (2)", "LeftCellUp", ms=1500),
          r.go("HOME_R", heading=90),
          r.wait("Spill rolls in (2)", when=["IntakeFull"], ms=1500),
          r.go("PARK_R", ctrl=[(24, 14), (18, 50)]),
          *waits(r, "Left CELL up", "LeftCellUp", 10.0),
          fire(r, "Fire from the LOADING ZONE (TIP 4)", "Empty"),
          r.go("PARK_R2", park=True))
    return r

def left(speed=50, name="duo-lz-left"):
    r = Route(name, (59, 132.25, 270), speed=speed)
    flower_points(r, "FLOWER_L", FAR_FLOWER_AT, 90).pt("HOME_L", 59, 131.75, 270).pt("PARK_L", 15, 119, 300).pt("PARK_L2", 15, 118, 300)
    r.add(r.action("SpinUp"),
          *waits(r, "Right tips", "LeftCellUp", 7.5),
          fire(r, "Fire the preloads", "Empty"),
          *flower(r, "FLOWER_L", "Collect at the FLOWER", ms=2500),
          *leave_flower(r, "FLOWER_L", "HOME_L"),
          fire(r, "Fire until it tips (TIP 2)", "RightCellUp"),
          fire(r, "Fire until it tips (2)", "RightCellUp", ms=1500),
          r.wait("Spill rolls in", when=["IntakeFull"], ms=1500),
          *waits(r, "Our CELL up again", "LeftCellUp", 7.5),
          fire(r, "Fire (TIP 4)", "Empty"),
          r.go("PARK_L", ctrl=[(59, 118), (30, 118)]),
          *waits(r, "Left CELL up", "LeftCellUp", 5.0),
          fire(r, "Fire from the LOADING ZONE", "Empty"),
          r.go("PARK_L2", park=True))
    return r

if __name__ == "__main__":
    import sys
    right(name="duo-lz-right", garden=True).write()
    left(name="duo-lz-left").write()
    study("DuoLzRightAuto,DuoLzLeftAuto@50", runs=int(sys.argv[1]) if len(sys.argv) > 1 else 10)
