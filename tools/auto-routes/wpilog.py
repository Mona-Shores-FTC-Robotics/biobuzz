"""wpilog.py: a minimal WPILOG reader. read(path) -> [(seconds, key, type, payload bytes)]; run as a script to print the
string entries whose key contains any of the arguments: python3 wpilog.py run.wpilog Events"""
import struct,sys
def read(path):
    b=open(path,'rb').read(); i=12+struct.unpack('<I',b[8:12])[0]; ents={}; out=[]
    while i<len(b):
        h=b[i]; i+=1; el=(h&3)+1; sl=((h>>2)&3)+1; tl=((h>>4)&7)+1
        e=int.from_bytes(b[i:i+el],'little'); i+=el; s=int.from_bytes(b[i:i+sl],'little'); i+=sl
        t=int.from_bytes(b[i:i+tl],'little'); i+=tl; p=b[i:i+s]; i+=s
        if e==0:
            if p[0]==0:
                eid=struct.unpack('<I',p[1:5])[0]; n=struct.unpack('<I',p[5:9])[0]; name=p[9:9+n].decode()
                k=9+n; m=struct.unpack('<I',p[k:k+4])[0]; typ=p[k+4:k+4+m].decode(); ents[eid]=(name,typ)
        else: out.append((t/1e6,)+ents[e]+(p,))
    return out
if __name__=='__main__':
    for t,n,typ,p in read(sys.argv[1]):
        if typ=='string' and any(k in n for k in sys.argv[2:]): print(f"{t:7.2f} {n}: {p.decode()}")
