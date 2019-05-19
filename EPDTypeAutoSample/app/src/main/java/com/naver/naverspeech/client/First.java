package com.naver.naverspeech.client;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.MotionEvent;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

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
                        wait(1000);
                    }
                } catch (InterruptedException ex) {}

                finish();
                // Run next activity

                SharedPreferences auto_login = getSharedPreferences("auto_login", MODE_PRIVATE);
                String id = auto_login.getString("id","");
                String pwd = auto_login.getString("pwd", "");

                if(!id.equals("") && !pwd.equals("")){
                    JSONObject send = new JSONObject();// JSONObject 생성

                    try {
                        send.put("id", id);
                        send.put("pw", pwd);
                    }catch(Exception e){
                        e.printStackTrace();
                    }

                    try {
                        commSock.kick(commSock.LOGIN, send.toString());
                        String check = commSock.read().getJSONObject(0).optString("message");

                        if(check.equals("true")){
                            Intent intent = new Intent(First.this, enter.class);
                            intent.putExtra("is_login",true);
                            startActivity(intent);
                        }
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                } else {
                    Intent intent = new Intent();
                    intent.setClass(First.this, loginActivity.class);

                    startActivity(intent);
                }
            }
        };
        splashThread.start();

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

            try {
                commSock.setSocket();
                Log.i("my", "Socket Connected.");
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i("my","make Handler and Thread");
        }

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
