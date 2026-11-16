import json,sys
S='/tmp/claude-0/-home-user-biobuzz/59b06a18-b061-5f2b-bc74-373e5c660fc0/scratchpad/pp/'
def seg(s,e,a0,a1): return {"startProgress":s,"endProgress":e,"interpolationType":"linear","reversed":False,"continueFromPrevious":False,"parameters":{"startDeg":a0,"endDeg":a1}}
base={"locked":False,"waitBeforeMs":0,"waitAfterMs":0,"waitBeforeName":"","waitAfterName":"","kind":"atomic"}
def pw(a0,a1,segs): return {"type":"piecewise","startDeg":a0,"endDeg":a1,"degrees":0,"reverse":False,"piecewiseHeading":{"segments":segs}}
def make(fname, export, wait_ms, first='CATCH', H=(20,88,110), in_cps=None, wait2_ms=None):
    d=json.load(open('/home/user/biobuzz/TeamCode/autos/drafts/r-two-tip.pp'))
    a=d['auto']; a['exportName']=export; a['points']['NECTAR_HANDOFF']=list(H)
    P=a['points']; end=P['LEFT_LAUNCH_'+first]
    to_n=dict(base,id="lane-to-nectar",color="#f28e2b",name="outside lane to NECTAR_HANDOFF",endPoint={"x":H[0],"y":H[1]},
      controlPoints=[{"x":27,"y":66}],heading=pw(180,H[2],[seg(0,0.25,180,90),seg(0.25,0.7,90,90),seg(0.7,1,90,H[2])]))
    n_park=dict(base,id="nectar-park",color="#59a14f",name="NECTAR_HANDOFF park",endPoint={"x":14.5,"y":98.7},controlPoints=[],
      heading={"type":"linear","startDeg":H[2],"endDeg":0})
    cps=in_cps or ([(19,108),(30,125)] if first=='CATCH' else [(19,104),(24,119)])
    lane_in=dict(base,id="lane-in",color="#76b7b2",name="past the LOADING ZONE to LEFT_LAUNCH_"+first,endPoint={"x":end[0],"y":end[1]},
      controlPoints=[{"x":x,"y":y} for x,y in cps],heading=pw(H[2],end[2],[seg(0,0.3,H[2],90),seg(0.3,0.6,90,0),seg(0.6,1,360,end[2])]))
    lines=[]
    for l in d['lines']:
        if l['id'].startswith('link-'): continue
        if l['id']=='wall-lane': lines+=[to_n,n_park,lane_in]; continue
        if first=='OFFSET':
            if l['id']=='line-mwbij6hk-eyn74i': l=dict(l,name='LEFT_LAUNCH_OFFSET park',heading={"type":"linear","startDeg":end[2],"endDeg":0})
            if l['id']=='to-scoring': l=dict(l,heading={"type":"linear","startDeg":end[2],"endDeg":90})
            if l['id']=='to-right-close':
                l=json.loads(json.dumps(l)); l['controlPoints']=[{"x":52,"y":108},{"x":65.2,"y":71.9},{"x":59.6,"y":40.9}]
                l['heading']=pw(end[2],90,[seg(0,0.15,end[2],270),seg(0.15,0.87,270,270),seg(0.87,1,270,90)])
        lines.append(l)
    d['lines']=lines
    pe=a['pathEnds']; del pe['wall-lane']; pe.update({'lane-to-nectar':'NECTAR_HANDOFF','nectar-park':'RIGHT_PARK','lane-in':'LEFT_LAUNCH_'+first})
    if first=='OFFSET': a['points'].pop('LEFT_LAUNCH_CATCH',None)
    cards=a['cards']; i=[c['id'] for c in cards].index('c-p-14')
    new=[{"id":"c-p-14","kind":"path","lineId":"lane-to-nectar","park":False}]
    if wait_ms: new.append({"id":"c-w-nectar","kind":"firstOf","label":"Wait for the human player's NECTAR","rows":[{"when":["IntakeFull"],"cards":[]},{"afterMs":wait_ms,"cards":[]}]})
    new.append({"id":"c-p-lane-in","kind":"path","lineId":"lane-in","park":False})
    cards[i:i+1]=new
    a['parkFrom'].update({'c-p-14':'nectar-park','c-p-lane-in':'line-mwbij6hk-eyn74i'})
    a.pop('linkPaths',None); a.pop('teamSpots',None); d['sequence']=[]
    json.dump(d,open(S+fname,'w'),indent=1)
if __name__=='__main__':
    make('hp-w0.pp','HP-W0',0)
    make('hp-w5.pp','HP-W5',500)
    make('hp-o5.pp','HP-O5',500,'OFFSET')
    make('hp-o0.pp','HP-O0',0,'OFFSET')
