def theory_svg():
    """Block A the way the lab drove it: steep face first, ramp toward the robot. The steep face leans 17 deg from
    vertical and meets the POLLEN (centre 1.4 in, against the uprights) below its middle. x along the push, z up, inches."""
    R = 1.4; cx, cz = 0.35, 1.4
    bottom, top, land, slant, flat, depth = 0.52, 1.30, 0.12, 0.2, 0.5, 1.4
    lean = math.atan2(slant, top - bottom - land)            # 17 deg from vertical
    nx, nz = math.cos(lean), math.sin(lean)                  # the face's normal, toward the ball: tilted up by `lean`
    px, pz = cx - R * nx, cz - R * nz                        # where the leaning face touches the ball
    # place the block so its leaning face passes through the contact point: the face runs from (tx, bottom+land) to (tx+slant, top)
    tx = px + (pz - (bottom + land)) * (slant / (top - bottom - land))   # the face's foot, nearest the ball; the block extends toward the robot (-x)
    block = [(tx, bottom), (tx, bottom + land), (tx - slant, top), (tx - slant - flat, top), (tx - depth, bottom)]
    rod = (tx - 12 / 25.4, 0.77)
    up_x = cx + R
    pts = " ".join(f"{X(x)},{Y(z)}" for x, z in block)
    deg = math.degrees(lean)
    return (f'<svg viewBox="0 0 660 400" role="img" aria-labelledby="th">\n'
    f'<title id="th">Block A driven steep face first, its ramp toward the robot. The steep face, leaning {deg:.0f} degrees from vertical, meets the POLLEN below its middle; the POLLEN rests against the upright. The push at the face is tilted up only {deg:.0f} degrees; the upright pushes back horizontally; friction at both contacts resists the POLLEN rising.</title>\n'
    '<defs><marker id="ar" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse"><path d="M0,0 L10,5 L0,10 z" fill="var(--petg)"/></marker>\n'
    '<marker id="ag" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse"><path d="M0,0 L10,5 L0,10 z" fill="var(--muted)"/></marker></defs>\n'
    f'<line x1="0" y1="{Y(0)}" x2="660" y2="{Y(0)}" stroke="var(--muted)" stroke-width="2"/>\n'
    f'<rect x="{X(up_x)}" y="{Y(3.6)}" width="30" height="{Y(0) - Y(3.6)}" fill="var(--steel)" opacity=".8"/>\n'
    f'<text x="{X(up_x) + 36}" y="{Y(2.6)}" class="lbl">upright</text>\n'
    f'<polygon points="{pts}" fill="var(--petg)" opacity=".85"/>\n'
    f'<circle cx="{X(rod[0])}" cy="{Y(rod[1])}" r="16" fill="var(--steel)" stroke="var(--fg)" stroke-width="1"/>\n'
    f'<text x="{X(tx - depth)}" y="{Y(0) + 20}" font-size="12">Block A: steep face first, ramp toward the robot</text>\n'
    f'<text x="{X(tx - depth)}" y="{Y(0) + 36}" class="lbl" font-size="11">the face leans {deg:.0f}°; the ball should pop onto the top and roll out, left</text>\n'
    f'<line x1="{X(tx - slant - flat)}" y1="{Y(1.9)}" x2="{X(tx - depth - 0.8)}" y2="{Y(1.9)}" stroke="var(--muted)" stroke-width="2" marker-end="url(#ag)"/>\n'
    f'<text x="{X(tx - depth - 0.8)}" y="{Y(1.9) - 8}" class="lbl" font-size="11">the ramp out: toward the robot</text>\n'
    f'<circle cx="{X(cx)}" cy="{Y(cz)}" r="{R * 100}" fill="var(--pollen)" opacity=".5" stroke="var(--fg)" stroke-width="1.5"/>\n'
    f'<text x="{X(cx)}" y="{Y(cz + 0.75)}" text-anchor="middle">POLLEN</text>\n'
    f'<line x1="{X(px)}" y1="{Y(pz)}" x2="{X(px + 1.0 * nx)}" y2="{Y(pz + 1.0 * nz)}" stroke="var(--petg)" stroke-width="3" marker-end="url(#ar)"/>\n'
    f'<text x="{X(px + 1.0 * nx) + 4}" y="{Y(pz) + 2}" fill="var(--petg)">push at the face,</text>\n'
    f'<text x="{X(px + 1.0 * nx) + 4}" y="{Y(pz) + 16}" fill="var(--petg)">tilted up only {deg:.0f}°</text>\n'
    f'<path d="M{X(px + 0.6)},{Y(pz)} A60,60 0 0 0 {X(px + 0.6 * nx)},{Y(pz + 0.6 * nz)}" fill="none" stroke="var(--petg)" stroke-width="1.5"/>\n'
    f'<line x1="{X(px)}" y1="{Y(pz)}" x2="{X(px + 0.9)}" y2="{Y(pz)}" stroke="var(--petg)" stroke-dasharray="4 4"/>\n'
    f'<text x="{X(px + 0.7)}" y="{Y(pz + 0.18)}" fill="var(--petg)">α</text>\n'
    f'<line x1="{X(up_x)}" y1="{Y(cz)}" x2="{X(up_x - 0.9)}" y2="{Y(cz)}" stroke="var(--muted)" stroke-width="3" marker-end="url(#ag)"/>\n'
    f'<text x="{X(up_x - 0.95)}" y="{Y(cz) - 8}" class="lbl" text-anchor="end">the upright pushes back</text>\n'
    f'<line x1="{X(up_x)}" y1="{Y(cz)}" x2="{X(up_x)}" y2="{Y(cz - 0.8)}" stroke="var(--muted)" stroke-width="3" marker-end="url(#ag)"/>\n'
    f'<text x="{X(up_x) - 8}" y="{Y(cz - 0.8) + 16}" class="lbl" text-anchor="end">friction, down</text>\n'
    f'<line x1="{X(px)}" y1="{Y(pz)}" x2="{X(px + 0.5 * nz)}" y2="{Y(pz - 0.5 * nx)}" stroke="var(--muted)" stroke-width="3" marker-end="url(#ag)"/>\n'
    f'<text x="{X(px) - 6}" y="{Y(0.3)}" class="lbl" text-anchor="end">friction at the face, down</text>\n'
    '</svg>')
