package com.flymaccin.demonicaistudio;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/** Kept as a compatibility entry point; the studio itself is the sole application UI. */
public final class MainActivity extends Activity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        startActivity(new Intent(this, StudioActivity.class));
        finish();
    }
}
