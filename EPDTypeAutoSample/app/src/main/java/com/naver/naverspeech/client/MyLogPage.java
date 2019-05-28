package com.naver.naverspeech.client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import static android.content.Context.MODE_PRIVATE;
import static com.naver.naverspeech.client.commSock.REQUEST_USERINFO;
import static com.naver.naverspeech.client.commSock.gson;

public class MyLogPage extends Fragment {
    View view;
    private Adapter adapter;
    RequestUserInfo info;

    @Override
    public void onResume() {
        super.onResume();
    }
    public void onPause(){
        super.onPause();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // 여기에 R.layout.activity_mypage부분을 원하는 xml로 바꾸면 됩니다.
        view= inflater.inflate(R.layout.activity_mypage, null);

        // 바꾸신 후 이 아래에서 초기화 진행해주시면 됩니다.
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new Adapter();
        recyclerView.setAdapter(adapter);

        new Thread(new Runnable(){
            public void run(){
                commSock.kick(REQUEST_USERINFO, "");
                String message = commSock.read();

                info = gson.fromJson(message, RequestUserInfo.class);
                getActivity().runOnUiThread(new Runnable(){
                    public void run(){
                        setData(info);
                    }
                });
            }
        }).start();

        Button logoutBtn = view.findViewById(R.id.logout);

        logoutBtn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                //
            }
        });

        return view;
    }

    private void setData(RequestUserInfo info){
        TextView nickname_tv = view.findViewById(R.id.nickname_tv);
        TextView id_tv = view.findViewById(R.id.id_tv);
        TextView talkwithme = view.findViewById(R.id.talkWithMe);
        TextView contribution = view.findViewById(R.id.contributionTV);

        id_tv.setText(info.id);

        nickname_tv.setText(info.nickname);
        talkwithme.setText(info.talkWithMe);


        String num = String.format("%.2f" , (100-info.contributionData.get(2)));

        String temp = "평균 " +info.contributionData.get(0)+"%( 상위 "+ num + "% )";
        contribution.setText(temp);


        for (History h : info.histories){
            // 각 List의 값들을 data 객체에 set 해줍니다.

            if(h.getContent().equals("")) continue;

            Data data = new Data();
            data.setTitle(h.getTitle());
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