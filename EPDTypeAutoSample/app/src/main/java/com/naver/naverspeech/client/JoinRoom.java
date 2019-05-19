package com.naver.naverspeech.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class JoinRoom extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_room);


        Button button = findViewById(R.id.button3);

        button.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                EditText edittext = findViewById(R.id.idInput);
                String key = edittext.getText().toString();

                commSock.kick(commSock.ENTER,key);
                try {
                    String s = commSock.read().getJSONObject(0).optString("message");

                    if(s.equals("false")){
                        Toast.makeText(JoinRoom.this, "존재하지 않는 PIN번호입니다.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Intent intent = new Intent(JoinRoom.this, MainActivity.class);

                        intent.putExtra("isHost", false);
                        intent.putExtra("pin", key);

                        if(s.equals("running")) intent.putExtra("running", true);
                        else intent.putExtra("running", false);

                        startActivity(intent);

                        finish();
                    }
                }catch(org.json.JSONException e){
                    e.printStackTrace();
                }
            }
        });

    }




}