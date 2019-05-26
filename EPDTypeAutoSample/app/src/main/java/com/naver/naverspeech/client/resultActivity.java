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
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

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
    char[] makingLocations;

    ArrayList<String> userNameList = new ArrayList<>();


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

                for(Talk talk: result.cont) {
                    contents.add(new content(talk.id, talk.msg));
                    contentSb.append(talk.id).append(" : ").append(talk.msg).append('\n');
                }

                makingLocations = new char[result.markData.length()];

                for(int i=0;i<makingLocations.length;i++){
                    makingLocations[i]=(result.markData.charAt(i));
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

                        for(i =0; i< contents.size();i++) {
                            LinearLayout newline = new LinearLayout(context);
                            newline.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            newline.setOrientation(LinearLayout.HORIZONTAL);
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
                                        else {
                                            s.lay.setBackgroundColor(Color.argb(0, 255, 255, 255));
                                            s.setFalse();
                                        }
                                    }
                                }
                            });
                            StringBuilder Sb = new StringBuilder();


                            TextView tt = new TextView(context);
                            tt.setText(Sb.append(" : ").append(contents.get(i).sentence).toString());

                            if(makingLocations[i]=='1'){
                                tt.setBackgroundColor(Color.argb(60,63,172,220));
                            }

                            newline.addView(nick);
                            newline.addView(tt);
                            line.addView(newline);
                            slist.add(new sentenceLine(newline, contents.get(i).nickname));
                        }

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

    public void drawChart(){
        ArrayList<Entry> lineEntries = new ArrayList<>();
        ArrayList<BarEntry> barEntries = new ArrayList<>();

        userNameList.add("");
        lineEntries.add(new Entry(0, 0));
        barEntries.add(new BarEntry(0, 0));

        int i = 1;
        for(Map.Entry<String, Double> entry : result.contrib.entrySet()){
            userNameList.add(entry.getKey());
            barEntries.add(new BarEntry(i++, entry.getValue().floatValue()));
        }

        i = 1;
        for(Map.Entry<String, Double> entry : result.keywordContrib.entrySet()){
            lineEntries.add(new Entry(i++, entry.getValue().floatValue()));
        }

        userNameList.add("");
        lineEntries.add(new Entry(i, 0));
        barEntries.add(new BarEntry(i++, 0));

        LineData lineData = new LineData();
        LineDataSet set = new LineDataSet(lineEntries, "키워드 발언 비율");

        set.setColor(Color.rgb(80, 80, 80));
        set.setLineWidth(2f);
        set.setCircleColor(Color.rgb(80, 80, 80));

        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setDrawValues(true);
        set.setValueTextSize(10f);
        set.setValueTextColor(Color.rgb(80, 80, 80));

        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineData.addDataSet(set);

        BarDataSet set1 = new BarDataSet(barEntries, "모든 발언 비율");
        set1.setColor(Color.rgb(60, 170, 220));
        set1.setValueTextColor(Color.rgb(80, 80, 80));
        set1.setValueTextSize(10f);
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);

        float barWidth = 1f / userNameList.size(); // x2 dataset

        BarData barData = new BarData(set1);
        barData.setBarWidth(barWidth);

        CombinedChart chart = findViewById(R.id.chart1);

        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);
        chart.setHighlightFullBarEnabled(false);

        chart.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.LINE
        });

        Legend legend = chart.getLegend();
        legend.setWordWrapEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(1f);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawLabels(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return userNameList.get((int) value % userNameList.size());
            }
        });


        CombinedData data = new CombinedData();

        data.setData(lineData);
        data.setData(barData);

        chart.setData(data);
        chart.invalidate();
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
        void setFalse(){
            chk = false;
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
