package com.flymaccin.demonicdaw;
import org.json.JSONObject;
import java.util.*;
import java.util.concurrent.atomic.*;
public final class RealtimeSnapshotManager {
  public static final class Snapshot{
    public final long epoch,projectRevision;public final List<AudioGraph.Node>processOrder;public final List<AudioGraph.Edge>edges;
    Snapshot(long e,long r,AudioGraph g){epoch=e;projectRevision=r;processOrder=g.processOrder;edges=g.edges;}
  }
  private final AtomicLong epoch=new AtomicLong();private final AtomicReference<Snapshot>active=new AtomicReference<>();
  public Snapshot compile(JSONObject canonical)throws Exception{AudioGraph g=AudioGraph.compile(new JSONObject(canonical.toString()));return new Snapshot(epoch.incrementAndGet(),canonical.optLong("revision",0),g);}
  public void publish(Snapshot ready){if(ready==null)throw new IllegalArgumentException("snapshot");active.set(ready);}
  public Snapshot compileAndPublish(JSONObject canonical)throws Exception{Snapshot s=compile(canonical);publish(s);return s;}
  public Snapshot acquireForRealtime(){return active.get();}
}
