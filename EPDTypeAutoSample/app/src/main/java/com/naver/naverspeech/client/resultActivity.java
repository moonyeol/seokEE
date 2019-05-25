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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
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
    RequestResult result;

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

        keyword[0] = findViewById(R.id.keyword1);
        keyword[1] = findViewById(R.id.keyword2);
        keyword[2] = findViewById(R.id.keyword3);
        keyword[3] = findViewById(R.id.keyword4);
        keyword[4] = findViewById(R.id.keyword5);

        line = findViewById(R.id.line);

        exitBtn.setText("... 분석 중입니다 ...");
        exitBtn.setEnabled(false);
        exitBtn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });

        new Thread(new Runnable(){
            public void run(){
                if(isExited) commSock.kick(commSock.EXIT, markedData);

                commSock.kick(commSock.REQUEST_RESULT, pincode);
                String msg = commSock.read();
                result = gson.fromJson(msg, RequestResult.class);

                if(result == null){
                    Toast.makeText(resultActivity.this, "retrieve NULL", Toast.LENGTH_SHORT).show();
                    return;
                }

                for(Map.Entry<String, Integer> entry : result.wordFrequency.entrySet())
                    sb.append(entry.getKey()).append(" ").append(entry.getValue()).append(" ");

                for(Map.Entry<String, Integer> entry : result.fiveKeyWord.entrySet())
                    fiveKeyword.add(entry.getKey());

                /*int i = 0;
                for(Map.Entry<String, Double> entry : result.contrib.entrySet())
                    userContrib.append(i++).append(". ").append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");*/

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

                        //contributionTV.setText(userContrib.toString());

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

                        drawChart();

                        exitBtn.setText("나가기");
                        exitBtn.setEnabled(true);
                    }
                });
            }
        }).start();


    }

    private void drawChart() {
        BarChart barChart = findViewById(R.id.barChart);
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        Description description = new Description();
        description.setText("");
        barChart.setDescription(description);
        barChart.setMaxVisibleValueCount(50);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(false);

        XAxis xl = barChart.getXAxis();
        xl.setGranularity(1f);
        xl.setCenterAxisLabels(true);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setSpaceTop(30f);
        barChart.getAxisRight().setEnabled(false);

        //data
        float groupSpace = 0.04f;
        float barSpace = 0.02f;
        float barWidth = 0.3f;

        List<BarEntry> yVals1 = new ArrayList<>();
        List<BarEntry> yVals2 = new ArrayList<>();

        int i = 1;
        for(Map.Entry<String, Double> entry : result.contrib.entrySet()) {
            yVals1.add(new BarEntry(i, entry.getValue().floatValue()));
            yVals2.add(new BarEntry(i, 2f));
        }

        BarDataSet set1, set2;

        if (barChart.getData() != null && barChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) barChart.getData().getDataSetByIndex(0);
            set2 = (BarDataSet) barChart.getData().getDataSetByIndex(1);
            set1.setValues(yVals1);
            set2.setValues(yVals2);
            barChart.getData().notifyDataChanged();
            barChart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(yVals1, "키워드 발언 횟수");
            set1.setColor(Color.rgb(104, 241, 175));
            set2 = new BarDataSet(yVals2, "총 발언 횟수");
            set2.setColor(Color.rgb(164, 228, 251));

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            dataSets.add(set2);

            BarData data = new BarData(dataSets);
            barChart.setData(data);
        }

        barChart.getBarData().setBarWidth(barWidth);
        barChart.groupBars(i, groupSpace, barSpace);
        barChart.invalidate();

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
