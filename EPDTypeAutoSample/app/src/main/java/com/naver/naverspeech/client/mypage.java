package com.naver.naverspeech.client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import static com.naver.naverspeech.client.commSock.REQUEST_USERINFO;
import static com.naver.naverspeech.client.commSock.gson;


public class mypage extends AppCompatActivity {

    private FragmentManager fragmentManager;

    private MyInfoPage infoPage;
    private MyLogPage logPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // fragment가 뿌려지는 레이아웃: activity_mypage_sample
        setContentView(R.layout.activity_mypage_sample);
        fragmentManager = getSupportFragmentManager();
        setNav();
    }

    private void setNav(){
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        infoPage = new MyInfoPage();
        logPage = new MyLogPage();

        // fragment 2개를 추가한 뒤에 show, hide로 보여지고 안 보여지고를 바꿉니다.
        transaction.add(R.id.frame_layout, infoPage);
        transaction.add(R.id.frame_layout, logPage);
        transaction.show(infoPage);
        transaction.hide(logPage);
        transaction.commit();


        // 하단 바 버튼 클릭 리스너
        // 하단 바 모양은 menu폴더 menu_bottom.xml에 있습니다.
        // 이 하단 바는 activity_mypage_sample.xml에 추가되어 있습니다.

        // 이 버튼을 클릭함으로 인해서 MyInfoPage, MyLogPage를 왔다갔다 할 수 있게 됩니다.
        // 현재 MyInfoPage와 MyLogPage는 표시하는 것이 똑같은데 이는 각 클래스에 들어가시면 바꿀 수 있습니다.
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                switch (item.getItemId()) {
                    case R.id.navigation_menu1: {
                        transaction.show(infoPage);
                        transaction.hide(logPage);
                        transaction.commit();
                        break;
                    }
                    case R.id.navigation_menu2: {
                        transaction.show(logPage);
                        transaction.hide(infoPage);
                        transaction.commit();
                        break;
                    }
                }
                return true;
            }
        });
    }
}
