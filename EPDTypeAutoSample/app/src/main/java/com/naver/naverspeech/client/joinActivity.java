package com.naver.naverspeech.client;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class joinActivity extends AppCompatActivity {

    EditText et_id, et_pw, et_nick;
    Spinner bYear, bMonth, bDay;
    String sId, sPw, sBirth, sGender, sNick;
    private RadioButton et_gender;

    boolean re_chk = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        et_id = findViewById(R.id.id_input);
        et_pw = findViewById(R.id.passwordInput);
        et_gender = findViewById(R.id.genderMen);
        et_nick = findViewById(R.id.nickInput);
        bYear = findViewById(R.id.birthYear);
        bMonth = findViewById(R.id.birthMonth);
        bDay = findViewById(R.id.birthDay);

        // spinner initialize
        Calendar cal = Calendar.getInstance();

        int currentYear = cal.get(Calendar.YEAR);
        ArrayList<Integer> yList = new ArrayList<>();
        ArrayList<Integer> mList = new ArrayList<>();
        ArrayList<Integer> dList = new ArrayList<>();

        for(int i = -70; i<=10; i++) yList.add(currentYear + i);
        for(int i=1; i<=12; i++) mList.add(i);
        for(int i=1; i<=31; i++) dList.add(i);

        ArrayAdapter<Integer> yAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, yList);
        ArrayAdapter<Integer> mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mList);
        ArrayAdapter<Integer> dAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dList);

        yAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        bYear.setAdapter(yAdapter);
        bMonth.setAdapter(mAdapter);
        bDay.setAdapter(dAdapter);
    }

    // 중복 확인 버튼
    public void bt_ok(View view){
        sId = et_id.getText().toString();
        commSock.kick(commSock.DUPLICATE, sId);

        JSONArray jsonArray = commSock.read();
        try {
            JSONObject id_chk = jsonArray.getJSONObject(0);

            if (id_chk.optString("message").equals("false")) {
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
        if(!re_chk){
            Toast.makeText(this,"아이디 중복을 확인해주세요!", Toast.LENGTH_SHORT).show();
        }
        else {

            sId = et_id.getText().toString();        //id
            sPw = et_pw.getText().toString();            //pw1
            sBirth = bYear.getSelectedItem().toString();

            int m = Integer.parseInt(bMonth.getSelectedItem().toString());
            int d = Integer.parseInt(bDay.getSelectedItem().toString());

            sBirth += (m<10) ? "0" + m : m + "";
            sBirth += (d<10) ? "0" + d : d + "";

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
            JSONArray jsonArray = commSock.read();

            try {
                JSONObject check = jsonArray.getJSONObject(0);

                if (check.optString("message").equals("true")) {
                    Toast.makeText(this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(joinActivity.this, loginActivity.class);
                    startActivity(intent);

                    finish();
                } else {
                    Toast.makeText(this, "예기치 못한 오류가 발생했습니다.\n다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch(Exception e){
                e.printStackTrace();
            }

        }
    }
}