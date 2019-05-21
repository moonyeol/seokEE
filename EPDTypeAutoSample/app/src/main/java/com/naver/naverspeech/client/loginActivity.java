package com.naver.naverspeech.client;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

public class loginActivity extends AppCompatActivity {

    EditText idText, passwordText;
    Button loginButton, joinButton, anonymousbutton;
    CheckBox reid;

    public static Activity _login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        _login = loginActivity.this;
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            /* 사용자 단말기의 권한 중 권한이 허용되어 있는지 체크합니다. */
            int permissionResult = checkSelfPermission(Manifest.permission.RECORD_AUDIO);
            /* 권한이 없을 때 */
            if (permissionResult == PackageManager.PERMISSION_DENIED) {
                /* 사용자가 권한을 한번이라도 거부한 적이 있는 지 확인합니다. */
                if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(loginActivity.this);
                    dialog.setTitle("권한이 필요합니다.")
                            .setMessage("이 기능을 사용하기 위해서는 권한이 필요합니다. 계속하시겠습니까?")
                            .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1000);
                                    }
                                }
                            })
                            .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(loginActivity.this, "기능을 취소했습니다.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .create()
                            .show();
                }
                // 최초로 권한을 요청하는 경우
                else {
                    // 권한을 요청합니다.
                    requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1000);
                }
            }
        }


        setContentView(R.layout.activity_login);
        idText = findViewById(R.id.nickInput);
        passwordText = findViewById(R.id.passwordInput);

        loginButton = findViewById(R.id.loginButton);
        joinButton = findViewById(R.id.joinButton);
        anonymousbutton = findViewById(R.id.anonymous);
        reid = findViewById(R.id.reid);

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
                        if(reid.isChecked()){
                            SharedPreferences.Editor editor = getSharedPreferences("auto_login", MODE_PRIVATE).edit();
                            editor.putString("id",idText.getText().toString());
                            editor.putString("pwd", passwordText.getText().toString());
                            editor.apply();
                        }

                        Toast.makeText(loginActivity.this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(loginActivity.this, enter.class);

                        intent.putExtra("is_login",true);
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







