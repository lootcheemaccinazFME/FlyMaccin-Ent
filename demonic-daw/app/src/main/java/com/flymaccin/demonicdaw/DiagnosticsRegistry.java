package com.flymaccin.demonicdaw;
import org.json.*;
import java.util.concurrent.atomic.*;
public final class DiagnosticsRegistry {
 private final AtomicLong graphEpoch=new AtomicLong(),underruns=new AtomicLong(),recoveryCount=new AtomicLong(),rejectedTransactions=new AtomicLong();
 public void graphPublished(long e){graphEpoch.set(e);}public void underrun(){underruns.incrementAndGet();}public void recovered(){recoveryCount.incrementAndGet();}public void rejectedTransaction(){rejectedTransactions.incrementAndGet();}
 public JSONObject snapshot(){return new JSONObject().put("graphEpoch",graphEpoch.get()).put("underruns",underruns.get()).put("recoveries",recoveryCount.get()).put("rejectedTransactions",rejectedTransactions.get());}
}