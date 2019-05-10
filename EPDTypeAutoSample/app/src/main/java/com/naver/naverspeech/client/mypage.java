package com.naver.naverspeech.client;

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
            Info info = new Info();

            init();

            getData(info);





            TextView id_tv = (TextView) findViewById(R.id.id_tv);
            id_tv.setText(info.id);
            TextView nickname_tv = (TextView) findViewById(R.id.nickname_tv);
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

        for (int i = 0; i < info.historys.size(); i++) {
            // 각 List의 값들을 data 객체에 set 해줍니다.
            Data data = new Data();
            data.setTitle(info.historys.get(i).date);
            data.setMember(info.historys.get(i).members);
            data.setNumber(info.historys.get(i).number);
            if(info.historys.get(i).content.length()>100)
                data.setContent(info.historys.get(i).content.substring(0,100));
//            data.setResId(linfo.historys.get(i).content);

            // 각 값이 들어간 data를 adapter에 추가합니다.
            adapter.addItem(data);
        }

        // adapter의 값이 변경되었다는 것을 알려줍니다.
        adapter.notifyDataSetChanged();
    }

    public class Info{
        String id;

        String nickname;
        LinkedList<history> historys;

        Info(){
            try {
                historys = new LinkedList<>();
                commSock.kick(commSock.REQUEST_USERINFO," ");
                JSONArray arr = commSock.read();
                JSONObject jsonObject = arr.getJSONObject(0);
                JSONObject message = new JSONObject(jsonObject.optString("message"));


                this.id = message.get("id").toString();
                this.nickname = message.get("nickname").toString();
                int i = 1;
                while(arr.isNull(i)) {
                    this.historys.add(new history(new JSONObject(arr.getJSONObject(i).optString("message"))));
                    i++;
                }

            }catch(org.json.JSONException e){
                e.printStackTrace();
            }
        }


        class history{
            String number;
            String content;
            String date;
            String members;
            history(JSONObject jobject){
                this.number = jobject.optString("number");
                this.content = jobject.optString("content");
                this.date = jobject.optString("date");
                this.members = jobject.optString("member");
            }
        }

    }

}
