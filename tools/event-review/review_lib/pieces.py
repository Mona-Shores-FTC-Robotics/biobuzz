"""Pieces launched per match (#506 detector), POLLEN or NECTAR, and the NECTAR rejects.

- Auto / TeleOp pieces: the #506 detector's shots in each gate opening, by which OpMode ran.
- NECTAR launched: shots of the robot's own counter whose front lane slot (/Lane/Slot1, 0.15 s
  before the count) held NECTAR. TeleOp POLLEN is the TeleOp total less those.
- NECTAR rejected: `Lane: REJECT` events, less likely false rejects: a reading whose hue is within
  FALSE_REJECT_HUE of the POLLEN range (the dim POLLEN at hue 46° in Belding's Q31, #508).
"""
import re

FALSE_REJECT_HUE = 2.0
KIND = {1: 'pollen', 2: 'nectar', 3: 'nectar', 4: 'unknown', 0: 'untracked'}


def false_reject(m, e):
    h = re.search(r'hue (\d+)°', e)
    lo = float(m.meta.get('ColorSensorSubsystem.pollenHueMin') or 48)
    hi = float(m.meta.get('ColorSensorSubsystem.pollenHueMax') or 75)
    return bool(h) and lo - FALSE_REJECT_HUE <= float(h.group(1)) <= hi + FALSE_REJECT_HUE


def classify_old(m):
    """{'auto_pollen': n, 'teleop_nectar': n, ...}: the robot's own counted shots by front slot."""
    a, ae, te, end = m.auto_start, m.auto_end, m.tele_start, m.log_end
    s1 = m['/Lane/Slot1']
    sh = m['/Launcher/Shots']
    row = {'%s_%s' % (ph, k): 0 for ph in ('auto', 'teleop') for k in ('pollen', 'nectar', 'unknown', 'untracked')}
    last = sh.at(a) or 0
    for t, v in sh:
        if t < a or t > end:
            continue
        if v > last:
            for _ in range(int(v - last)):
                val = s1.at(t - 0.15 - 1e-9)
                val = int(val) if val is not None else 0
                ph = 'auto' if t <= ae else ('teleop' if t >= te else 'auto')
                row['%s_%s' % (ph, KIND.get(val, 'unknown'))] += 1
        last = v
    return row


def build(ms_by_team, vols):
    rows, nec = [], []
    for team, ms in ms_by_team.items():
        own_in = opp_rej = false_rej = own_launched = 0
        for m in ms:
            vs = [v for v in vols if v['team'] == team and v['log'] == m.log]
            a = sum(v['new'] for v in vs if v['phase'] == 'auto')
            t = sum(v['new'] for v in vs if v['phase'] == 'tele')
            o = classify_old(m)
            nshot = o['auto_nectar'] + o['teleop_nectar']
            rej = [e for _, e in m.events if e.startswith('Lane: REJECT')]
            fr = sum(1 for e in rej if false_reject(m, e))
            own = (m.alliance or m.log_alliance or '').upper()
            own_in += sum(1 for _, e in m.events if e.startswith('lane: %s_NECTAR in' % own)) if own else 0
            opp_rej += len(rej) - fr
            false_rej += fr
            own_launched += nshot
            rows.append(dict(robot=team, match=m.name, order=m.order, auto_pollen=a, teleop_pollen=max(0, t - nshot),
                             unidentified=0, alliance=m.alliance or m.log_alliance, nectar_shot=nshot,
                             nectar_rejected=len(rej) - fr, auto_old=o['auto_pollen'],
                             teleop_old=o['teleop_pollen'] + o['auto_untracked'] + o['auto_unknown'] + o['teleop_untracked']
                             + o['teleop_unknown'] + nshot, total=a + t))
        nec.append(dict(robot=team, own_nectar_in=own_in, opposing_at_sensor=opp_rej, opposing_rejected=opp_rej,
                        opposing_launched=0, false_rejects=false_rej, own_nectar_launched=own_launched))
    nec.sort(key=lambda r: -int(r['robot']))
    return {'pieces': rows, 'nectar': nec}
