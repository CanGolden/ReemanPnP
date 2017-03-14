package com.jcan.reemanpnp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

/**
 * Created by ZJcan on 2017-03-13.
 */

public class BaseActivity extends AppCompatActivity {

    private String TAG = "BaseActivity";
    private boolean isFront = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");
        isFront = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG, "onPause");
        isFront = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onButton(View v) {

    }

    public boolean isFront() {
        return isFront;
    }

    public void setFront(boolean front) {
        isFront = front;
    }
}
