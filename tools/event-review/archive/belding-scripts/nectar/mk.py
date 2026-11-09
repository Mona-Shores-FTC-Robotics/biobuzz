import json
S='/tmp/claude-0/-home-user-biobuzz/fc16faa2-d150-54ec-97b5-3071dce4f144/scratchpad/nectar'
raw=json.load(open(f'{S}/raw.json'))
verdict={('20245','Q7',0):('correct','cleared sensor by +0.27 s (alpha 0.047); never counted in; burst cut to 0.15 s, hold-off 0.30 s; next 5 shots POLLEN'),
         ('20245','Q7',1):('correct','tracker had counted it in at -0.04 s; "piece out past the sensor" at +0.35 s; sensor read POLLEN by +0.36 s; never at front at a shot'),
         ('20245','Q31',0):('false (likely POLLEN)','one dim reading hue 46 (red max 47, POLLEN min 48) between POLLEN readings 61-76; lane was FULL with a POLLEN held; it backed out and re-entered as "POLLEN in, 4" at +0.61 s'),
         ('20245','M3',0):('correct','sensor empty (alpha 0.021) by +0.14 s; never counted in; burst 0.52 s, no hold-off logged')}
rows=[]; seen={}
for r in raw['rejects']:
    i=seen.get((r['team'],r['match']),0); seen[(r['team'],r['match'])]=i+1
    v=verdict[(r['team'],r['match'],i)]
    rows.append(dict(kind='reject',team=r['team'],match=r['match'],alliance=r['alliance'],when=r['phase'],event=r['event'],
        rejecting_timeline_s=r['rejecting_changes'],verdict=v[0],outcome=v[1]))
summ=[]
for s in raw['summary']:
    opp=[x for x in s['sightings'] if not x['own']]
    summ.append(dict(team=s['team'],match=s['match'],alliance=s['alliance'],lane_in=s['lane_in'],
        nectar_sightings_own=sum(1 for x in s['sightings'] if x['own']),nectar_sightings_opposing=len(opp),
        reject_events=s['rejects'],counted_shots=s['shots'],front_slot_at_shot=s['shot_front'],opposing_sightings=opp))
json.dump(dict(build='7ed351fdb3',source='scratchpad/bel/<team>/matches/*.wpilog.gz',
  notes=['sighting = consecutive colour samples with alpha>0.05 classified RED/BLUE by the build\'s hue ranges, Auto+TeleOp only',
         'front_slot_at_shot = /Lane/Slot1 just before /Launcher/Shots increments (LaneTracker inference, drifts)',
         'Q31 RED sighting is the false reject (POLLEN edge reading), not a real opposing NECTAR'],
  rejects=rows,matches=summ),open(f'{S}/rejects.json','w'),indent=1)
print(len(rows),len(summ))
