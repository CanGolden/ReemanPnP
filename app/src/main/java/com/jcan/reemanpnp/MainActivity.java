package com.jcan.reemanpnp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.jcan.reemanpnp.forcamera.CameraAty;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onButton(View v) {
        switch (v.getId()) {
            case R.id.button:
                startActivity(new Intent(this, CameraAty.class));
                break;
            default:
                break;
        }
    }
}
