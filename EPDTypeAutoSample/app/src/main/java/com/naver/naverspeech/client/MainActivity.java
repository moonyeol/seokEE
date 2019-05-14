package com.naver.naverspeech.client;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.WorkerThread;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

// 1. Main Activity 클래스를 정의합니다.
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String CLIENT_ID = "yb5xynql42"; // "내 애플리케이션"에서 Client ID를 확인해서 이곳에 적어주세요.

    private RecognitionHandler handler;
    private NaverRecognizer naverRecognizer;

    private Button btnStart;
    private String mResult;
    private AudioWriterPCM writer;
    private boolean isRunning = true;
    private TextView et_pin;

    private Context context;
    private LinearLayout listView;

    private ArrayList<CheckBox> talk =  new ArrayList<>();
    private LinearLayout talkList;
    private String selectedUser = null;

    private String msg;
    private int func;

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
        btnStart = findViewById(R.id.btn_start);

        handler = new RecognitionHandler(this);
        naverRecognizer = new NaverRecognizer(this, handler, CLIENT_ID);

        Intent intent = getIntent();
        boolean host = intent.getExtras().getBoolean("isHost");
        String pin = intent.getExtras().getString("pin");

        et_pin = findViewById(R.id.pincode4);
        et_pin.setText(pin);

        if(host) btnStart.setVisibility(Button.VISIBLE);
        else btnStart.setVisibility(Button.GONE);


        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* 사용자의 OS 버전이 마시멜로우 이상인지 체크합니다. */
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    /* 사용자 단말기의 권한 중 권한이 허용되어 있는지 체크합니다. */
                    int permissionResult = checkSelfPermission(Manifest.permission.RECORD_AUDIO);
                    /* 권한이 없을 때 */
                    if (permissionResult == PackageManager.PERMISSION_DENIED) {
                        /* 사용자가 권한을 한번이라도 거부한 적이 있는 지 확인합니다. */
                        if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                            dialog.setTitle("권한이 필요합니다.")
                                    .setMessage("이 기능을 사용하기 위해서는 권한이 필요합니다. 계속하시겠습니까?")
                                    .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1000);
                                            }
                                        }
                                    })
                                    .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Toast.makeText(MainActivity.this, "기능을 취소했습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .create()
                                    .show();
                        }
                        // 최초로 권한을 요청하는 경우
                        else {
                            // 권한을 요청합니다.
                            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1000);
                        }
                    }
                    /* 권한이 있는 경우 */
                    else {
                        /* 음성 인식 기능을 처리합니다. */
                        if(!naverRecognizer.getSpeechRecognizer().isRunning()) {
                            mResult = "";
                            btnStart.setText(R.string.str_stop);
                            naverRecognizer.recognize();

                            commSock.kick(commSock.START, "START");
                        } else {
                            Log.d(TAG, "stop and wait Final Result");
                            btnStart.setEnabled(false);
                            naverRecognizer.getSpeechRecognizer().stop();
                        }
                    }
                }
                /* 사용자의 OS 버전이 마시멜로우 이하일 떄 */
                else {
                    /* 음성 인식 기능을 처리합니다. */
                    if(!naverRecognizer.getSpeechRecognizer().isRunning()) {
                        mResult = "";
                        btnStart.setText(R.string.str_stop);
                        naverRecognizer.recognize();

                        commSock.kick(commSock.START, "START");
                    } else {
                        Log.d(TAG, "stop and wait Final Result");
                        btnStart.setEnabled(false);
                        naverRecognizer.getSpeechRecognizer().stop();
                    }
                }
            }
        });

        new Thread(new Runnable(){
            public void run(){
                try {
                    final ScrollView scrollview = ((ScrollView) findViewById(R.id.scrollView));

                    while(isRunning) {

                        JSONArray jsonArray = commSock.read();

                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        func = jsonObject.optInt("func");
                        msg = jsonObject.optString("message");

                        if(func == commSock.REQUEST_USERLIST){
                            JSONObject msg = jsonArray.getJSONObject(0);
                            JSONObject msg2 = new JSONObject(msg.get("message").toString());
                            JSONArray msgCon = msg2.getJSONArray("con");

                            ArrayList<String> strings = new ArrayList<>();

                            for(int i=0; i<msgCon.length(); i++)
                                strings.add(msgCon.getJSONObject(i).toString());

                            for(String s : strings){
                                JSONObject json = new JSONObject(s);
                                String nick = json.get("nick").toString();
                                Button btn = new Button(context);
                                btn.setText(nick);
                                userList.add(new UserListButton(btn, nick));
                            }
                        }

                        runOnUiThread(new Runnable() {
                            public void run() {
                                switch(func){
                                    case commSock.MSG:
                                        scrollview.post(new Runnable() { @Override public void run() { scrollview.fullScroll(ScrollView.FOCUS_DOWN); } });
                                        CheckBox c = new CheckBox(context);
                                        c.setText(msg);
                                        c.setButtonDrawable(R.drawable.cb_check_on_off);
                                        c.setPadding(8,8,8,8);

                                        if(selectedUser != null)
                                            if(msg.contains(selectedUser)) c.setBackgroundColor(Color.argb(60,63,172,220));

                                        talk.add(c);
                                        talkList.addView(c);
                                        break;
                                    case commSock.START:
                                        Toast.makeText(MainActivity.this, "녹음 시작", Toast.LENGTH_SHORT).show();

                                        btnStart.callOnClick();
                                        if(!naverRecognizer.getSpeechRecognizer().isRunning())
                                            if(naverRecognizer != null) naverRecognizer.recognize();
                                        break;
                                    case commSock.EXIT:
                                        Toast.makeText(MainActivity.this, msg + " 종료", Toast.LENGTH_SHORT).show();

                                        for(int i=0; i<userList.size();i++){
                                            if(userList.get(i).getNickname().equals(msg)){
                                                if(selectedUser.equals(userList.get(i).getNickname())){
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
            if(naverRecognizer != null) naverRecognizer.recognize();
        }

        commSock.kick(commSock.REQUEST_USERLIST, "");
    }

    public void updateChatHighlight(){
        for(CheckBox chk : talk) {
            String s = (String)chk.getText();
            if(selectedUser != null && s.contains(selectedUser)) {
                chk.setBackgroundColor(Color.argb(60,63,172,220));
            } else {
                chk.setBackgroundColor(Color.argb(0, 255, 255, 255));
            }
        }
    }
    public void updateUserList(){
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
        btnStart.setText(R.string.str_start);
        btnStart.setEnabled(true);
    }
    @Override
    protected void onStop() {
        super.onStop();
        // 음성인식 서버를 종료합니다.
        naverRecognizer.getSpeechRecognizer().release();
        isRunning = false;
    }

    protected void onDestroy(){
        super.onDestroy();
    }

    // SpeechRecognizer 쓰레드의 메시지를 처리하는 핸들러를 정의합니다.
    static class RecognitionHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;
        RecognitionHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }

    public void bt_exit(View view) // 나가기 버튼을 눌렀을 때
    {
        // 확인창을 띄우고 yes면 나가기, _데이터 저장은 필요없,,! no면 안나가기
        new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle("회의 종료")
                .setMessage("정말 나가시겠습니까?")
                .setIcon(android.R.drawable.ic_menu_save)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // 확인시 처리 로직
                        commSock.kick(commSock.EXIT, "");
                        isRunning = false;
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

    class UserListButton {
        String nickname;
        Button btn;
        boolean selected;

        UserListButton(Button btn, final String nickname){
            this.btn = btn;
            btn.setOnClickListener(new Button.OnClickListener(){
                @Override
                public void onClick(View view) {
                    if(selectedUser != null && selectedUser.equals(nickname)){
                        selectedUser = null;
                        updateChatHighlight();
                    } else {
                        for (CheckBox c : talk) {
                            String s = (String) c.getText();

                            if (s.contains(nickname))
                                c.setBackgroundColor(Color.argb(60, 63, 172, 220));
                            else c.setBackgroundColor(Color.argb(0, 255, 255, 255));
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