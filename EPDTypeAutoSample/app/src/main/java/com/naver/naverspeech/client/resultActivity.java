package com.naver.naverspeech.client;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import android.os.Environment;
import org.json.JSONArray;
import org.json.JSONObject;
public class resultActivity extends AppCompatActivity {
    private String pincode;
    JSONArray info, info2;
    StringBuilder sb = new StringBuilder();
    WebView wordCloud;
    Button exitBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Intent intent = getIntent();
        pincode = intent.getStringExtra("pincode");
        commSock.kick(commSock.REQUEST_RESULT, pincode);

        exitBtn = findViewById(R.id.exit);

        exitBtn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });

        new Thread(new Runnable(){
                public void run(){
                    exitBtn = findViewById(R.id.exit);
                    exitBtn.setText("... 계산 중입니다.");
                    exitBtn.setEnabled(false);

                    info = commSock.read();
                    info2 = commSock.read();
                    try {
                        JSONObject msg = info.getJSONObject(0);
                        JSONArray keywordList = new JSONArray(msg.get("message").toString());

                        for (int i = 0; i < keywordList.length(); i++){
                            JSONObject obj = keywordList.getJSONObject(i);
                            sb.append(obj.getString("keyword"));
                            sb.append(" ");
                            sb.append(obj.getString("freq"));
                            sb.append(" ");
                        }

                        msg = info2.getJSONObject(0);

                        sb.append(msg.get("message").toString());

                    } catch(Exception e){
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable(){
                        public void run(){
                            TextView tv = findViewById(R.id.resultView);

                            tv.setText(sb.toString());
                            wordCloud = findViewById(R.id.webView);
                            wordCloud.getSettings().setJavaScriptEnabled(true);
                            wordCloud.loadUrl("file:///android_asset/test.html");
                            wordCloud.setWebChromeClient(new WebChromeClient());
                            String userAgent = wordCloud.getSettings().getUserAgentString();
                            wordCloud.getSettings().setUserAgentString(userAgent+"ahndroid");
                            wordCloud.addJavascriptInterface(new AndroidBridge(wordCloud,sb),"android");
                            wordCloud.reload();

                            exitBtn.setText("나가기");
                            exitBtn.setEnabled(true);
                        }
                    });
                }
            }).start();


    }
}
