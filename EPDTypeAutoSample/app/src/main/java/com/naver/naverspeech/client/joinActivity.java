package com.naver.naverspeech.client;


import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.naver.naverspeech.client.commSock.gson;

public class joinActivity extends AppCompatActivity {

    EditText et_id, et_pw, et_nick;
    DatePicker datePicker;
    RadioGroup gender;
    boolean re_chk = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        et_id = findViewById(R.id.id_input);
        et_pw = findViewById(R.id.passwordInput);
        gender = findViewById(R.id.gender);
        et_nick = findViewById(R.id.nickInput);
        datePicker = findViewById(R.id.dataPicker);

        datePicker.init(1990, 1, 30, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {

            }
        });
        et_id.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { return; }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { re_chk = false; }
            @Override
            public void afterTextChanged(Editable editable) { re_chk = false; }
        });
    }

    // 중복 확인 버튼

    public void bt_ok(View view){
        if(et_id.getText().toString().equals("")){
            Toast.makeText(this, "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        commSock.kick(commSock.DUPLICATE, et_id.getText().toString());

        String s = commSock.read();
        SocketMessage msg = gson.fromJson(s,SocketMessage.class);

        try {
            String id_chk = msg.message;

            if (id_chk.equals("false")) {
                re_chk = false;
                Toast.makeText(this, "존재하는 아이디 입니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "가능한 아이디 입니다.", Toast.LENGTH_SHORT).show();
                re_chk = true;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void bt_join(View view){
        String id,pw,nick,genderStr, birth;

        id = et_id.getText().toString();
        pw = Hashing.SHA256(et_pw.getText().toString());
        nick = et_nick.getText().toString();
        SimpleDateFormat dateformat = new SimpleDateFormat("yy-MM-dd");
        Calendar getDate = Calendar.getInstance();
        getDate.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
        birth = dateformat.format(getDate.getTime());

        switch(gender.getCheckedRadioButtonId()){
            case R.id.genderMen:
                genderStr = "MALE";
                break;
            case R.id.genderWomen:
                genderStr = "FEMALE";
                break;
            case R.id.genderNone:
                genderStr = "NONE";
                break;
            default:
                Toast.makeText(this,"성별을 선택해주세요.", Toast.LENGTH_SHORT).show();
                return;
        }

        if(pw.equals("")) Toast.makeText(this,"비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
        else if(nick.equals("")) Toast.makeText(this,"닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show();
        else if(!re_chk) Toast.makeText(this,"아이디 중복을 확인해주세요.", Toast.LENGTH_SHORT).show();
        else {

            Member info = new Member();

            info.setGender(genderStr);
            info.setID(id);
            info.setPassword(pw);
            info.setBirth(birth);
            info.setNickname(nick);

            commSock.kick(commSock.ENROLL, gson.toJson(info));
            String message = commSock.read();
            SocketMessage msg = gson.fromJson(message, SocketMessage.class);

            if (msg.message.equals("true")) {
                Toast.makeText(this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(joinActivity.this, loginActivity.class);
                startActivity(intent);

                finish();
            } else {
                Toast.makeText(this, "예기치 못한 오류가 발생했습니다.\n다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            }

        }
    }


}