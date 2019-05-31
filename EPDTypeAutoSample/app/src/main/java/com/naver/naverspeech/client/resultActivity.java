package com.naver.naverspeech.client;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
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
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.naver.naverspeech.client.commSock.REQUEST_FILE;
import static com.naver.naverspeech.client.commSock.gson;
import static com.naver.naverspeech.client.commSock.read;

public class resultActivity extends AppCompatActivity {
    private String pincode;
    private Context context;

    StringBuilder sb = new StringBuilder();

    WebView wordCloud;
    ImageButton exitBtn;
    LinearLayout line;
    RequestResult result;
    TextView Rname;
    TextView dateView;
    ImageButton export;

    int keywordIdList[] = {R.id.keyword1, R.id.keyword2, R.id.keyword3, R.id.keyword4};
    boolean isExited;

    ArrayList<SentenceLine> slist = new ArrayList<>();
    char[] makingLocations;

    ArrayList<String> userNameList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        context = this;
        Rname = findViewById(R.id.textView17);
        export = findViewById(R.id.export2);
        Intent intent = getIntent();
        pincode = intent.getStringExtra("pincode");
        isExited = intent.getBooleanExtra("exited", false);
        exitBtn = findViewById(R.id.exit);
        line = findViewById(R.id.line);
        dateView = findViewById(R.id.textView02);

//        exitBtn.setText("... 분석 중입니다 ...");
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

                Log.d("RESULT", result.toString());


                if(result == null){
                    Toast.makeText(resultActivity.this, "retrieve NULL", Toast.LENGTH_SHORT).show();
                    return;
                }

                final JSONObject shell = new JSONObject();
                final JSONArray jsonArray = new JSONArray();

                for(Map.Entry<String, Integer> entry : result.wordFrequency.entrySet()) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("keyword", entry.getKey());
                        jsonObject.put("freq", entry.getValue());
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    jsonArray.put(jsonObject);
                }

                try { shell.put("data", jsonArray); } catch(Exception e){ e.printStackTrace(); }


                makingLocations = new char[result.markData.length()];
                for(int i=0;i<makingLocations.length;i++){
                    makingLocations[i]=(result.markData.charAt(i));
                }

                runOnUiThread(new Runnable(){
                    public void run(){

                        int index = 0;
                        for(Talk t : result.cont){
                            LinearLayout newline = new LinearLayout(context);
                            newline.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            newline.setOrientation(LinearLayout.HORIZONTAL);

                            final TextView nickname = new TextView(context);
                            final TextView colon = new TextView(context);
                            final TextView content = new TextView(context);

                            Typeface type = Typeface.createFromAsset(getAssets(), "fonts/nanumbarungothicbold.ttf");

                            colon.setText(" : ");
                            content.setText(t.msg);
                            nickname.setText(t.id);
                            Rname.setText(result.roomName);

                            //Date start = new Date(result.date);
                            //Date end = new Date(result.end);

                            //SimpleDateFormat sdf = new SimpleDateFormat("yy.MM.dd", Locale.KOREA);

                            //int totHour = end.getHours() - start.getHours();
                            //int totMin= end.getMinutes() - start.getMinutes();

                           // String startTime = String.format(Locale.KOREA, "%2d : %2d", start.getHours(), start.getMinutes());
                            //String endTime = String.format(Locale.KOREA, "%2d : %2d", end.getHours(), end.getMinutes());

                            //String str = sdf.format(start) + " / " +  startTime + " ~ " + endTime + " (" +totHour+ "시간 " + totMin + "분)";

                            dateView.setText(result.date + " " + result.end);

                            colon.setTypeface(type);
                            content.setTypeface(type);
                            nickname.setTypeface(type);

                            nickname.setClickable(true);

                            nickname.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    for(SentenceLine line : slist){
                                        if(line.s_nickname.equals(nickname.getText()) && !line.chk) line.setTrue();
                                        else line.setFalse();
                                    }
                                }
                            });

                            if(index < result.markData.length() && makingLocations[index++]=='1') newline.setBackgroundColor(Color.argb(60,63,172,220));

                            newline.addView(nickname);
                            newline.addView(colon);
                            newline.addView(content);
                            line.addView(newline);

                            slist.add(new SentenceLine(newline, nickname, content));
                        }

                        index = 0;
                        for(Map.Entry<String, Integer> entry : result.fiveKeyWord.entrySet()){
                            final Button keyword = findViewById(keywordIdList[index++]);
                            keyword.setText(entry.getKey());

                            keyword.setOnClickListener(new Button.OnClickListener(){
                                public void onClick(View v){
                                    for(SentenceLine line : slist){
                                        if(line.s_content.contains(keyword.getText()) && !line.highlight) line.setMark();
                                        else line.releaseMark();
                                    }
                                }
                            });
                        }

                        for(int i=index; i<4; i++){
                            Button keyword = findViewById(keywordIdList[i]);
                            keyword.setVisibility(View.GONE);
                        }

                        wordCloud = findViewById(R.id.webView);
                        wordCloud.getSettings().setJavaScriptEnabled(true);
                        wordCloud.loadUrl("file:///android_asset/test.html");
                        wordCloud.setWebChromeClient(new WebChromeClient());
                        String userAgent = wordCloud.getSettings().getUserAgentString();
                        wordCloud.getSettings().setUserAgentString(userAgent+"ahndroid");
                        wordCloud.addJavascriptInterface(new AndroidBridge(wordCloud,shell.toString()),"android");
                        wordCloud.reload();

                        drawChart();

