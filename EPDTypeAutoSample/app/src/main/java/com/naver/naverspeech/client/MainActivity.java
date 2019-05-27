package com.naver.naverspeech.client;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.WorkerThread;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.transition.Visibility;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.naver.naverspeech.client.utils.AudioWriterPCM;
import com.naver.speech.clientapi.SpeechConfig;
import com.naver.speech.clientapi.SpeechRecognitionException;
import com.naver.speech.clientapi.SpeechRecognitionListener;
import com.naver.speech.clientapi.SpeechRecognitionResult;
import com.naver.speech.clientapi.SpeechRecognizer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.naver.naverspeech.client.commSock.gson;
import static com.naver.naverspeech.client.commSock.socket;

// 1. Main Activity 클래스를 정의합니다.
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String CLIENT_ID = "yb5xynql42"; // "내 애플리케이션"에서 Client ID를 확인해서 이곳에 적어주세요.

    private RecognitionHandler handler;
    private NaverRecognizer naverRecognizer;

    private ImageButton btnStart;
    private String mResult;
    private AudioWriterPCM writer;
    private AtomicBoolean isRunning = new AtomicBoolean(true);
    private TextView et_pin;
    private TextView et_Title;
    private ImageButton btnExit;

    private Context context;
    private GridLayout listView;

    private ArrayList<CustomTalk> talk =  new ArrayList<>();
    private LinearLayout talkList;
    private String selectedUser = null;

    private String msg;
    private int func;


    StringBuilder sb;

    String pin;
    ArrayList<UserListButton> userList = new ArrayList<>();

    // 음성 인식 메시지를 처리합니다.
    private void handleMessage(Message msg) {
        switch (msg.what) {
            // 음성 인식을 시작할 준비가 완료된 경우
            case R.id.clientReady:
                Toast t = Toast.makeText(this, "Connected..", Toast.LENGTH_SHORT);
                writer = new AudioWriterPCM(Environment.getExternalStorageDirectory().getAbsolutePath() + "/NaverSpeechTest");
                writer.open("Test");
                break;
            // 현재 음성 인식이 진행되고 있는 경우
            case R.id.audioRecording:
                writer.write((short[]) msg.obj);
                break;
            // 처리가 되고 있는 도중에 결과를 받은 경우
            case R.id.partialResult:
                break;
            // 최종 인식이 완료되면 유사 결과를 모두 보여줍니다.
            case R.id.finalResult:
                SpeechRecognitionResult speechRecognitionResult = (SpeechRecognitionResult) msg.obj;
                List<String> results = speechRecognitionResult.getResults();
                String txt = results.get(0);

                if(!txt.equals("")) {
                    Toast.makeText(this, txt, Toast.LENGTH_SHORT).show();
                    commSock.kick(commSock.MSG, txt);
                }
                break;
            // 인식 오류가 발생한 경우
            case R.id.recognitionError:
                if (writer != null) {
                    writer.close();
                }
                mResult = "Error code : " + msg.obj.toString();

                Toast.makeText(this, mResult, Toast.LENGTH_SHORT).show();
                //btnStart.setVisibility(Button.GONE);

                if(naverRecognizer != null) naverRecognizer.recognize();
                break;
            // 음성 인식 비활성화 상태인 경우
            case R.id.clientInactive:
                if (writer != null) {
                    writer.close();
                }

                //btnStart.setText(R.string.str_start);
                //btnStart.setEnabled(true);

                if(naverRecognizer != null) naverRecognizer.recognize();
                break;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        context = this;
        listView = findViewById(R.id.userList);
        talkList = findViewById(R.id.talkList);
        btnStart = findViewById(R.id.btnstart);
        btnExit = findViewById(R.id.btnExit);
        et_pin = findViewById(R.id.pincode4);
        et_Title = findViewById(R.id.title);

        handler = new RecognitionHandler(this);
        naverRecognizer = new NaverRecognizer(this, handler, CLIENT_ID);

        Intent intent = getIntent();
        boolean host = intent.getExtras().getBoolean("isHost");
        pin = intent.getExtras().getString("pin");
        et_Title.setText(intent.getExtras().getString("title"));

        et_pin.setText(pin);

        Log.d("MAIN", "host : " + host);

        if(host) btnStart.setEnabled(true);
        else btnStart.setEnabled(false);

        btnExit.setVisibility(View.GONE);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!naverRecognizer.getSpeechRecognizer().isRunning()) {
                    mResult = "";

                    naverRecognizer.recognize();

                    commSock.kick(commSock.START, "START");
                } else {
                    Log.d(TAG, "stop and wait Final Result");
                    naverRecognizer.getSpeechRecognizer().stop();
                }

                btnStart.setVisibility(View.GONE);
                btnExit.setVisibility(View.VISIBLE);
            }
        });

        new Thread(new Runnable(){
            public void run(){
                try {
                    final ScrollView scrollview = findViewById(R.id.resulScroll);

                    while(isRunning.compareAndSet(true,true)) {
                        String message = commSock.read();
                        SocketMessage sMsg = gson.fromJson(message, SocketMessage.class);

                        Log.e("TAG", sMsg.func + " " + sMsg.message);

                        if(sMsg.func == commSock.REQUEST_USERLIST){
                            MemberList mList = gson.fromJson(sMsg.message, MemberList.class);

                            for(String s : mList.list){
                                Button btn = new Button(context);
                                btn.setText(s);
                                userList.add(new UserListButton(btn, s));
                            }
                        }

                        msg = sMsg.message;
                        func = sMsg.func;

                        runOnUiThread(new Runnable() {
                            public void run() {
                                switch(func){
                                    case commSock.MSG:
                                        CheckBox c = new CheckBox(context);

                                        SimpleTalk t = gson.fromJson(msg, SimpleTalk.class);
                                        sb = new StringBuilder();
                                        sb.append(t.talker).append(" : ").append(t.content);

                                        c.setText(sb.toString());

                                        c.setButtonDrawable(R.drawable.cb_check_on_off);
                                        c.setPadding(8,8,8,8);

                                        if(selectedUser != null)
                                            if(t.talker.equals(selectedUser)) c.setBackgroundColor(Color.argb(60,63,172,220));

                                        CustomTalk customTalk = new CustomTalk(c, t.talker, t.content);

                                        talk.add(customTalk);
                                        talkList.addView(c);

                                        scrollview.post(new Runnable() { @Override public void run() { scrollview.fullScroll(ScrollView.FOCUS_DOWN); } });
                                        break;
                                    case commSock.START:
                                        Toast.makeText(MainActivity.this, "녹음 시작", Toast.LENGTH_SHORT).show();
                                        btnStart.callOnClick();
                                        break;
                                    case commSock.EXIT:
                                        Toast.makeText(MainActivity.this, msg + " 종료", Toast.LENGTH_SHORT).show();


                                        for (int i = 0; i < userList.size(); i++) {
                                            if (userList.get(i).getNickname().equals(msg)) {
                                                if (selectedUser != null && selectedUser.equals(userList.get(i).getNickname())) {
                                                    selectedUser = null;
                                                    updateChatHighlight();
                                                }
                                                userList.remove(i);
                                                break;
                                            }
                                        }

                                        updateUserList();
                                        break;
                                    case commSock.ENTER:
                                        Toast.makeText(MainActivity.this, msg + " 입장", Toast.LENGTH_SHORT).show();

                                        Button btn = new Button(context);
                                        btn.setText(msg);
                                        userList.add(new UserListButton(btn, msg));

                                        updateUserList();
                                        break;
                                    case commSock.REQUEST_USERLIST:
                                        updateUserList();
                                        break;
                                }

                            }
                        });
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

        boolean isRecording = intent.getExtras().getBoolean("running");

        if(isRecording){
            Toast.makeText(this, "녹음 시작!", Toast.LENGTH_SHORT).show();
            naverRecognizer.getSpeechRecognizer().initialize();
            btnStart.callOnClick();
            btnStart.setEnabled(false);
        }

        commSock.kick(commSock.REQUEST_USERLIST, "");
    }
    public void updateChatHighlight(){
        if(!isRunning.get()) return;

        for(CustomTalk chk : talk) {
            if(selectedUser != null && chk.talker.equals(selectedUser)) {
                chk.checkBox.setBackgroundColor(Color.argb(60,63,172,220));
            } else {
                chk.checkBox.setBackgroundColor(Color.argb(0, 255, 255, 255));
            }
        }
    }
    public void updateUserList(){
        if(!isRunning.get()) return;

        new Thread(new Runnable(){
            public void run(){
                runOnUiThread(new Runnable(){
                    public void run(){
                        listView.removeAllViews();

                        for(UserListButton u : userList){
                            listView.addView(u.getButton());
                        }
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 음성인식 서버 초기화를 진행합니다.
        naverRecognizer.getSpeechRecognizer().initialize();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mResult = "";
        btnStart.setEnabled(true);
    }
    @Override
    protected void onStop() {
        super.onStop();
        // 음성인식 서버를 종료합니다.
        naverRecognizer.getSpeechRecognizer().release();
        isRunning.compareAndSet(true, false);
    }

    protected void onDestroy(){
        super.onDestroy();
    }

    // SpeechRecognizer 쓰레드의 메시지를 처리하는 핸들러를 정의합니다.
    static class RecognitionHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;
        RecognitionHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }

    public void bt_exit(View view) {
        // 확인창을 띄우고 yes면 나가기, _데이터 저장은 필요없,,! no면 안나가기
        CustomDialog cdd = new CustomDialog(MainActivity.this);
        cdd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                 cdd.setTitle("회의 종료");
                cdd.setMessage("정말 나가시겠습니까?");
                cdd.setIcon(android.R.drawable.ic_menu_save);
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // 확인시 처리 로직
                        StringBuilder markedData = new StringBuilder();

                        Log.i("MAIN", "talk");
                        for(CustomTalk t : talk){
                            if(t.checkBox.isChecked()) markedData.append("1");
                            else markedData.append("0");
                        }


                        Log.i("MAIN", "Recording Service Terminate.");
                        naverRecognizer.getSpeechRecognizer().release();
                        isRunning.compareAndSet(true,false);

                        Log.i("MAIN", "Kick Exit");
                        commSock.kick(commSock.EXIT, markedData.toString());

                        Log.i("MAIN", "Make Intent & Start Result Activity.");
                        Intent intent = new Intent(MainActivity.this, resultActivity.class);
                        intent.putExtra("exited", true);
                        intent.putExtra("pincode", pin);
                        startActivity(intent);

                        Log.i("MAIN", "finish Activity");
                        finish();

                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // 취소시 처리 로직
                        Toast.makeText(MainActivity.this, "취소하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }



    class CustomTalk {
        public CheckBox checkBox;
        public String talker;
        public String cont;

        CustomTalk(CheckBox checkBox, String talker, String cont){
            this.checkBox = checkBox;
            this.talker = talker;
            this.cont = cont;
        }
    }
    class UserListButton {
        String nickname;
        Button btn;
        boolean selected;

        UserListButton(Button btn, final String nickname){
            this.btn = btn;
            this.btn.setOnClickListener(new Button.OnClickListener(){
                @Override
                public void onClick(View view) {
                    if(selectedUser != null && selectedUser.equals(nickname)){
                        selectedUser = null;
                        updateChatHighlight();
                    } else {
                        for (CustomTalk c : talk) {
                            String s = c.talker;

                            if (s.equals(nickname))
                                c.checkBox.setBackgroundColor(Color.argb(60, 63, 172, 220));
                            else c.checkBox.setBackgroundColor(Color.argb(0, 255, 255, 255));
                        }
                        selectedUser = nickname;
                        updateChatHighlight();
                    }
                }
            });
            this.nickname = nickname;
            this.selected = false;
        }
        Button getButton(){
            return this.btn;
        }

        String getNickname(){
            return this.nickname;
        }
    }
}


// 2. SpeechRecognitionListener를 상속한 클래스를 정의합니다.
class NaverRecognizer implements SpeechRecognitionListener {
    private final static String TAG = NaverRecognizer.class.getSimpleName();
    private Handler mHandler;
    private SpeechRecognizer mRecognizer;
    public NaverRecognizer(Context context, Handler handler, String clientId) {
        this.mHandler = handler;
        try {
            mRecognizer = new SpeechRecognizer(context, clientId);
        } catch (SpeechRecognitionException e) {
            e.printStackTrace();
        }
        mRecognizer.setSpeechRecognitionListener(this);
    }
    public SpeechRecognizer getSpeechRecognizer() {
        return mRecognizer;
    }
    public void recognize() {
        try {
            mRecognizer.recognize(new SpeechConfig(SpeechConfig.LanguageType.KOREAN, SpeechConfig.EndPointDetectType.AUTO));
        } catch (SpeechRecognitionException e) {
            e.printStackTrace();
        }
    }
    @Override
    @WorkerThread
    public void onInactive() {
        Message msg = Message.obtain(mHandler, R.id.clientInactive);
        msg.sendToTarget();
    }
    @Override
    @WorkerThread
    public void onReady() {
        Message msg = Message.obtain(mHandler, R.id.clientReady);
        msg.sendToTarget();
    }
    @Override
    @WorkerThread
    public void onRecord(short[] speech) {
        Message msg = Message.obtain(mHandler, R.id.audioRecording, speech);
        msg.sendToTarget();
    }
    @Override
    @WorkerThread
    public void onPartialResult(String result) {
        Message msg = Message.obtain(mHandler, R.id.partialResult, result);
        msg.sendToTarget();
    }
    @Override
    @WorkerThread
    public void onEndPointDetected() {
        Log.d(TAG, "Event occurred : EndPointDetected");
    }
    @Override
    @WorkerThread
    public void onResult(SpeechRecognitionResult result) {
        Message msg = Message.obtain(mHandler, R.id.finalResult, result);
        msg.sendToTarget();
    }
    @Override
    @WorkerThread
    public void onError(int errorCode) {
        Message msg = Message.obtain(mHandler, R.id.recognitionError, errorCode);
        msg.sendToTarget();
    }
    @Override
    @WorkerThread
    public void onEndPointDetectTypeSelected(SpeechConfig.EndPointDetectType epdType) {
        Message msg = Message.obtain(mHandler, R.id.endPointDetectTypeSelected, epdType);
        msg.sendToTarget();
    }
}