package com.flymaccin.demonicaistudio;

import java.util.*;

public final class DemonicExtensionPolicy {
    private static final Set<String> ALLOWED=new HashSet<>(Arrays.asList(
        "download","import.sample","import.instrument","media","sfz","sf2","shortcut","stream","library","production"));
    public static boolean validate(DemonicExtensionManifest m){
        if(m==null||m.apiVersion!=1)return false;
        for(String c:m.capabilities)if(!ALLOWED.contains(c))return false;
        return true;
    }
    private DemonicExtensionPolicy(){}
}
