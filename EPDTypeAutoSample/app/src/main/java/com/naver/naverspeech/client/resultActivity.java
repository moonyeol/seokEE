package com.naver.naverspeech.client;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class resultActivity extends AppCompatActivity {
    private String pincode;
    ArrayList<userCon> userCons;
    ArrayList<content> contents;
    ArrayList<sentenceLine> slist;
    String[] keywords;
    String[] makingLocations;
    JSONArray info, info2, info3, info4, info5;
    StringBuilder sb = new StringBuilder();
    WebView wordCloud;
    Button exitBtn;
    private Context context;
    TextView keyword1;
    TextView keyword2;
    TextView keyword3;
    TextView keyword4;
    TextView keyword5;
    TextView contributionTV;
    ScrollView result;
    String conStr = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        context = this;
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
        keyword1 = findViewById(R.id.keyword1);
        keyword2 = findViewById(R.id.keyword2);
        keyword3 = findViewById(R.id.keyword3);
        keyword4 = findViewById(R.id.keyword4);
        keyword5 = findViewById(R.id.keyword5);
        contributionTV =findViewById(R.id.contributionTV);
        result = findViewById(R.id.resulScroll);
        new Thread(new Runnable(){
                public void run(){
                    exitBtn = findViewById(R.id.exit);
                    exitBtn.setText("... 계산 중입니다.");
                    exitBtn.setEnabled(false);

                    info = commSock.read();
                    info2 = commSock.read();
                    info3 = commSock.read();
                    info4 = commSock.read();
                    info5 = commSock.read();

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
                        keywords = msg.get("message").toString().split(" ");

                        msg = info3.getJSONObject(0);
                        JSONArray conList = new JSONArray(msg.get("message").toString());
                        for (int i = 0; i < conList.length(); i++) {
                            JSONObject obj = keywordList.getJSONObject(i);
                            userCons.add(new userCon(obj.getString("nickname"),obj.getDouble("contribution")));
                        }

                        for(int i=0; i<userCons.size();i++){
                            conStr += (i+1) +". " + userCons.get(i).nickname + " : " + userCons.get(i).contribution +"\n";
                        }

                        msg = info4.getJSONObject(0);
                        JSONArray contentList = new JSONArray(msg.get("message").toString());
                        for (int i = 0; i < conList.length(); i++) {
                            JSONObject obj = keywordList.getJSONObject(i);
                            contents.add(new content(obj.getString("nickname"),obj.getString("msg")));
                        }

                        msg = info5.getJSONObject(0);
                        makingLocations = msg.get("message").toString().split("");

                    } catch(Exception e){
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable(){
                        public void run(){
                            keyword1.setText("1. "+keywords[0]);
                            keyword2.setText("2. "+keywords[1]);
                            keyword3.setText("3. "+keywords[2]);
                            keyword4.setText("4. "+keywords[3]);
                            keyword5.setText("5. "+keywords[4]);

                            contributionTV.setText(conStr);


                            for(int i =0; i< contents.size();i++) {
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
                                result.addView(line);
                                slist.add(new sentenceLine(line, contents.get(i).nickname));
                            }




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

    class userCon{
        String nickname;
        double contribution;

        userCon(String nickname, double contribution){
            this.nickname = nickname;
            this.contribution = contribution;
        }
    }
}
