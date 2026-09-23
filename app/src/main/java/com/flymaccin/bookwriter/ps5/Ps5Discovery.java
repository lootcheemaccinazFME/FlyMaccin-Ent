package com.flymaccin.bookwriter.ps5;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
public final class Ps5Discovery {
  public static final int PORT=9302;
  public static List<String> scan(int timeoutMs) throws Exception {
    String request="SRCH * HTTP/1.1\n";
    byte[] data=request.getBytes(StandardCharsets.US_ASCII);
    DatagramSocket s=new DatagramSocket(); s.setBroadcast(true); s.setSoTimeout(Math.max(250,timeoutMs));
    s.send(new DatagramPacket(data,data.length,InetAddress.getByName("255.255.255.255"),PORT));
    List<String> out=new ArrayList<>(); long end=System.currentTimeMillis()+timeoutMs;
    while(System.currentTimeMillis()<end){
      byte[] buf=new byte[2048]; DatagramPacket p=new DatagramPacket(buf,buf.length);
      try{s.receive(p);String r=new String(p.getData(),0,p.getLength(),StandardCharsets.UTF_8);out.add(p.getAddress().getHostAddress()+"\n"+r);}
      catch(SocketTimeoutException e){break;}
    } s.close(); return out;
  }
}
