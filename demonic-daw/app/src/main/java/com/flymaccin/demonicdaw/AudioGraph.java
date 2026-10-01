package com.flymaccin.demonicdaw;
import org.json.*;
import java.util.*;
public final class AudioGraph {
  public enum Kind{SOURCE,INSTRUMENT,SAMPLER,TRACK,BUS,MASTER,OUTPUT}
  public static final class Node{public final String id;public final Kind kind;public final float gain,pan;public final boolean mute,solo;Node(String i,Kind k,float g,float p,boolean m,boolean s){id=i;kind=k;gain=g;pan=p;mute=m;solo=s;}}
  public static final class Edge{public final String from,to;Edge(String f,String t){from=f;to=t;}}
  public final Map<String,Node> nodes;public final List<Edge> edges;public final List<Node> processOrder;
  private AudioGraph(Map<String,Node>n,List<Edge>e){nodes=Collections.unmodifiableMap(n);edges=Collections.unmodifiableList(e);processOrder=Collections.unmodifiableList(sort(n,e));}
  public static AudioGraph compile(JSONObject project)throws Exception{
    NativeProjectState.validate(NativeProjectState.normalize(project));
    LinkedHashMap<String,Node>n=new LinkedHashMap<>();ArrayList<Edge>e=new ArrayList<>();
    put(n,new Node("master",Kind.MASTER,1,0,false,false));put(n,new Node("output",Kind.OUTPUT,1,0,false,false));e.add(new Edge("master","output"));
    JSONArray buses=project.optJSONArray("buses");if(buses!=null)for(int i=0;i<buses.length();i++){JSONObject b=buses.getJSONObject(i);String id=b.getString("id");put(n,new Node(id,Kind.BUS,(float)b.optDouble("gain",1),(float)b.optDouble("pan",0),b.optBoolean("mute"),b.optBoolean("solo")));}
    JSONArray ts=project.getJSONArray("tracks");for(int i=0;i<ts.length();i++){JSONObject t=ts.getJSONObject(i);String id=t.getString("id");put(n,new Node(id,Kind.TRACK,(float)t.optDouble("gain",1),(float)t.optDouble("pan",0),t.optBoolean("mute"),t.optBoolean("solo")));}
    for(int i=0;i<ts.length();i++){JSONObject t=ts.getJSONObject(i);String to=t.optString("routeId","master");if(to.isEmpty())to="master";e.add(new Edge(t.getString("id"),to));}
    if(buses!=null)for(int i=0;i<buses.length();i++){JSONObject b=buses.getJSONObject(i);String to=b.optString("routeId","master");if(to.isEmpty())to="master";e.add(new Edge(b.getString("id"),to));}
    for(Edge x:e)if(!n.containsKey(x.from)||!n.containsKey(x.to))throw new IllegalStateException("GRAPH_DANGLING_ROUTE");
    return new AudioGraph(n,e);
  }
  private static void put(Map<String,Node>n,Node x){if(n.put(x.id,x)!=null)throw new IllegalStateException("GRAPH_DUPLICATE_NODE");}
  private static List<Node> sort(Map<String,Node>n,List<Edge>e){Map<String,Integer>d=new LinkedHashMap<>();Map<String,List<String>>o=new HashMap<>();for(String id:n.keySet()){d.put(id,0);o.put(id,new ArrayList<>());}for(Edge x:e){d.put(x.to,d.get(x.to)+1);o.get(x.from).add(x.to);}ArrayDeque<String>q=new ArrayDeque<>();for(String id:d.keySet())if(d.get(id)==0)q.add(id);ArrayList<Node>r=new ArrayList<>();while(!q.isEmpty()){String id=q.remove();r.add(n.get(id));for(String to:o.get(id)){int v=d.get(to)-1;d.put(to,v);if(v==0)q.add(to);}}if(r.size()!=n.size())throw new IllegalStateException("GRAPH_CYCLE");return r;}
}
