package com.naver.naverspeech.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static com.naver.naverspeech.client.commSock.gson;

public class JoinRoom extends Activity {
    EditText edittext;
    Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_room);

        edittext = findViewById(R.id.roomNumber);
        button = findViewById(R.id.button3);

        edittext.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //Enter key Action
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    button.callOnClick();
                    return true;
                }
                return false;
            }
        });

        button.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                String key = edittext.getText().toString();

                if(key.equals("")){
                    Toast.makeText(JoinRoom.this, "PIN번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                commSock.kick(commSock.ENTER,key);

                String message = commSock.read();
                SocketMessage msg = gson.fromJson(message,SocketMessage.class);

                Title t = gson.fromJson(msg.message, Title.class);

                if(t.pincode.equals("false")){
                    Toast.makeText(JoinRoom.this, "존재하지 않는 PIN번호입니다.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent intent = new Intent(JoinRoom.this, MainActivity.class);

                    intent.putExtra("isHost", false);
                    intent.putExtra("pin", key);
                    intent.putExtra("title", t.title);

                    if(t.pincode.equals("running")) intent.putExtra("running", true);
                    else intent.putExtra("running", false);

                    startActivity(intent);

                    finish();
                }
            }
        });

    }




}