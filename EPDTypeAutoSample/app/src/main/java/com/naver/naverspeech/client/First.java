package com.naver.naverspeech.client;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.MotionEvent;

public class First extends Activity {

    private Thread splashThread;

    /** Called when the activity is first created. */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        // The thread to wait for splash screen events
        splashThread = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        // Wait given period of time or exit on touch
                        wait(10000);
                    }
                } catch (InterruptedException ex) {
                }
                finish();
                // Run next activity
                Intent intent = new Intent();
                intent.setClass(First.this,
                        enter.class);
                startActivity(intent);
            }
        };
        splashThread.start();
    }



    @Override
    public boolean onTouchEvent(MotionEvent evt) {
        if (evt.getAction() == MotionEvent.ACTION_DOWN) {
            synchronized (splashThread) {
                splashThread.notifyAll();
            }
        }
        return true;
    }

}
