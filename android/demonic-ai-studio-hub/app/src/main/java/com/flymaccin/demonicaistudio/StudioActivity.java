package com.flymaccin.demonicaistudio;

import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.Locale;

public final class StudioActivity extends Activity {
    private static final int BG=Color.rgb(8,9,14), PANEL=Color.rgb(19,22,31), PANEL2=Color.rgb(30,33,44);
    private static final int PURPLE=Color.rgb(164,92,255), RED=Color.rgb(255,61,88), CYAN=Color.rgb(55,226,255);
    private static final int GOLD=Color.rgb(255,206,84), GREEN=Color.rgb(77,230,151), WHITE=Color.rgb(239,241,248), MUTED=Color.rgb(157,163,180);
    private static final int MIC_PERMISSION=2201, SAMPLE_PICK=3300;
    private static final String PREFS="fmefun_runtime", LAST_PROJECT="last_project";
    private final AudioEngine audio=new AudioEngine();
    private final Handler handler=new Handler(Looper.getMainLooper());
    private FmeFunProject project; private SessionRuntime runtime; private FrameLayout content; private TextView status,position; private Button loopButton;
    private boolean playing,pendingRecord; private int selectedTrack=0,selectedPad=0,drumLane=0,drumStep=0,octave=4;
    private final int[] loopChoices={4,8,16,24,38,48,-1};

