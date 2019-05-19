package com.naver.naverspeech.client;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.json.JSONArray;

public class resultActivity extends AppCompatActivity {
    private String pincode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Intent intent = getIntent();
        pincode = intent.getStringExtra("pincode");
        commSock.kick(commSock.REQUEST_RESULT,pincode);
        JSONArray info = commSock.read();
    }
}
