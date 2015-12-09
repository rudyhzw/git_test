
package com.example.rudytest;

import android.R.integer;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;

public class WelcomeAct extends Activity {
    private boolean isFirstin;
    private static final int GO_HOME = 1000;
    private static final int GO_WEL = 1001;
    private int TIME = 1000 * 8;

    private Handler myHandler = new Handler() {

        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case GO_HOME:
                    startActivity(new Intent(WelcomeAct.this, MainActivity.class));
                    finish();
                    break;

                case GO_WEL:
                    startActivity(new Intent(WelcomeAct.this, Guide.class));
                    finish();
                    break;
            }

        };

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcomepage);
        init();
    }

    private void init() {
        SharedPreferences sp = getSharedPreferences("ld", MODE_PRIVATE);
        isFirstin = sp.getBoolean("isFirstin", true);
        if (!isFirstin) {
            myHandler.sendEmptyMessageDelayed(GO_HOME, TIME);
        }else{
            myHandler.sendEmptyMessageDelayed(GO_WEL, TIME);
            Editor editor = sp.edit();
            editor.putBoolean("isFirstin", false);
            editor.commit();
        }
    }

}
