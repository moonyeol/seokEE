package com.naver.naverspeech.client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.view.View;
import android.widget.TextView;

import static com.naver.naverspeech.client.commSock.REQUEST_USERINFO;
import static com.naver.naverspeech.client.commSock.gson;


public class mypage extends AppCompatActivity {
    private Adapter adapter;
    RequestUserInfo info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new Adapter();
        recyclerView.setAdapter(adapter);

        new Thread(new Runnable(){
            public void run(){
                commSock.kick(REQUEST_USERINFO, "");
                String message = commSock.read();

                info = gson.fromJson(message, RequestUserInfo.class);
                runOnUiThread(new Runnable(){
                    public void run(){
                        setData(info);
                    }
                });
            }
        }).start();

        Button logoutBtn = findViewById(R.id.logout);

        logoutBtn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                SharedPreferences.Editor editor = getSharedPreferences("auto_login", MODE_PRIVATE).edit();
                editor.putString("id","");
                editor.putString("pwd", "");
                editor.apply();

                enter._enter.finish();
                Intent intent = new Intent(mypage.this, loginActivity.class);
                finish();
                startActivity(intent);
            }
        });

    }

    private void setData(RequestUserInfo info){
        TextView nickname_tv = findViewById(R.id.nickname_tv);
        TextView id_tv = findViewById(R.id.id_tv);
        TextView talkwithme = findViewById(R.id.talkWithMe);
        TextView contribution = findViewById(R.id.contributionTV);

        id_tv.setText(info.id);

        nickname_tv.setText(info.nickname);
        talkwithme.setText(info.talkWithMe);

        String temp = "나의 회의 평균 기여도 : " +info.contributionData.get(0)+" | 유저 평균 기여도 : "+ info.contributionData.get(1) + " | 나의 기여도 순위 : " + info.contributionData.get(2);
        contribution.setText(temp);


        for (History h : info.histories){
            // 각 List의 값들을 data 객체에 set 해줍니다.

            Data data = new Data();
            data.setTitle(h.getDate());
            data.setMember(h.getMembers());
            data.setNumber(h.getNumber());

            String content = h.getContent();
            if(content.length()>100)
                data.setContent(content.substring(0,100));
            else   data.setContent(content);

            // 각 값이 들어간 data를 adapter에 추가합니다.
            adapter.addItem(data);
        }

        // adapter의 값이 변경되었다는 것을 알려줍니다.
        adapter.notifyDataSetChanged();
    }
}
