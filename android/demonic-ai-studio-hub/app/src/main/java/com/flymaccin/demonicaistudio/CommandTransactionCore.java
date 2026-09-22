package com.flymaccin.demonicaistudio;

import org.json.JSONObject;
import java.util.*;

final class CommandTransactionCore {
    interface Mutation { void apply(CanonicalProjectState state) throws Exception; }
    static final class Result { final long revision; final String transactionId; Result(long r,String id){revision=r;transactionId=id;} }
    private CanonicalProjectState state;
    private final ArrayDeque<String> undo=new ArrayDeque<>(), redo=new ArrayDeque<>();
    CommandTransactionCore(CanonicalProjectState state){this.state=state;}

    synchronized Result execute(long expectedRevision,String transactionId,Mutation mutation)throws Exception{
        if(state.revision!=expectedRevision)throw new IllegalStateException("revision conflict");
        String before=state.toJson().toString(); long beforeRevision=state.revision;
        try{mutation.apply(state);state.validate();if(state.revision==beforeRevision)state.revision++;undo.push(before);redo.clear();return new Result(state.revision,transactionId);}
        catch(Exception e){state=CanonicalProjectState.fromJson(new JSONObject(before));throw e;}
    }
    synchronized CanonicalProjectState undo() { if(undo.isEmpty())return state;redo.push(state.toJson().toString());state=CanonicalProjectState.fromJson(new JSONObject(undo.pop()));state.revision++;return state; }
    synchronized CanonicalProjectState redo() { if(redo.isEmpty())return state;undo.push(state.toJson().toString());state=CanonicalProjectState.fromJson(new JSONObject(redo.pop()));state.revision++;return state; }
    synchronized CanonicalProjectState state(){return state;}
}
