package com.flymaccin.lootcheerom;
import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class DreyvenBeatLabActivity extends Activity {
  private final boolean[][] steps = new boolean[4][16];
  private final String[] names = {"Kick", "Snare", "Hat", "Clap"};
  private final Button[][] pads = new Button[4][16];
  private final Handler handler = new Handler(Looper.getMainLooper());
  private final ExecutorService audio = Executors.newSingleThreadExecutor();
  private boolean playing = false;
  private int step = 0, bpm = 100;
  private TextView status;
  private final Runnable tick = new Runnable() {
    @Override public void run() {
      if (!playing) return;
      final int current = step;
      for(int i=0;i<4;i++) {
        if(steps[i][current]) { final int voice=i; audio.execute(() -> tone(voice)); }
      }
      step=(step+1)%16;
      status.setText("BPM "+bpm+"   Step "+(current+1)+"/16");
      handler.postDelayed(this, Math.max(50, 60000L/bpm/4));
    }
  };
  @Override public void onCreate(Bundle b) {
    super.onCreate(b);
    ScrollView sc=new ScrollView(this); LinearLayout root=new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);root.setPadding(12,22,12,22);root.setBackgroundColor(0xff101526);sc.addView(root);
    TextView title=new TextView(this);title.setText("DREYVEN BEAT LAB\n16-Step Drum Sequencer");
    title.setTextColor(0xffffd166);title.setTextSize(24);title.setGravity(Gravity.CENTER);root.addView(title);
    status=new TextView(this);status.setTextColor(Color.WHITE);status.setText("BPM 100");
    status.setTextSize(17);root.addView(status);
    SeekBar tempo=new SeekBar(this);tempo.setMax(160);tempo.setProgress(40);root.addView(tempo);
    tempo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
      public void onProgressChanged(SeekBar s,int p,boolean user){bpm=60+p;status.setText("BPM "+bpm);}
      public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}
    });
    for(int row=0;row<4;row++){
      TextView label=new TextView(this);label.setText(names[row]);label.setTextColor(Color.WHITE);root.addView(label);
      HorizontalScrollView hs=new HorizontalScrollView(this);LinearLayout strip=new LinearLayout(this);
      for(int col=0;col<16;col++){
        final int r=row,c=col;Button pad=new Button(this);
        pad.setText("·");pad.setMinWidth(76);pad.setMinimumWidth(76);
        strip.addView(pad,new LinearLayout.LayoutParams(76,88));pads[row][col]=pad;
        pad.setOnClickListener(v->{steps[r][c]=!steps[r][c];pad.setText(steps[r][c]?"●":"·");});
      }hs.addView(strip);root.addView(hs);
    }
    Button play=new Button(this);play.setText("PLAY / STOP");root.addView(play);
    play.setOnClickListener(v->{playing=!playing;handler.removeCallbacks(tick);if(playing)handler.post(tick);});
    Button demo=new Button(this);demo.setText("LOAD DEMO BEAT");root.addView(demo);
    demo.setOnClickListener(v->{for(int r=0;r<4;r++)for(int c=0;c<16;c++){steps[r][c]=r==0?(c%4==0):r==1?(c==4||c==12):r==2?(c%2==0):(c==12);pads[r][c].setText(steps[r][c]?"●":"·");}});
    Button clear=new Button(this);clear.setText("CLEAR");root.addView(clear);
    clear.setOnClickListener(v->{for(int r=0;r<4;r++)for(int c=0;c<16;c++){steps[r][c]=false;pads[r][c].setText("·");}});
    setContentView(sc);
  }
  private void tone(int voice){
    final int sr=22050,n=voice==0?4000:voice==1?2600:voice==2?1100:2200;
    short[] pcm=new short[n];double phase=0;
    for(int i=0;i<n;i++){
      double t=i/(double)sr,env=Math.pow(1.0-i/(double)n,voice==2?3:2);
      double f=voice==0?130-90*(i/(double)n):voice==1?190:voice==2?7000:900;
      phase+=2*Math.PI*f/sr;
      double noise=Math.sin(i*12.9898+voice*78.233)*43758.5453;noise=2*(noise-Math.floor(noise))-1;
      double wave=voice==0?Math.sin(phase):voice==1?0.35*Math.sin(phase)+0.65*noise:noise;
      pcm[i]=(short)(Math.max(-1,Math.min(1,wave*env*0.45))*32767);
    }
    AudioTrack track=null;
    try{
      int size=n*2;
      track=new AudioTrack.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
      .setAudioFormat(new AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(sr).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
      .setTransferMode(AudioTrack.MODE_STATIC).setBufferSizeInBytes(size).build();
      track.write(pcm,0,n);track.play();Thread.sleep(Math.max(100,n*1000L/sr+30));
    }catch(Exception ignored){}finally{if(track!=null){try{track.stop();}catch(Exception ignored){}track.release();}}
  }
  @Override protected void onDestroy(){playing=false;handler.removeCallbacks(tick);audio.shutdownNow();super.onDestroy();}
}
