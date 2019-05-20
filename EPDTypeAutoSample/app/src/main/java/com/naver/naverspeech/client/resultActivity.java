package com.naver.naverspeech.client;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

import org.json.JSONArray;

public class resultActivity extends AppCompatActivity {
    private String pincode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Intent intent = getIntent();
        pincode = intent.getStringExtra("pincode");
        commSock.kick(commSock.REQUEST_RESULT, pincode);

//        WebView wordCloud = findViewById(R.id.wordCloud);
//        wordCloud.getSettings().setJavaScriptEnabled(true);
//        wordCloud.loadUrl("file:///android_asset/www/index.html");

        JSONArray info = commSock.read();
    }
}
