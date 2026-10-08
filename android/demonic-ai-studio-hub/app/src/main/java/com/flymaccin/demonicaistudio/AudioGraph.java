package com.flymaccin.demonicaistudio;

import java.util.*;

final class AudioGraph {
    enum Kind { SOURCE, INSTRUMENT, SAMPLER, TRACK, BUS, MASTER, OUTPUT }
    static final class Node {
        final String id; final Kind kind; final float gain,pan; final boolean mute,solo;
        Node(String id,Kind kind,float gain,float pan,boolean mute,boolean solo){this.id=id;this.kind=kind;this.gain=gain;this.pan=pan;this.mute=mute;this.solo=solo;}
    }
    static final class Edge { final String from,to; Edge(String from,String to){this.from=from;this.to=to;} }
    final Map<String,Node> nodes; final List<Edge> edges;
    AudioGraph(Collection<Node> nodes,Collection<Edge> edges){
        LinkedHashMap<String,Node> n=new LinkedHashMap<>();for(Node x:nodes){if(n.put(x.id,x)!=null)throw new IllegalArgumentException("duplicate node");}
        this.nodes=Collections.unmodifiableMap(n);this.edges=Collections.unmodifiableList(new ArrayList<>(edges));validate();
    }
    void validate(){
        for(Edge e:edges)if(!nodes.containsKey(e.from)||!nodes.containsKey(e.to))throw new IllegalStateException("dangling edge");
        topologicalOrder();
    }
    List<Node> topologicalOrder(){
        Map<String,Integer> degree=new LinkedHashMap<>();Map<String,List<String>> out=new HashMap<>();
        for(String id:nodes.keySet()){degree.put(id,0);out.put(id,new ArrayList<>());}
        for(Edge e:edges){degree.put(e.to,degree.get(e.to)+1);out.get(e.from).add(e.to);}
        ArrayDeque<String> q=new ArrayDeque<>();for(Map.Entry<String,Integer>x:degree.entrySet())if(x.getValue()==0)q.add(x.getKey());
        ArrayList<Node> order=new ArrayList<>();while(!q.isEmpty()){String id=q.remove();order.add(nodes.get(id));for(String to:out.get(id)){int d=degree.get(to)-1;degree.put(to,d);if(d==0)q.add(to);}}
        if(order.size()!=nodes.size())throw new IllegalStateException("audio graph cycle");return Collections.unmodifiableList(order);
    }
    static AudioGraph fromProject(CanonicalProjectState p){
        ArrayList<Node> n=new ArrayList<>();ArrayList<Edge> e=new ArrayList<>();
        n.add(new Node("master",Kind.MASTER,1,0,false,false));n.add(new Node("output",Kind.OUTPUT,1,0,false,false));e.add(new Edge("master","output"));
        for(CanonicalProjectState.Track t:p.tracks.values()){n.add(new Node(t.id,Kind.TRACK,t.gain,t.pan,t.mute,t.solo));e.add(new Edge(t.id,t.routeId==null?"master":t.routeId));}
        return new AudioGraph(n,e);
    }
}
