package com.naver.naverspeech.client;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

public class joinActivity extends AppCompatActivity {

    EditText et_id, et_pw, et_birth, et_nick;
    String sId, sPw, sBirth, sGender, sNick;
    private RadioButton et_gender;

    boolean re_chk = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        et_id = findViewById(R.id.idInput);
        et_pw = findViewById(R.id.passwordInput);
        et_birth = findViewById(R.id.birthInput);
        et_gender = findViewById(R.id.genderMen);
        et_nick = findViewById(R.id.nickInput);

        /*new Thread(new Runnable(){
            public void run(){
                while(isRunning) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (!et_pw.equals("") && et_pw.equals(et_pw_chk)) {
                                t_pwchk.setText("비밀번호 일치");
                                password_check = true;
                            }
                            else if (!et_pw.equals("")) {
                                t_pwchk.setText("비밀번호 불일치");
                                password_check = false;
                            }
                        }
                    });
                }
            }
        }).start();*/

    }

    // 중복 확인 버튼
    public void bt_ok(View view){
        sId = et_id.getText().toString();

        commSock.kick(commSock.DUPLICATE, sId);
        String id_chk =  commSock.read();

        if(id_chk.equals("false")){
            re_chk = false;
            Toast.makeText(this,"존재하는 아이디 입니다.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,"가능한 아이디 입니다.", Toast.LENGTH_SHORT).show();
            re_chk = true;
        }
    }

    public void bt_join(View view){
        if(!re_chk){
            Toast.makeText(this,"아이디 중복을 확인해주세요!", Toast.LENGTH_SHORT).show();
        }
        else {

            sId = et_id.getText().toString();        //id
            sPw = et_pw.getText().toString();            //pw1
            sBirth = et_birth.getText().toString();

            if(et_gender.isChecked()){
                sGender = "MALE";
            } else {
                sGender = "FEMALE";
            }

            sNick = et_nick.getText().toString();

            JSONObject send = new JSONObject();// JSONObject 생성

            try {
                send.put("id", sId);
                send.put("pw", sPw);
                send.put("gender", sGender);
                send.put("birth", sBirth);
                send.put("nick", sNick);
            }catch(Exception e){
                e.printStackTrace();
            }

            commSock.kick(commSock.ENROLL, send.toString());
            String check = commSock.read();

            if(check.equals("true")){
                Toast.makeText(this,"회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(joinActivity.this, loginActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this,"예기치 못한 오류가 발생했습니다.\n다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            }

        }
    }
}