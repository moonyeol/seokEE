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
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import static com.naver.naverspeech.client.commSock.gson;

public class loginActivity extends AppCompatActivity {

    EditText idText, passwordText;
    Button loginButton, joinButton, anonymousbutton;
    CheckBox reid;
    int permissionResult, permissionWrite;

    public static Activity _login;

    class LoginInfo{
        public String id;
        public String pw;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _login = loginActivity.this;

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

            try {
                commSock.setSocket();
                Log.i("my", "Socket Connected.");
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i("my","make Handler and Thread");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            /* 사용자 단말기의 권한 중 권한이 허용되어 있는지 체크합니다. */
            permissionResult = checkSelfPermission(Manifest.permission.RECORD_AUDIO);
            permissionWrite = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            /* 권한이 없을 때 */
            if (permissionResult == PackageManager.PERMISSION_DENIED || permissionWrite == PackageManager.PERMISSION_DENIED) {
                /* 사용자가 권한을 한번이라도 거부한 적이 있는 지 확인합니다. */
                if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) ||
                        shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    ) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(loginActivity.this);
                    dialog.setTitle("권한이 필요합니다.")
                            .setMessage("이 기능을 사용하기 위해서는 권한이 필요합니다. 계속하시겠습니까?")
                            .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
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
                    requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
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

        passwordText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //Enter key Action
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    loginButton.callOnClick();
                    return true;
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new Button.OnClickListener(){

            public void onClick(View v) {
                LoginInfo info = new LoginInfo();
                info.id = idText.getText().toString();
                info.pw = Hashing.SHA256(passwordText.getText().toString());

                commSock.kick(commSock.LOGIN, gson.toJson(info));

                String msg = commSock.read();
                SocketMessage check = gson.fromJson(msg,SocketMessage.class);

                if(check.message.equals("true")){
                    if(reid.isChecked()){
                        SharedPreferences.Editor editor = getSharedPreferences("auto_login", MODE_PRIVATE).edit();
                        editor.putString("id",info.id);
                        editor.putString("pwd",info.pw);
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
