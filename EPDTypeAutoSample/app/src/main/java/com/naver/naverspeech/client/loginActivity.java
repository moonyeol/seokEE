package com.naver.naverspeech.client;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class loginActivity extends AppCompatActivity {

    EditText idText, passwordText;
    Button loginButton, joinButton, anonymousbutton;
    TextView reid;
    String ids, pws; // 정보 추가하기
    public static Activity _login;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        _login = loginActivity.this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        idText = (EditText) findViewById(R.id.idInput);
        passwordText = (EditText) findViewById(R.id.passwordInput);

        loginButton = (Button) findViewById(R.id.loginButton);
        joinButton = (Button) findViewById(R.id.joinButton);
        anonymousbutton = (Button) findViewById(R.id.anonymous);
        reid = (CheckBox) findViewById(R.id.reid);


        loginButton.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {

                Intent intent = new Intent(loginActivity.this, enter.class);
                finish();
                startActivity(intent);
            }
        });
        joinButton.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {

                Intent intent = new Intent(loginActivity.this, joinActivity.class);
                finish();
                startActivity(intent);
            }
        });

        anonymousbutton.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {

                Intent intent = new Intent(loginActivity.this, anonymous.class);
                finish();
                startActivity(intent);
            }
        });

    }

    public void login_login(View view){

        ids = idText.getText().toString();        //id
        pws = passwordText.getText().toString();            //pw

        // 로그인
        commSock.kick(9,ids+"&"+pws);

    }


}







