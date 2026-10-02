package com.flymaccin.demonicaistudio;
import java.util.*;
final class CommandStack {
 interface Command { void apply(); void revert(); String label(); }
 private final Deque<Command> undo=new ArrayDeque<>(),redo=new ArrayDeque<>();
 void execute(Command c){c.apply();undo.push(c);redo.clear();}
 boolean canUndo(){return !undo.isEmpty();} boolean canRedo(){return !redo.isEmpty();}
 String undo(){if(undo.isEmpty())return "";Command c=undo.pop();c.revert();redo.push(c);return c.label();}
 String redo(){if(redo.isEmpty())return "";Command c=redo.pop();c.apply();undo.push(c);return c.label();}
 void clear(){undo.clear();redo.clear();}
}