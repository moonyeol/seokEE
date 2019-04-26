package com.naver.naverspeech.client;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
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


        Button button = (Button) findViewById(R.id.button3);
        button.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                EditText edittext = (EditText) findViewById(R.id.editText);
                int pkey = Integer.parseInt(edittext.getText().toString());
                commSock.kick("",2,pkey,"");
                Intent intent = new Intent(JoinRoom.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }




}
