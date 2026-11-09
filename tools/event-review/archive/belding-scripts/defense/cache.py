import sys, gzip, struct, pickle, glob, os
KEEP = {'/Events','/DriverStation/RobotMode','/DriverStation/Joystick0/ButtonValues','/DriverStation/Joystick0/AxisValues',
'/DriverStation/Joystick1/ButtonValues','/Drive/CommandForward','/Drive/CommandStrafe','/Drive/CommandTurn','/Drive/OdometrySpeedInPerSec',
'/Odometry/PedroInches/X','/Odometry/PedroInches/Y','/Odometry/PedroInches/HeadingDeg','/Odometry/Referenced','/Lane/Full','/Lane/Count',
'/Launcher/Shots','/FeedGate/Feeding','/Drive/WheelTicksPerSec/FrontLeft','/Drive/WheelTicksPerSec/FrontRight','/Drive/WheelTicksPerSec/BackLeft','/Drive/WheelTicksPerSec/BackRight','/DriverStation/Enabled','/DriverStation/Autonomous','/RealMetadata/OpMode','/RealMetadata/RobotConfig','/RealMetadata/MatchId'}
def read(path):
    b = gzip.open(path,'rb').read()
    i = 12 + struct.unpack_from('<I', b, 8)[0]
    entries, data = {}, {}
    while i < len(b):
        h = b[i]; i += 1
        il, sl, tl = (h & 3) + 1, ((h >> 2) & 3) + 1, ((h >> 4) & 7) + 1
        eid = int.from_bytes(b[i:i+il],'little'); i += il
        size = int.from_bytes(b[i:i+sl],'little'); i += sl
        ts = int.from_bytes(b[i:i+tl],'little'); i += tl
        p = b[i:i+size]; i += size
        if eid == 0:
            if p and p[0] == 0:
                e = struct.unpack_from('<I',p,1)[0]; o=5
                n = struct.unpack_from('<I',p,o)[0]; o+=4; name=p[o:o+n].decode(errors='replace'); o+=n
                n = struct.unpack_from('<I',p,o)[0]; o+=4; typ=p[o:o+n].decode(errors='replace')
                entries[e]=(name,typ); data.setdefault(name,[])
            continue
        if eid not in entries: continue
        name, typ = entries[eid]
        if name not in KEEP: continue
        try:
            if typ=='double': v=struct.unpack('<d',p)[0]
            elif typ=='float': v=struct.unpack('<f',p)[0]
            elif typ=='int64': v=struct.unpack('<q',p)[0]
            elif typ=='boolean': v=bool(p[0])
            elif typ in ('string','json'): v=p.decode(errors='replace')
            elif typ=='float[]': v=struct.unpack('<%df'%(len(p)//4),p)
            else: continue
        except Exception: continue
        data[name].append((ts/1e6,v))
    return {k:v for k,v in data.items() if k in KEEP}
base='/tmp/claude-0/-home-user-biobuzz/fc16faa2-d150-54ec-97b5-3071dce4f144/scratchpad/bel'
for f in sorted(glob.glob(base+'/*/matches/*.wpilog.gz')):
    team=f.split('/')[-3]; out='%s_%s.pkl'%(team,os.path.basename(f)[:19])
    if os.path.exists(out): continue
    d=read(f); pickle.dump(d,open(out,'wb')); print(out, {k:len(v) for k,v in d.items() if k in('/Events','/DriverStation/Joystick0/AxisValues')})
