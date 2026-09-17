import math, os, random, struct, wave
ROOT=os.path.join('demonic-daw','app','src','main','assets','factory')
os.makedirs(ROOT,exist_ok=True)
SR=44100

def norm(xs):
    peak=max(1e-9,max(abs(x) for x in xs)); return [max(-1,min(1,x/peak*.82)) for x in xs]
def wav(name,fn,dur=2.2):
    n=int(SR*dur); xs=norm([fn(i/SR,dur) for i in range(n)])
    with wave.open(os.path.join(ROOT,name+'.wav'),'wb') as w:
        w.setnchannels(1);w.setsampwidth(2);w.setframerate(SR);w.writeframes(b''.join(struct.pack('<h',int(x*32767)) for x in xs))
def fade(t,d,a=.01,r=.25):
    return min(1,t/max(a,1e-4))*min(1,max(0,(d-t)/max(r,1e-4)))
def harm(t,f,coefs): return sum(a*math.sin(2*math.pi*f*(i+1)*t) for i,a in enumerate(coefs))
F=261.625565
random.seed(91)

wav('grand',lambda t,d: fade(t,d,.004,.45)*harm(t,F,[1,.55,.31,.18,.1,.06])*math.exp(-1.7*t))
wav('rhodes',lambda t,d: fade(t,d,.006,.55)*(math.sin(2*math.pi*F*t)+.34*math.sin(4*math.pi*F*t+.3))*math.exp(-1.1*t))
wav('organ',lambda t,d: fade(t,d,.03,.35)*harm(t,F,[1,.65,.35,.22,.14]))
wav('guitar',lambda t,d: fade(t,d,.002,.35)*(harm(t,F,[1,.45,.22,.12])+.07*(random.random()*2-1))*math.exp(-2.2*t))
wav('funk-guitar',lambda t,d: fade(t,d,.002,.12)*(harm(t,F,[1,.32,.17,.08])+.04*(random.random()*2-1))*math.exp(-4.0*t),1.3)
wav('bass',lambda t,d: fade(t,d,.003,.4)*(math.sin(2*math.pi*(F/4)*t)+.28*math.sin(4*math.pi*(F/4)*t)+.12*math.sin(6*math.pi*(F/4)*t))*math.exp(-.65*t))
wav('gfunk-bass',lambda t,d: fade(t,d,.004,.45)*(math.sin(2*math.pi*(F/4)*t)+.22*math.sin(2*math.pi*(F/2)*t)+.08*math.sin(2*math.pi*F*t))*math.exp(-.5*t))
wav('brass',lambda t,d: fade(t,d,.06,.35)*harm(t,F,[1,.55,.35,.22,.16,.1,.07])*math.exp(-.35*t))
wav('strings',lambda t,d: fade(t,d,.28,.55)*harm(t,F,[1,.48,.28,.18,.12,.08])*(.94+.06*math.sin(2*math.pi*5.1*t)))
wav('demonic-synth',lambda t,d: fade(t,d,.01,.45)*(sum(math.sin(2*math.pi*F*(i+1)*t)/(i+1) for i in range(7))+.22*math.sin(2*math.pi*(F*.5)*t))*math.exp(-.3*t))

banks={
'grand':('grand',60,.7),'rhodes':('rhodes',60,.9),'gospel':('organ',60,.5),'jazz':('rhodes',60,.7),'organ':('organ',60,.5),
'electric-guitar':('guitar',60,.35),'acoustic-guitar':('guitar',60,.45),'funk-guitar':('funk-guitar',60,.15),'lead-guitar':('guitar',60,.5),
'electric-bass':('bass',48,.35),'synth-bass':('gfunk-bass',48,.3),'gfunk-bass':('gfunk-bass',48,.5),'808-sub':('bass',48,.8),'walking-bass':('bass',48,.3),'slap-bass':('funk-guitar',48,.2),
'trumpet':('brass',60,.35),'sax':('brass',60,.45),'trombone':('brass',55,.5),'brass-stack':('brass',60,.55),'strings':('strings',60,1.1),'synth':('demonic-synth',60,.5),'perc-fx':('funk-guitar',60,.2)
}
for bank,(sample,root,rel) in banks.items():
    with open(os.path.join(ROOT,bank+'.sfz'),'w',encoding='utf8') as f:
        f.write(f'<region> sample={sample}.wav lokey=0 hikey=127 pitch_keycenter={root} lovel=1 hivel=127 ampeg_release={rel} volume=-2\n')
print('Generated',len(banks),'Demonic Factory SFZ banks in',ROOT)
