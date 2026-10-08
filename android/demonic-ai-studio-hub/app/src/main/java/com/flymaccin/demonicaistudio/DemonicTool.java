package com.flymaccin.demonicaistudio;

import android.content.Context;
import android.net.Uri;
import java.util.Set;

public interface DemonicTool {
    String id();
    String name();
    String version();
    Set<String> capabilities();
    boolean accepts(Uri uri, String mimeType);
    ToolResult execute(Context context, ToolRequest request) throws Exception;

    final class ToolRequest {
        public final Uri uri;
        public final String mimeType;
        public final String action;
        public ToolRequest(Uri uri,String mimeType,String action){this.uri=uri;this.mimeType=mimeType;this.action=action;}
    }
    final class ToolResult {
        public final boolean ok; public final String message;
        private ToolResult(boolean ok,String message){this.ok=ok;this.message=message;}
        public static ToolResult ok(String m){return new ToolResult(true,m);}
        public static ToolResult error(String m){return new ToolResult(false,m);}
    }
}
