package com.naver.naverspeech.client;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.app.Activity;
import android.widget.Toast;

import static com.naver.naverspeech.client.commSock.gson;


public class enter extends Activity {

    public static Activity _enter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);
        _enter = enter.this;

        Intent intent = getIntent();

        Button joinBtn = findViewById(R.id.enter_room);
        Button makeBtn = findViewById(R.id.make_room);
        Button pageBtn = findViewById(R.id.mypage);

        boolean is_login = intent.getBooleanExtra("is_login",false);

        pageBtn.setVisibility(is_login ? View.VISIBLE : View.GONE);
        joinBtn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                Intent intent = new Intent(enter.this, JoinRoom.class);
                startActivity(intent);
            }
        });

        makeBtn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) { //make_room
                Intent intent = new Intent(enter.this, MainActivity.class);

                try {
                    commSock.kick(commSock.PINCODE, "");
                    String msg = commSock.read();
                    SocketMessage key = gson.fromJson(msg, SocketMessage.class);

                    intent.putExtra("isHost",true);
                    intent.putExtra("pin", key.message);
                    intent.putExtra("running", false);

                    startActivity(intent);

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
        pageBtn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                Intent intent = new Intent(enter.this, mypage.class);
                startActivity(intent);
            }
        });
    }

    protected void onStop() {
        super.onStop();

    }
    protected void onDestroy(){
        Toast.makeText(this,"onDestroy", Toast.LENGTH_SHORT).show();
        super.onDestroy();

        try {
            commSock.socket.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
