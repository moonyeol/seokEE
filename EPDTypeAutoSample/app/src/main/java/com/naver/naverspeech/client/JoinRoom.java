package com.naver.naverspeech.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static com.naver.naverspeech.client.commSock.gson;

public class JoinRoom extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_room);


        Button button = findViewById(R.id.button3);

        button.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                EditText edittext = findViewById(R.id.roomNumber);
                String key = edittext.getText().toString();

                commSock.kick(commSock.ENTER,key);

                String message = commSock.read();
                SocketMessage msg = gson.fromJson(message,SocketMessage.class);

                if(msg.message.equals("false")){
                    Toast.makeText(JoinRoom.this, "존재하지 않는 PIN번호입니다.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent intent = new Intent(JoinRoom.this, MainActivity.class);

                    intent.putExtra("isHost", false);
                    intent.putExtra("pin", key);

                    if(msg.message.equals("running")) intent.putExtra("running", true);
                    else intent.putExtra("running", false);

                    startActivity(intent);

                    finish();
                }
            }
        });

    }




}