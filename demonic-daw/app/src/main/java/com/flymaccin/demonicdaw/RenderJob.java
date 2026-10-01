package com.flymaccin.demonicdaw;
import org.json.JSONObject;
import java.util.UUID;
public final class RenderJob {
 public enum State{QUEUED,RUNNING,COMPLETED,FAILED,CANCELLED}
 public final String id=UUID.randomUUID().toString(),projectId,format;public final double startBeat,endBeat;private volatile State state=State.QUEUED;private volatile double progress;
 public RenderJob(String projectId,String format,double startBeat,double endBeat){this.projectId=projectId;this.format=format;this.startBeat=Math.max(0,startBeat);this.endBeat=Math.max(this.startBeat,endBeat);}
 public synchronized void start(){if(state!=State.QUEUED)throw new IllegalStateException("JOB_STATE");state=State.RUNNING;}public synchronized void progress(double p){if(state==State.RUNNING)progress=Math.max(0,Math.min(1,p));}public synchronized void complete(){if(state!=State.RUNNING)throw new IllegalStateException("JOB_STATE");progress=1;state=State.COMPLETED;}public synchronized void cancel(){if(state==State.QUEUED||state==State.RUNNING)state=State.CANCELLED;}
 public JSONObject json(){return new JSONObject().put("id",id).put("projectId",projectId).put("format",format).put("startBeat",startBeat).put("endBeat",endBeat).put("state",state.name()).put("progress",progress);}
}