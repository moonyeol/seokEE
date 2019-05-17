package com.naver.naverspeech.client;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import java.io.FileOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;


import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.os.Environment;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;


public class mypage extends AppCompatActivity {
    private Adapter adapter;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_mypage);

            TextView nickname_tv = findViewById(R.id.nickname_tv);
            TextView id_tv = findViewById(R.id.id_tv);

            Info info = new Info();

            RecyclerView recyclerView = findViewById(R.id.recyclerView);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(linearLayoutManager);

            adapter = new Adapter();
            recyclerView.setAdapter(adapter);



            getData(info);

            id_tv.setText(info.id);
            nickname_tv.setText(info.nickname);

            //            Button make = findViewById(R.id.make);
//        make.setOnClickListener(new Button.OnClickListener(){
//            public void onClick(View v) {
////                commSock.kick();
//            }
//        });
        }



    private void init() {

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new Adapter();
        recyclerView.setAdapter(adapter);
    }

    private void getData(Info info) {
        // 임의의 데이터입니다.

        for (history h : info.historys){
            // 각 List의 값들을 data 객체에 set 해줍니다.

            Data data = new Data();
            data.setTitle(h.date);
            data.setMember(h.members);
            data.setNumber(h.number);

            if(h.content.length()>100)
                data.setContent(h.content.substring(0,100));
//                data.setContent(h.content);



            // 각 값이 들어간 data를 adapter에 추가합니다.
            adapter.addItem(data);
        }

        // adapter의 값이 변경되었다는 것을 알려줍니다.
        adapter.notifyDataSetChanged();
    }

    class history{
        String number;
        String content;
        String date;
        String members;

        history(String jString){
            try {
                JSONObject jobject = new JSONObject(jString);
                this.number = jobject.get("number").toString();
                this.content = jobject.get("content").toString();
                this.date = jobject.get("date").toString();
                this.members = jobject.get("members").toString();
                System.out.println(this.number);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        public String toString(){
            return this.date + " " +this.members + " " + this.number + " " + this.content;
        }
    }


    public class Info{
        String id;
        String nickname;
        ArrayList<history> historys;
        ArrayList<String> strings;

        Info(){
            try {
                historys = new ArrayList<>();
                strings = new ArrayList<>();

                commSock.kick(commSock.REQUEST_USERINFO," ");
                JSONArray info = commSock.read();

                JSONObject infoObject = new JSONObject(info.getJSONObject(0).optString("message"));

                this.id = infoObject.get("id").toString();
                this.nickname = infoObject.get("nickname").toString();

                JSONArray arr = commSock.read();
                JSONObject msg = arr.getJSONObject(0);
                JSONObject msg2 = new JSONObject(msg.get("message").toString());
                JSONArray msgCon = msg2.getJSONArray("con");

                for(int i=0; i<msgCon.length(); i++)
                   strings.add(msgCon.getJSONObject(i).toString());


                for(String s : strings){
                    historys.add(new history(s));
                }

            }catch(org.json.JSONException e){
                e.printStackTrace();
            }
        }
    }

}
