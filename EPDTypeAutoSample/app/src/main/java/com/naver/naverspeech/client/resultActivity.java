package com.naver.naverspeech.client;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

import static com.naver.naverspeech.client.commSock.gson;
import static com.naver.naverspeech.client.commSock.socket;

public class resultActivity extends AppCompatActivity {
    private String pincode;
    private Context context;

    StringBuilder sb = new StringBuilder();
    StringBuilder userContrib = new StringBuilder();
    StringBuilder contentSb = new StringBuilder();

    WebView wordCloud;
    Button exitBtn;
    TextView contributionTV;
    ScrollView resultScroll;
    LinearLayout line;

    TextView[] keyword = new TextView[5];
    ArrayList<String> fiveKeyword = new ArrayList<>();
    ArrayList<content> contents = new ArrayList<>();
    boolean isExited;

    String markedData;
    ArrayList<sentenceLine> slist = new ArrayList<>();
    String[] makingLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        context = this;

        Intent intent = getIntent();
        pincode = intent.getStringExtra("pincode");
        markedData = intent.getStringExtra("markData");
        isExited = intent.getBooleanExtra("exited", false);

        exitBtn = findViewById(R.id.exit);
        exitBtn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });

        keyword[0] = findViewById(R.id.keyword1);
        keyword[1] = findViewById(R.id.keyword2);
        keyword[2] = findViewById(R.id.keyword3);
        keyword[3] = findViewById(R.id.keyword4);
        keyword[4] = findViewById(R.id.keyword5);

        contributionTV =findViewById(R.id.contributionTV);
        resultScroll = findViewById(R.id.resulScroll);
        line = findViewById(R.id.line);


        new Thread(new Runnable(){
            public void run(){
                exitBtn.setText("... 분석 중입니다 ...");
                exitBtn.setEnabled(false);

                if(isExited) commSock.kick(commSock.EXIT, markedData);

                commSock.kick(commSock.REQUEST_RESULT, pincode);
                String msg = commSock.read();
                RequestResult result = gson.fromJson(msg, RequestResult.class);

                if(result == null){
                    Toast.makeText(resultActivity.this, "retrieve NULL", Toast.LENGTH_SHORT).show();
                    return;
                }

                for(Map.Entry<String, Integer> entry : result.wordFrequency.entrySet())
                    sb.append(entry.getKey()).append(" ").append(entry.getValue()).append(" ");

                for(Map.Entry<String, Integer> entry : result.fiveKeyWord.entrySet())
                    fiveKeyword.add(entry.getKey());

                int i = 0;
                for(Map.Entry<String, Double> entry : result.contrib.entrySet())
                    userContrib.append(i++).append(". ").append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");

                makingLocations = result.markData.split(" ");

                for(Talk talk: result.cont) {
                    contents.add(new content(talk.id, talk.msg));
                    contentSb.append(talk.id).append(" : ").append(talk.msg).append('\n');
                }


                runOnUiThread(new Runnable(){
                    public void run(){
                        int i = 0;

                        for(String word : fiveKeyword){
                            String s = (i + 1) + ". " + word;
                            keyword[i++].setText(s);
                        }

                        contributionTV.setText(userContrib.toString());

                        /* Content */
                        TextView tv = new TextView(context);
                        tv.setText(contentSb.toString());

                        line.addView(tv);


                        /*for(i =0; i< contents.size();i++) {
                            LinearLayout line = new LinearLayout(context);
                            final TextView nick = new TextView(context);
                            nick.setText(contents.get(i).nickname);
                            nick.setClickable(true);
                            nick.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    for(sentenceLine s : slist){
                                        if(s.nick.equals(nick.getText())&&s.chk==false){
                                             s.lay.setBackgroundColor(Color.argb(60, 63, 172, 220));
                                            s.setTrue();
                                        }
                                        else
                                            s.lay.setBackgroundColor(Color.argb(0, 255, 255, 255));
                                    }
                                }
                            });



                            TextView tt = new TextView(context);
                            tt.setText(" : "+contents.get(i).sentence);

                            if(makingLocations[i].equals("1")){
                                tt.setBackgroundColor(Color.argb(60,63,172,220));
                            }

                            line.addView(nick);
                            line.addView(tt);
                            resultScroll.addView(line);
                            slist.add(new sentenceLine(line, contents.get(i).nickname));
                        }*/

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
    class sentenceLine{
        LinearLayout lay;
        String nick;
        boolean chk = false;
        sentenceLine(LinearLayout lay, String nick){
            this.lay =lay;
            this.nick = nick;
        }
        void setTrue(){
            chk = true;
        }
    }
    class content{
        String nickname;
        String sentence;
        content(String nickname, String sentence){
            this.nickname =nickname;
            this.sentence =sentence;
        }

    }
}
