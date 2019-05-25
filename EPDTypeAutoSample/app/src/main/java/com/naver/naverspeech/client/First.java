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

import static com.naver.naverspeech.client.commSock.gson;

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

                // Run next activity

                SharedPreferences auto_login = getSharedPreferences("auto_login", MODE_PRIVATE);
                String id = auto_login.getString("id","");
                String pwd = auto_login.getString("pwd", "");

                if(!id.equals("") && !pwd.equals("")){
                    LoginInfo info = new LoginInfo();
                    info.id = id;
                    info.pw = pwd;

                    try {
                        commSock.kick(commSock.LOGIN, gson.toJson(info));
                        String msg = commSock.read();
                        SocketMessage check = gson.fromJson(msg,SocketMessage.class);

                        if(check.message.equals("true")){
                            Intent intent = new Intent(First.this, enter.class);
                            intent.putExtra("is_login",true);
                            startActivity(intent);
                            finish();
                        }
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                } else {
                    Intent intent = new Intent();
                    intent.setClass(First.this, loginActivity.class);

                    startActivity(intent);
                    finish();
                }
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