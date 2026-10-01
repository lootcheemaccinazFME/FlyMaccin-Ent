package com.flymaccin.demonicaistudio;
import java.util.*;
public final class FunRoomRegistry {
 public static final List<FunRoom> ROOMS=Collections.unmodifiableList(Arrays.asList(
  new FunRoom("studio","Demonic Studio","NATIVE","Piano • guitar • drums • recording • projects • library"),
  new FunRoom("daw","Demonic DAW","NATIVE ENGINE","Timeline • mixer • SFZ • render • sessions"),
  new FunRoom("daw2","Demonic DAW 2","NATIVE ENGINE","Take recorder • drums • pattern renderer • project sessions"),
  new FunRoom("emulator","FME Universal Emulator","NATIVE ENGINE","16 libretro cores • ROM import • controller • save states"),
  new FunRoom("8bit","8-Bit Ism","GAME ROOM","Arcade/player game room"),
  new FunRoom("hyphyxels","HY-PHYXELS: Arcade Invasion","GAME ROOM","Pixel invasion arcade room"),
  new FunRoom("carnival","Bay Carnival3 Parade","GAME ROOM","Bay Carnival parade game room"),
  new FunRoom("fmegames","FME Games / HY-PHYXELS","GAME ROOM","FME game collection"),
  new FunRoom("agent","FME Agent","NATIVE SERVICE","Agent engineering and PS5 integration room"),
  new FunRoom("potna","Pocket Potna","NATIVE CLIENT","Pocket Potna assistant room"),
  new FunRoom("remote","Pocket Potna Remote","CONTROL ROOM","Pairing • PS5 • browser • downloads • apps • files • cloud • settings"),
  new FunRoom("tv","Demonic TV","MEDIA ROOM","TV • local video • browser • PS5 integration"),
  new FunRoom("video","FME AI Video Generator","AI ROOM","Prompt • references • LTX video generation • preview"),
  new FunRoom("octop","Octop FME","CONTROL PLANE","Agents • memory • knowledge • plugins • automation"),
  new FunRoom("bayauto","Bay Auto RP","VEHICLE ROOM","Bay Auto roleplay experience")
 ));
 private FunRoomRegistry(){}
}
