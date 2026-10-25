"""The check that retired v10: does anything on the self-tripping nose piece touch the locked ball? Pivot (0.32, 0.96),
the posts give 54 deg of swing; the ball locked at (0.33, 2.48) with the lip 0.83 and the body advanced 0.22 (lip 1.05)."""
import math
R = 1.4
def contact_swing(pivot, tip, lip, ball=(0.33, 2.48)):
    px, pz = lip - pivot[0], pivot[1]; tx, tz = lip - tip[0], tip[1]
    for d in range(0, 181):
        a = math.radians(d); dx, dz = tx - px, tz - pz
        x = px + dx * math.cos(a) - dz * math.sin(a); z = pz + dx * math.sin(a) + dz * math.cos(a)
        if math.hypot(x - ball[0], z - ball[1]) <= R: return d
for L in (0, 0.4, 0.6, 0.8, 1.0, 1.3, 1.6, 2.0):
    print(f"toe {L} in on v10's pin: touches the ball after {contact_swing((0.32, 0.96), (-L, 0.42) if L else (0, 0.6), 1.05)} deg; the posts give 54")
for L in (1.0, 1.3): print(f"toe {L} in hinged at the lip's foot: touches after {contact_swing((0, 0.52), (-L, 0.52), 0.83)} deg; that design swung 75")
