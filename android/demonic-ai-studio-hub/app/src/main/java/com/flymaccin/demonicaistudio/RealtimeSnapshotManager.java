package com.flymaccin.demonicaistudio;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

final class RealtimeSnapshotManager {
    static final class Snapshot {
        final long epoch,projectRevision; final List<AudioGraph.Node> processOrder; final List<AudioGraph.Edge> edges;
        Snapshot(long epoch,long revision,AudioGraph graph){this.epoch=epoch;this.projectRevision=revision;this.processOrder=graph.topologicalOrder();this.edges=graph.edges;}
    }
    private final AtomicLong epochs=new AtomicLong();
    private final AtomicReference<Snapshot> current=new AtomicReference<>();
    Snapshot compile(CanonicalProjectState state){state.validate();return new Snapshot(epochs.incrementAndGet(),state.revision,AudioGraph.fromProject(state));}
    Snapshot publish(Snapshot snapshot){current.set(snapshot);return snapshot;}
    Snapshot compileAndPublish(CanonicalProjectState state){return publish(compile(state));}
    Snapshot acquireForAudioCallback(){return current.get();}
}
