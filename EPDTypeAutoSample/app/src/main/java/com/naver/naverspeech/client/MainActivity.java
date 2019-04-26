package com.naver.naverspeech.client;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.annotation.WorkerThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.naver.naverspeech.client.utils.AudioWriterPCM;
import com.naver.speech.clientapi.SpeechConfig;
import com.naver.speech.clientapi.SpeechRecognitionException;
import com.naver.speech.clientapi.SpeechRecognitionListener;
import com.naver.speech.clientapi.SpeechRecognitionResult;
import com.naver.speech.clientapi.SpeechRecognizer;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.Date;
import java.util.List;

// 1. Main Activity 클래스를 정의합니다.
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String CLIENT_ID = "yb5xynql42"; // "내 애플리케이션"에서 Client ID를 확인해서 이곳에 적어주세요.

    private RecognitionHandler handler;
    private NaverRecognizer naverRecognizer;
    private TextView txtResult;
    private Button btnStart;
    private String mResult;
    private AudioWriterPCM writer;

    // 음성 인식 메시지를 처리합니다.
    private void handleMessage(Message msg) {
        switch (msg.what) {
            // 음성 인식을 시작할 준비가 완료된 경우
            case R.id.clientReady:
                txtResult.setText("Connected");
                writer = new AudioWriterPCM(Environment.getExternalStorageDirectory().getAbsolutePath() + "/NaverSpeechTest");
                writer.open("Test");
                break;
            // 현재 음성 인식이 진행되고 있는 경우
            case R.id.audioRecording:
                writer.write((short[]) msg.obj);
                break;
            // 처리가 되고 있는 도중에 결과를 받은 경우
            case R.id.partialResult:
                mResult = (String) (msg.obj);
                txtResult.setText(mResult);
                break;
            // 최종 인식이 완료되면 유사 결과를 모두 보여줍니다.
            case R.id.finalResult:
                SpeechRecognitionResult speechRecognitionResult = (SpeechRecognitionResult) msg.obj;
                List<String> results = speechRecognitionResult.getResults();
                StringBuilder strBuf = new StringBuilder();
                // 전달 받은 모든 문자열을 차례대로 출력합니다.
                for(String result : results) {
                    strBuf.append(result);
                    strBuf.append("\n");
                }
                Log.i("my","Send Message");

                JSONObject send = new JSONObject();
                try {
                    send.put("talker", "1");
                    send.put("func", 0);
                    send.put("number", 1);
                    send.put("time", new Date());
                    send.put("message", results.get(0));
                }catch(Exception e){
                    e.printStackTrace();
                }

                commSock.sendMessage(send.toString());

                mResult = strBuf.toString();
                txtResult.setText(mResult);
                break;
            // 인식 오류가 발생한 경우
            case R.id.recognitionError:
                if (writer != null) {
                    writer.close();
                }
                mResult = "Error code : " + msg.obj.toString();
                txtResult.setText(mResult);
                btnStart.setText(R.string.str_start);
                btnStart.setEnabled(true);
                break;
            // 음성 인식 비활성화 상태인 경우
            case R.id.clientInactive:
                if (writer != null) {
                    writer.close();
                }
                btnStart.setText(R.string.str_start);
                btnStart.setEnabled(true);
                break;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtResult = (TextView) findViewById(R.id.txt_result);
        btnStart = (Button) findViewById(R.id.btn_start);
        handler = new RecognitionHandler(this);
        naverRecognizer = new NaverRecognizer(this, handler, CLIENT_ID);
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
                            txtResult.setText("Connecting...");
                            btnStart.setText(R.string.str_stop);
                            naverRecognizer.recognize();
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
                        txtResult.setText("Connecting...");
                        btnStart.setText(R.string.str_stop);
                        naverRecognizer.recognize();
                    } else {
                        Log.d(TAG, "stop and wait Final Result");
                        btnStart.setEnabled(false);
                        naverRecognizer.getSpeechRecognizer().stop();
                    }
                }
            }
        });

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

            try {
                commSock.setSocket();
                Log.i("my", "Socket Connected.");
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.i("my","make Handler and Thread");
        }

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
        txtResult.setText("");
        btnStart.setText(R.string.str_start);
        btnStart.setEnabled(true);
    }
    @Override
    protected void onStop() {
        super.onStop();
        // 음성인식 서버를 종료합니다.
        naverRecognizer.getSpeechRecognizer().release();

        try {
            commSock.socket.close();
        }catch(Exception e){
            e.printStackTrace();
        }
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




