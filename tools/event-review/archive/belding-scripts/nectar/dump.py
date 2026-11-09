import sys, glob, os, pickle
sys.path.insert(0, '/home/user/biobuzz/tools/pull-logs')
import read_logs as R
S='/tmp/claude-0/-home-user-biobuzz/fc16faa2-d150-54ec-97b5-3071dce4f144/scratchpad'
out={}
for team in ('19429','20245'):
  for kind in ('matches','practice'):
    for p in sorted(glob.glob(f'{S}/bel/{team}/{kind}/*.wpilog.gz')):
        d=R.read(p)
        keep={k:v for k,v in d.items() if k.startswith(('/Events','/Lane/','/ColorSensor/','/DriverStation/','/Launcher/Shots','/RealMetadata','/Intake/TicksPerSecond','/Intake/State','/Intake/Requested'))}
        out[(team,kind,os.path.basename(p))]=keep
pickle.dump(out,open(f'{S}/nectar/cache.pkl','wb'))
print(len(out))
