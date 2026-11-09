import pickle
S='/tmp/claude-0/-home-user-biobuzz/fc16faa2-d150-54ec-97b5-3071dce4f144/scratchpad'
c=pickle.load(open(f'{S}/nectar/cache.pkl','rb'))
for (t,k,f),d in sorted(c.items()):
    if k!='matches': continue
    rd=' | '.join(str(v) for _,v in d.get('/RealMetadata/RobotDifferences',[]))
    print(t,f[:19], 'nectar/reject in RobotDifferences:', 'ectar' in rd or 'eject' in rd, '| hw colour:', [v for _,v in d.get('/RealMetadata/Hardware/ColorSensorSubsystem',[])])
