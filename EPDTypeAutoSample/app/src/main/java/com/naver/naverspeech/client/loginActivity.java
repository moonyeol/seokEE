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
import android.widget.Toast;

import org.json.JSONObject;

public class loginActivity extends AppCompatActivity {

    EditText idText, passwordText;
    Button loginButton, joinButton, anonymousbutton;
    TextView reid;
    String ids, pws;
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
                // 로그인 맞는지 체크.
                JSONObject send = new JSONObject();// JSONObject 생성

                try {
                    send.put("id", idText.getText().toString());
                    send.put("pw", passwordText.getText().toString());
                }catch(Exception e){
                    e.printStackTrace();
                }


                try {
                    commSock.kick(commSock.LOGIN, send.toString());
                    String check = commSock.read().getJSONObject(0).optString("message");

                    if(check.equals("true")){
                        Toast.makeText(loginActivity.this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(loginActivity.this, enter.class);
                        finish();
                        startActivity(intent);
                    } else {
                        Toast.makeText(loginActivity.this, "존재하지 않는 아이디거나 틀린 비밀번호입니다.", Toast.LENGTH_SHORT).show();
                    }
                    }catch(org.json.JSONException e){
                        e.printStackTrace();
                }
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


}