    private final Runnable transportRunner=new Runnable(){@Override public void run(){
        if(!playing)return;
        long from=project.transport.tick(), stepTicks=UnifiedTransport.PPQ/4L, to=from+stepTicks;
        runtime.playRange(from,to,audio);
        if(project.transport.looping()&&to>=project.transport.loopEnd())project.transport.seek(project.transport.loopStart()); else project.transport.seek(to);
        updatePosition();
        long base=Math.max(20,Math.round(60000.0/project.transport.bpm()/4.0));
        int step=(int)(from/stepTicks); long swing=Math.round(base*(project.drums.swing()/100.0)*.5);
        handler.postDelayed(this,step%2==0?Math.max(10,base-swing):base+swing);
    }};

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);requestWindowFeature(Window.FEATURE_NO_TITLE);getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);hideBars();
        String last=getSharedPreferences(PREFS,MODE_PRIVATE).getString(LAST_PROJECT,"Untitled FME Fun");
        project=FmeFunStore.load(this,last); if(project.name==null||project.name.isEmpty())project.name=last;
        runtime=new SessionRuntime(this,project); setContentView(buildShell());showHome();updatePosition();
    }

    private void hideBars(){if(Build.VERSION.SDK_INT>=30){WindowInsetsController c=getWindow().getInsetsController();if(c!=null)c.hide(WindowInsets.Type.statusBars()|WindowInsets.Type.navigationBars());}else getWindow().getDecorView().setSystemUiVisibility(5894|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}

    private View buildShell(){
        LinearLayout root=column();root.setBackgroundColor(BG);
        LinearLayout header=row();header.setGravity(Gravity.CENTER_VERTICAL);header.setPadding(dp(8),dp(4),dp(8),dp(4));
        header.addView(text("DEMONIC AI STUDIO · .FMEFUN",17,WHITE,true));position=text("",12,CYAN,true);header.addView(position);header.addView(space(1));
        header.addView(btn("▶",GREEN,v->startTransport()));header.addView(btn("Ⅱ",GOLD,v->pauseTransport()));header.addView(btn("■",RED,v->stopTransport()));
        header.addView(btn("↶",PANEL2,v->undo()));header.addView(btn("↷",PANEL2,v->redo()));
        loopButton=btn(loopLabel(),PURPLE,v->{cycleLoop();});header.addView(loopButton);
        status=text("SESSION READY",11,GREEN,true);header.addView(status);
        root.addView(header,new LinearLayout.LayoutParams(-1,dp(54)));
        content=new FrameLayout(this);root.addView(content,new LinearLayout.LayoutParams(-1,0,1));
        HorizontalScrollView navScroll=new HorizontalScrollView(this);navScroll.setHorizontalScrollBarEnabled(false);LinearLayout nav=row();
        nav.addView(nav("HOME",v->showHome()));nav.addView(nav("TIMELINE",v->showTimeline()));nav.addView(nav("PIANO ROLL",v->showPiano()));
        nav.addView(nav("DRUM/TRACKER",v->showDrums()));nav.addView(nav("MIXER/BUS",v->showMixer()));nav.addView(nav("SAMPLER",v->showSampler()));
        nav.addView(nav("RECORD",v->showRecord()));nav.addView(nav("PROJECT/EXPORT",v->showProject()));navScroll.addView(nav);root.addView(navScroll,new LinearLayout.LayoutParams(-1,dp(58)));
        return root;
    }

    private void showHome(){
        LinearLayout p=page();p.addView(text("UNIFIED FME SESSION GRAPH",23,PURPLE,true));p.addView(text("Touch UI → command/history → transport/scheduler → MIDI, sampler, audio clips, mixer buses/sends → renderer → .fmefun",14,WHITE,false));
        LinearLayout r1=row();r1.addView(card("TIMELINE","Drag clips · pinch zoom · scroll",GREEN,v->showTimeline()));r1.addView(card("PIANO ROLL","Touch notes · MIDI engine",CYAN,v->showPiano()));r1.addView(card("DRUM/TRACKER","32 steps · 8 lanes · per-step data",RED,v->showDrums()));p.addView(r1);
        LinearLayout r2=row();r2.addView(card("MIXER","8 tracks · 4 buses · sends",PURPLE,v->showMixer()));r2.addView(card("SAMPLER","16 mapped pads · pitch/gain/pan",GOLD,v->showSampler()));r2.addView(card("CAPTURE","Arm · monitor · waveform clip",WHITE,v->showRecord()));p.addView(r2);
        setPage(scroll(p));
    }

    private void showTimeline(){
        LinearLayout p=page();LinearLayout bar=row();bar.addView(text("ARRANGEMENT",20,GREEN,true));bar.addView(space(1));bar.addView(btn("UNDO",PANEL2,v->undo()));bar.addView(btn("REDO",PANEL2,v->redo()));p.addView(bar,new LinearLayout.LayoutParams(-1,dp(44)));
        TimelineTouchView view=new TimelineTouchView(this,project);view.setListener(new TimelineTouchView.Listener(){
            public void moveClip(FmeFunProject.Clip c,long tick,int track){runtime.commands.moveClip(c,tick,track);autosave("CLIP MOVED");view.invalidate();}
            public void viewport(float zoom,float scroll){autosaveSilent();}
        });p.addView(view,new LinearLayout.LayoutParams(-1,0,1));p.addView(text("Drag clips vertically/horizontally. Pinch to zoom. Empty-space drag scrolls. Audio clips render waveform peaks.",12,MUTED,false));setPage(p);
    }

    private void showPiano(){
        LinearLayout p=page();LinearLayout top=row();top.addView(text("PIANO + MIDI",20,CYAN,true));top.addView(btn("OCT−",PANEL2,v->{octave=Math.max(2,octave-1);showPiano();}));top.addView(text("OCT "+octave,13,WHITE,true));top.addView(btn("OCT+",PANEL2,v->{octave=Math.min(6,octave+1);showPiano();}));top.addView(btn(project.mixer.channels[0].recordArm?"ARM ●":"ARM ○",project.mixer.channels[0].recordArm?RED:PANEL2,v->{runtime.commands.toggleArm(0);autosave("PIANO ARM");showPiano();}));p.addView(top,new LinearLayout.LayoutParams(-1,dp(46)));
        PianoKeyboardView keys=new PianoKeyboardView(this);keys.setOctave(octave);keys.setNoteListener((hz,name)->{int midi=(int)Math.round(69+12*Math.log(hz/440.0)/Math.log(2));runtime.triggerMidi(midi,110,0,audio);if(project.mixer.channels[0].recordArm){runtime.commands.addNote(project.transport.snap(project.transport.tick()),UnifiedTransport.PPQ,midi,110,0);autosave("MIDI NOTE "+name);}});
        p.addView(keys,new LinearLayout.LayoutParams(-1,dp(160)));
        HorizontalScrollView hs=new HorizontalScrollView(this);PianoRollTouchView roll=new PianoRollTouchView(this,project.midi,project.transport);roll.setMinimumWidth(dp(1800));roll.setEditor(new PianoRollTouchView.Editor(){public MidiClip.Note add(long t,long d,int pitch,int vel,int ch){MidiClip.Note n=runtime.commands.addNote(t,d,pitch,vel,ch);autosaveSilent();return n;}public void move(MidiClip.Note n,long t,int pitch){runtime.commands.moveNote(n,t,pitch);autosave("MIDI NOTE MOVED");}});hs.addView(roll,new HorizontalScrollView.LayoutParams(dp(1800),dp(650)));p.addView(hs,new LinearLayout.LayoutParams(-1,0,1));setPage(p);
    }

    private void showDrums(){
        LinearLayout p=page();LinearLayout top=row();top.addView(text("DRUM / TRACKER",20,RED,true));top.addView(space(1));top.addView(btn("SWING "+project.drums.swing()+"%",PANEL2,v->{project.drums.setSwing((project.drums.swing()+5)%80);autosave("SWING");showDrums();}));p.addView(top,new LinearLayout.LayoutParams(-1,dp(45)));
        DrumStepTouchView grid=new DrumStepTouchView(this,project.drums);grid.setEditor(new DrumStepTouchView.Editor(){public void toggle(int l,int s){runtime.commands.toggleDrum(l,s);autosaveSilent();}public void selected(int l,int s){drumLane=l;drumStep=s;showDrumInspector(p,l,s);}});p.addView(grid,new LinearLayout.LayoutParams(-1,0,1));showDrumInspector(p,drumLane,drumStep);setPage(p);
    }

    private void showDrumInspector(LinearLayout p,int lane,int step){
        if(p.getChildCount()>2)p.removeViews(2,p.getChildCount()-2);DrumPattern.Step s=project.drums.step(lane,step);LinearLayout box=row();box.setGravity(Gravity.CENTER_VERTICAL);box.addView(text("L"+(lane+1)+" S"+(step+1),13,WHITE,true));
        box.addView(text("VEL",10,MUTED,true));box.addView(simpleSeek(127,s.velocity,v->{s.velocity=Math.max(1,v);autosaveSilent();}),new LinearLayout.LayoutParams(dp(130),dp(44)));
        box.addView(text("PROB",10,MUTED,true));box.addView(simpleSeek(100,s.probability,v->{s.probability=v;autosaveSilent();}),new LinearLayout.LayoutParams(dp(130),dp(44)));
        box.addView(text("SHIFT",10,MUTED,true));box.addView(simpleSeek(240,s.microShiftTicks+120,v->{s.microShiftTicks=v-120;autosaveSilent();}),new LinearLayout.LayoutParams(dp(130),dp(44)));
        box.addView(text("REP",10,MUTED,true));box.addView(simpleSeek(8,Math.max(1,s.repeat),v->{s.repeat=Math.max(1,v);autosaveSilent();}),new LinearLayout.LayoutParams(dp(110),dp(44)));
        box.addView(text("PITCH",10,MUTED,true));box.addView(simpleSeek(48,Math.round(s.pitch)+24,v->{s.pitch=v-24;autosaveSilent();}),new LinearLayout.LayoutParams(dp(130),dp(44)));p.addView(box,new LinearLayout.LayoutParams(-1,dp(54)));
    }

    private void showMixer(){
        ScrollView sc=new ScrollView(this);LinearLayout p=page();p.addView(text("MIXER · ROUTING · SENDS",21,PURPLE,true));
        for(int i=0;i<project.mixer.channels.length;i++)p.addView(trackStrip(i));
        p.addView(text("BUSES",18,GOLD,true));for(int i=0;i<project.mixer.buses.length;i++)p.addView(busStrip(i));sc.addView(p);setPage(sc);
    }

    private View trackStrip(int i){
        MixerGraph.Channel ch=project.mixer.channels[i];LinearLayout r=row();r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(5),dp(3),dp(5),dp(3));r.setBackgroundColor(PANEL);
        r.addView(text("T"+(i+1),13,WHITE,true),new LinearLayout.LayoutParams(dp(42),-1));
        r.addView(text("GAIN",9,MUTED,true));r.addView(mixerSeek(200,Math.round(ch.gain*100),v->ch.gain=v/100f,v->runtime.commands.setGain(i,v/100f)),new LinearLayout.LayoutParams(dp(130),dp(42)));
        r.addView(text("PAN",9,MUTED,true));r.addView(mixerSeek(200,Math.round(ch.pan*100+100),v->ch.pan=(v-100)/100f,v->runtime.commands.setPan(i,(v-100)/100f)),new LinearLayout.LayoutParams(dp(130),dp(42)));
        r.addView(btn(ch.mute?"M●":"M",ch.mute?RED:PANEL2,v->{ch.mute=!ch.mute;autosave("MUTE");showMixer();}));r.addView(btn(ch.solo?"S●":"S",ch.solo?GOLD:PANEL2,v->{ch.solo=!ch.solo;autosave("SOLO");showMixer();}));
        r.addView(btn(ch.recordArm?"ARM●":"ARM",ch.recordArm?RED:PANEL2,v->{runtime.commands.toggleArm(i);autosave("ARM");showMixer();}));r.addView(btn(ch.monitor?"MON●":"MON",ch.monitor?GREEN:PANEL2,v->{runtime.commands.toggleMonitor(i);autosave("MONITOR");showMixer();}));
        r.addView(btn("BUS "+(ch.bus+1),PURPLE,v->{ch.bus=(ch.bus+1)%project.mixer.buses.length;autosave("ROUTE");showMixer();}));
        for(int b=0;b<project.mixer.buses.length;b++){final int bus=b;r.addView(simpleSeek(100,Math.round(ch.sends[b]*100),v->{ch.sends[bus]=v/100f;autosaveSilent();}),new LinearLayout.LayoutParams(dp(72),dp(40)));}
        return r;
    }

    private View busStrip(int i){MixerGraph.Channel b=project.mixer.buses[i];LinearLayout r=row();r.setGravity(Gravity.CENTER_VERTICAL);r.setBackgroundColor(PANEL2);r.addView(text("BUS "+(i+1),13,GOLD,true),new LinearLayout.LayoutParams(dp(70),-1));r.addView(text("GAIN",9,MUTED,true));r.addView(simpleSeek(200,Math.round(b.gain*100),v->{b.gain=v/100f;autosaveSilent();}),new LinearLayout.LayoutParams(dp(180),dp(42)));r.addView(text("PAN",9,MUTED,true));r.addView(simpleSeek(200,Math.round(b.pan*100+100),v->{b.pan=(v-100)/100f;autosaveSilent();}),new LinearLayout.LayoutParams(dp(180),dp(42)));r.addView(btn(b.mute?"MUTED":"MUTE",b.mute?RED:PANEL,v->{b.mute=!b.mute;autosave("BUS MUTE");showMixer();}));return r;}

    private void showSampler(){
        LinearLayout p=page();p.addView(text("16-PAD SAMPLER",21,GOLD,true));GridLayout g=new GridLayout(this);g.setColumnCount(8);
        for(int i=0;i<FmeFunProject.PADS;i++){final int pad=i;FmeFunProject.SampleMap m=project.sample(i);Button b=btn((i+1)+(m.source.isEmpty()?"":" ●"),i==selectedPad?GOLD:PANEL2,v->{selectedPad=pad;runtime.triggerPad(pad,1f);showSampler();});b.setOnLongClickListener(v->{selectedPad=pad;pickSample();return true;});g.addView(b,new GridLayout.LayoutParams(){ {width=dp(105);height=dp(68);setMargins(dp(3),dp(3),dp(3),dp(3));} });}p.addView(g);
        FmeFunProject.SampleMap m=project.sample(selectedPad);p.addView(text("PAD "+(selectedPad+1)+" · long-press a pad to map WAV/audio",13,WHITE,true));p.addView(text(m.source.isEmpty()?"UNMAPPED":new File(m.source).getName(),11,MUTED,false));
        LinearLayout ctl=row();ctl.addView(text("PITCH",10,MUTED,true));ctl.addView(simpleSeek(48,Math.round(m.pitch)+24,v->{m.pitch=v-24;runtime.sampler.setPitch(selectedPad,m.pitch);autosaveSilent();}),new LinearLayout.LayoutParams(dp(180),dp(44)));ctl.addView(text("GAIN",10,MUTED,true));ctl.addView(simpleSeek(200,Math.round(m.gain*100),v->{m.gain=v/100f;autosaveSilent();}),new LinearLayout.LayoutParams(dp(180),dp(44)));ctl.addView(text("PAN",10,MUTED,true));ctl.addView(simpleSeek(200,Math.round(m.pan*100+100),v->{m.pan=(v-100)/100f;autosaveSilent();}),new LinearLayout.LayoutParams(dp(180),dp(44)));p.addView(ctl);setPage(scroll(p));
    }

    private void showRecord(){
        LinearLayout p=page();p.setGravity(Gravity.CENTER);MixerGraph.Channel ch=project.mixer.channels[selectedTrack];p.addView(text("CAPTURE ENGINE",24,RED,true));p.addView(btn("TRACK "+(selectedTrack+1),PURPLE,v->{selectedTrack=(selectedTrack+1)%project.mixer.channels.length;showRecord();}));
        p.addView(btn(ch.recordArm?"ARMED ●":"ARM ○",ch.recordArm?RED:PANEL2,v->{runtime.commands.toggleArm(selectedTrack);autosave("ARM");showRecord();}));p.addView(btn(ch.monitor?"MONITOR ●":"MONITOR ○",ch.monitor?GREEN:PANEL2,v->{runtime.commands.toggleMonitor(selectedTrack);autosave("MONITOR");showRecord();}));
        p.addView(btn(runtime.recorder.isRunning()?"■ STOP + PLACE CLIP":"● RECORD WAV",runtime.recorder.isRunning()?GREEN:RED,v->toggleRecord()));p.addView(text("Captured audio is placed at record-start transport position and receives waveform metadata.",12,MUTED,false));setPage(p);
    }

    private void showProject(){
        LinearLayout p=page();p.addView(text("PROJECT / EXPORT",22,GREEN,true));EditText name=new EditText(this);name.setText(project.name);name.setTextColor(WHITE);name.setSingleLine(true);p.addView(name,new LinearLayout.LayoutParams(-1,dp(54)));
        p.addView(btn("SAVE NAME",GREEN,v->{String n=name.getText().toString().trim();if(!n.isEmpty())project.name=n;autosave("PROJECT SAVED");}));
        LinearLayout tempo=row();tempo.addView(text("BPM "+Math.round(project.transport.bpm()),13,CYAN,true));tempo.addView(simpleSeek(270,(int)Math.round(project.transport.bpm()-30),v->{project.transport.setBpm(v+30);updatePosition();autosaveSilent();}),new LinearLayout.LayoutParams(dp(300),dp(44)));tempo.addView(btn(loopLabel(),PURPLE,v->cycleLoop()));p.addView(tempo);
        LinearLayout loops=row();loops.addView(text("LOOP BARS",11,MUTED,true));
        for(int bars:loopChoices){final int choice=bars;loops.addView(btn(choice<0?"UNLIMITED":String.valueOf(choice),project.transport.loopBars()==choice?PURPLE:PANEL2,v->setLoopChoice(choice)));}p.addView(loops);
        p.addView(btn("EXPORT MASTER WAV",CYAN,v->export(false)));p.addView(btn("EXPORT MASTER + TRACK/BUS STEMS",PURPLE,v->export(true)));p.addView(btn("UNDO "+(runtime.commands.history.canUndo()?"●":"○"),PANEL2,v->undo()));p.addView(btn("REDO "+(runtime.commands.history.canRedo()?"●":"○"),PANEL2,v->redo()));
        p.addView(text(summary(),13,WHITE,false));setPage(scroll(p));
    }

    private void startTransport(){if(playing)return;playing=true;handler.removeCallbacks(transportRunner);handler.post(transportRunner);status.setText("PLAYING");}
    private void pauseTransport(){playing=false;handler.removeCallbacks(transportRunner);runtime.stopPlayback();status.setText("PAUSED");autosaveSilent();}
    private void stopTransport(){playing=false;handler.removeCallbacks(transportRunner);runtime.stopPlayback();project.transport.seek(0);updatePosition();status.setText("STOPPED");autosaveSilent();}
    private void cycleLoop(){int cur=project.transport.loopBars(),idx=0;for(int i=0;i<loopChoices.length;i++)if(loopChoices[i]==cur)idx=i;setLoopChoice(loopChoices[(idx+1)%loopChoices.length]);}
    private void setLoopChoice(int bars){project.transport.setLoopBars(bars);loopButton.setText(loopLabel());autosave("LOOP "+loopLabel());showProject();}
    private String loopLabel(){return project.transport.looping()?("LOOP "+project.transport.loopBars()+" BAR"):"LOOP UNLIMITED";}
    private void updatePosition(){long t=project.transport.tick(),bar=t/project.transport.ticksPerBar()+1,beat=(t%project.transport.ticksPerBar())/UnifiedTransport.PPQ+1;position.setText(String.format(Locale.US," %d:%d · %.0f BPM ",bar,beat,project.transport.bpm()));}

    private void toggleRecord(){if(runtime.recorder.isRunning()){FmeFunProject.Clip c=runtime.stopCapture(selectedTrack);if(c!=null)autosave("TAKE PLACED");else status.setText("CAPTURE FAILED");showRecord();return;}if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){pendingRecord=true;requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},MIC_PERMISSION);return;}beginRecord();}
    private void beginRecord(){try{File d=new File(getExternalFilesDir(android.os.Environment.DIRECTORY_MUSIC),"FMEFunTakes");if(!d.exists())d.mkdirs();File f=new File(d,"Take_"+System.currentTimeMillis()+".wav");if(runtime.startCapture(f,selectedTrack)){status.setText("RECORDING T"+(selectedTrack+1));showRecord();}else status.setText("ARM TRACK FIRST");}catch(Exception e){status.setText("RECORD FAILED");}}
    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==MIC_PERMISSION&&pendingRecord){pendingRecord=false;if(g.length>0&&g[0]==PackageManager.PERMISSION_GRANTED)beginRecord();}}

    private void pickSample(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("audio/*");i.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"audio/wav","audio/x-wav","audio/wave"});startActivityForResult(i,SAMPLE_PICK);}
    @Override protected void onActivityResult(int r,int code,Intent data){super.onActivityResult(r,code,data);if(r==SAMPLE_PICK&&code==RESULT_OK&&data!=null&&data.getData()!=null){try{Uri u=data.getData();File dir=new File(getFilesDir(),"samples");if(!dir.exists())dir.mkdirs();File out=new File(dir,"pad_"+selectedPad+"_"+System.currentTimeMillis()+".wav");try(InputStream in=getContentResolver().openInputStream(u);FileOutputStream fos=new FileOutputStream(out)){byte[] buf=new byte[32768];int n;while(in!=null&&(n=in.read(buf))>0)fos.write(buf,0,n);}runtime.mapSample(selectedPad,out.getAbsolutePath());autosave("PAD "+(selectedPad+1)+" MAPPED");showSampler();}catch(Exception e){status.setText("SAMPLE IMPORT FAILED");}}}

    private void export(boolean stems){status.setText("RENDERING…");new Thread(()->{try{FmeFunRenderer.ExportResult r=runtime.renderer.export(this,project,stems);runOnUiThread(()->status.setText("EXPORTED "+r.master.getName()+" · "+r.stems.size()+" STEMS"));}catch(Exception e){runOnUiThread(()->status.setText("EXPORT FAILED · "+e.getClass().getSimpleName()));}},"fmefun-export").start();}

    private void undo(){String s=runtime.commands.history.undo();if(!s.isEmpty()){autosave("UNDO · "+s);showHome();}}
    private void redo(){String s=runtime.commands.history.redo();if(!s.isEmpty()){autosave("REDO · "+s);showHome();}}
    private void autosave(String msg){try{runtime.save();getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString(LAST_PROJECT,project.name).apply();status.setText(msg);}catch(Exception e){status.setText("SAVE FAILED");}}
    private void autosaveSilent(){try{runtime.save();getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString(LAST_PROJECT,project.name).apply();}catch(Exception ignored){}}
    private String summary(){return "Format v"+FmeFunProject.VERSION+" · "+project.clips.size()+" clips · "+project.midi.notes.size()+" MIDI notes · "+project.midi.cc.size()+" CC · "+project.samples.size()+" sample mappings · 8 tracks · 4 buses · "+loopLabel();}

    private interface IntApply{void set(int v);}
    private SeekBar simpleSeek(int max,int value,IntApply a){SeekBar s=new SeekBar(this);s.setMax(max);s.setProgress(Math.max(0,Math.min(max,value)));s.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int v,boolean user){if(user)a.set(v);}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}});return s;}
    private SeekBar mixerSeek(int max,int value,IntApply preview,IntApply commit){SeekBar s=new SeekBar(this);s.setMax(max);s.setProgress(value);s.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){int start=value;public void onStartTrackingTouch(SeekBar b){start=b.getProgress();}public void onProgressChanged(SeekBar b,int v,boolean user){if(user)preview.set(v);}public void onStopTrackingTouch(SeekBar b){int end=b.getProgress();preview.set(start);commit.set(end);autosaveSilent();}});return s;}

    private LinearLayout page(){LinearLayout l=column();l.setPadding(dp(10),dp(7),dp(10),dp(7));return l;}
    private ScrollView scroll(View v){ScrollView s=new ScrollView(this);s.addView(v);return s;}
    private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);return l;}
    private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private Space space(float weight){Space s=new Space(this);s.setLayoutParams(new LinearLayout.LayoutParams(0,1,weight));return s;}
    private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setGravity(Gravity.CENTER_VERTICAL);if(bold)t.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);t.setPadding(dp(6),dp(3),dp(6),dp(3));return t;}
    private Button btn(String s,int color,View.OnClickListener l){Button b=new Button(this);b.setText(s);b.setTextColor(WHITE);b.setTextSize(11);b.setAllCaps(false);b.setBackgroundColor(color);b.setPadding(dp(7),0,dp(7),0);b.setOnClickListener(l);return b;}
    private Button nav(String s,View.OnClickListener l){Button b=btn(s,PANEL2,l);b.setLayoutParams(new LinearLayout.LayoutParams(dp(145),dp(50)));return b;}
    private View card(String title,String sub,int color,View.OnClickListener l){LinearLayout c=column();c.setPadding(dp(10),dp(8),dp(10),dp(8));c.setBackgroundColor(PANEL);c.addView(text(title,17,color,true));c.addView(text(sub,11,MUTED,false));c.setOnClickListener(l);c.setLayoutParams(new LinearLayout.LayoutParams(0,dp(105),1));return c;}
    private void setPage(View v){content.removeAllViews();content.addView(v,new FrameLayout.LayoutParams(-1,-1));}
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}

    @Override protected void onPause(){super.onPause();autosaveSilent();}
    @Override protected void onDestroy(){playing=false;handler.removeCallbacksAndMessages(null);autosaveSilent();if(runtime!=null)runtime.close();super.onDestroy();}
}