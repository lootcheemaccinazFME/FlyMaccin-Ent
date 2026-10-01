package com.flymaccin.demonicaistudio;

import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

final class MaestroBridge {
    interface Callback { void done(JSONObject json, Exception error); }
    private String baseUrl;
    MaestroBridge(String baseUrl){ setBaseUrl(baseUrl); }
    void setBaseUrl(String value){ String v=value==null?"":value.trim(); while(v.endsWith("/"))v=v.substring(0,v.length()-1); baseUrl=v; }
    String getBaseUrl(){ return baseUrl; }

    void models(Callback cb){ request("GET","/api/v1/models",null,cb); }
    void status(String jobId, Callback cb){ request("GET","/api/v1/status/"+UriEncoder.encode(jobId),null,cb); }
    void generate(String prompt,String model,String resolution,int seconds,Callback cb){
        try{
            JSONObject body=new JSONObject();
            body.put("prompt",prompt);
            if(model!=null&&!model.isEmpty())body.put("model_type",model);
            body.put("resolution",resolution);
            body.put("duration_seconds",seconds);
            body.put("_queue_mode","now");
            request("POST","/api/v1/generate",body,cb);
        }catch(Exception e){ cb.done(null,e); }
    }
    private void request(String method,String path,JSONObject body,Callback cb){
        new Thread(()->{
            HttpURLConnection c=null;
            try{
                if(baseUrl.isEmpty())throw new IOException("Set Maestro server address first");
                c=(HttpURLConnection)new URL(baseUrl+path).openConnection();
                c.setRequestMethod(method); c.setConnectTimeout(10000); c.setReadTimeout(30000);
                c.setRequestProperty("Accept","application/json");
                if(body!=null){c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");try(OutputStream o=c.getOutputStream()){o.write(body.toString().getBytes(StandardCharsets.UTF_8));}}
                int code=c.getResponseCode(); InputStream in=code<400?c.getInputStream():c.getErrorStream();
                String text=read(in); if(code>=400)throw new IOException("Maestro HTTP "+code+": "+text);
                cb.done(new JSONObject(text),null);
            }catch(Exception e){cb.done(null,e);}finally{if(c!=null)c.disconnect();}
        },"MaestroBridge").start();
    }
    private static String read(InputStream in)throws IOException{if(in==null)return "{}";ByteArrayOutputStream o=new ByteArrayOutputStream();byte[] b=new byte[8192];for(int n;(n=in.read(b))>0;)o.write(b,0,n);return o.toString("UTF-8");}
    private static final class UriEncoder{static String encode(String s){try{return URLEncoder.encode(s,"UTF-8");}catch(Exception e){return s;}}}
}