//                        exitBtn.setText("나가기");
                        exitBtn.setEnabled(true);
                        export.setOnClickListener(new Button.OnClickListener() {
                            public void onClick(View v) {
                                final CustomDialog dialog = new CustomDialog(context, CustomDialog.EDITTEXT);
                                dialog.setTitleText("회의록 파일 저장");
                                dialog.setContentText("파일 이름을 지정해주세요");
                                dialog.setPositiveText("저장");
                                dialog.setNegativeText("취소");

                                dialog.setText(result.roomName);
                                dialog.setPositiveListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        try {
                                            commSock.kick(REQUEST_FILE, pincode);

                                            String value = read();
                                            SocketMessage receive = gson.fromJson(value, SocketMessage.class);

                                            String fileURL = receive.message;

                                            String Save_folder = "/seokEE";
                                            String Save_Path = "";
                                            String File_Name = dialog.getText();
                                            String ext = Environment.getExternalStorageState();
                                            if (ext.equals(Environment.MEDIA_MOUNTED)) {
                                                Save_Path = Environment.getExternalStorageDirectory()+ Save_folder;
                                            }
                                            File dir = new File(Save_Path);
                                            if (!dir.exists()) {
                                                dir.mkdirs();
                                            }
                                            downloadFile(fileURL,Save_Path,File_Name);

                                        }catch(Exception e){
                                            e.printStackTrace();
                                        }
                                        dialog.dismiss();
                                    }
                                });
                                dialog.setNegativeListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                    }
                                });
                                dialog.show();
                            }
                        });
                    }
                });
            }
        }).start();


    }

    public void highLightChat(){

    }
    public void downloadFile(String furl,String fpath,String fname) {
        File direct = new File(fpath);

        if (!direct.exists()) {
            direct.mkdirs();
        }
        Uri uri = Uri.parse(furl);
        DownloadManager mgr = (DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);

        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI
                        | DownloadManager.Request.NETWORK_MOBILE)
                .setDestinationInExternalPublicDir(direct+"/",fname+".docx")
                .setNotificationVisibility( DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);


        mgr.enqueue(request);

        // Open Download Manager to view File progress

//            context.startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));

    }
    public void drawChart(){
        ArrayList<Entry> lineEntries = new ArrayList<>();
        ArrayList<BarEntry> barEntries = new ArrayList<>();

        userNameList.add("");
        lineEntries.add(new Entry(0, 0));
        barEntries.add(new BarEntry(0, 0));

        HashMap<String, ContributionData> chartData = new HashMap<>();

        for(Map.Entry<String, Double> entry : result.contrib.entrySet()){
            ContributionData data = new ContributionData();
            data.talk = entry.getValue();

            chartData.put(entry.getKey(), data);
        }

        for(Map.Entry<String, Double> entry : result.keywordContrib.entrySet()){
            ContributionData data = chartData.get(entry.getKey());
            if(data == null) data = new ContributionData();
            data.keyword = entry.getValue();
        }

        int i = 1;
        for(Map.Entry<String, ContributionData> entry : chartData.entrySet()){
            ContributionData data = entry.getValue();
            userNameList.add(entry.getKey());
            lineEntries.add(new Entry(i, data.keyword.floatValue()));
            barEntries.add(new BarEntry(i++,data.talk.floatValue()));
        }

        userNameList.add("");
        lineEntries.add(new Entry(i, 0));
        barEntries.add(new BarEntry(i, 0));

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
        set1.setValueTextColor(Color.rgb(30, 120, 170));
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
        leftAxis.setAxisMaximum(1.2f);

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

        chart.getDescription().setText("");
        chart.setData(data);
        chart.invalidate();
    }


    class SentenceLine{
        LinearLayout lay;
        TextView nickname;
        TextView content;

        String s_nickname;
        String s_content;

        boolean chk = false;
        boolean highlight = false;

        SentenceLine(LinearLayout lay, TextView nick, TextView content){
            this.lay =lay;
            this.nickname = nick;
            this.content = content;

            this.s_nickname = nick.getText().toString();
            this.s_content = content.getText().toString();
        }
        void setTrue(){
            chk = true;
            nickname.setBackgroundColor(Color.argb(60, 63, 172, 220));
        }
        void setFalse(){
            chk = false;
            nickname.setBackgroundColor(Color.argb(0, 255, 255, 255));
        }

        void setMark(){
            highlight = true;
            content.setBackgroundColor(Color.argb(60, 63, 172, 220));
        }
        void releaseMark(){
            highlight = false;
            content.setBackgroundColor(Color.argb(0, 255, 255, 255));
        }
    }
    class ContributionData{
        Double keyword;
        Double talk;

        ContributionData(){
            keyword = 0.0;
            talk = 0.0;
        }
        ContributionData(Double keyword, Double talk){
            this.keyword = keyword;
            this.talk = talk;
        }
    }

}