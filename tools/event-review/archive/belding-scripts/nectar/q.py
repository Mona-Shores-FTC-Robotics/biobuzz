import pickle,sys
S='/tmp/claude-0/-home-user-biobuzz/fc16faa2-d150-54ec-97b5-3071dce4f144/scratchpad'
c=pickle.load(open(f'{S}/nectar/cache.pkl','rb'))
for (t,k,f),d in sorted(c.items()):
    if k!='matches': continue
    md={kk.split('/')[-1]:v[-1][1] for kk,v in d.items() if kk.startswith('/RealMetadata') and v}
    print('=====',t,f, md.get('GitCommit'), md.get('MatchId'), md.get('OpMode'))
    print('  tun:', {x:md.get('ColorSensorSubsystem.'+x) for x in ['rejectEnemyNectar','rejectBurstS','rejectHoldOffS','redNectarHueMin','redNectarHueMax','blueNectarHueMin','blueNectarHueMax','pollenHueMin','pollenHueMax','rejectAfterReadings','rejectMaxBursts','presentAbove','leaveBelow']})
    print('  DS:', d.get('/DriverStation/AllianceStation'), d.get('/DriverStation/RobotMode'))
    for ts,e in d.get('/Events',[]):
        if any(s in str(e) for s in ('PLAY','NECTAR','REJECT','alliance','Alliance','UNKNOWN')):
            print('   %7.2f %s'%(ts,e))
