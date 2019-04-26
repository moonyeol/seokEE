package com.naver.naverspeech.client;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.app.Activity;
import android.widget.Toast;


public class enter extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);

        Button button = (Button) findViewById(R.id.enter_room);
        button.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {

                Intent intent = new Intent(enter.this, JoinRoom.class);
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
