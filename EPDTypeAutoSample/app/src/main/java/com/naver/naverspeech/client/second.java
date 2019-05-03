package com.naver.naverspeech.client;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class second extends Activity {
    public static Activity _second;
    public static boolean islogin = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        _second = second.this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);


        Button button = (Button) findViewById(R.id.login);
        button.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {

                Intent intent = new Intent(second.this, enter.class);
                finish();
                startActivity(intent);
            }
        });
        Button button2 = (Button) findViewById(R.id.anonymous);
        button2.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                Intent intent = new Intent(second.this, anonymous.class);

                startActivity(intent);
            }
        });





    }
}
