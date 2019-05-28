package com.naver.naverspeech.client;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.app.Activity;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;

import static com.naver.naverspeech.client.commSock.gson;


public class enter extends Activity {

    public static Activity _enter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);
        _enter = enter.this;

        Intent intent = getIntent();

        Button joinBtn = findViewById(R.id.enter_room);
        Button makeBtn = findViewById(R.id.make_room);
        Button pageBtn = findViewById(R.id.mypage);

        boolean is_login = intent.getBooleanExtra("is_login",false);

        if(!is_login) {
            pageBtn.setEnabled(false);
            pageBtn.setText(R.string.forUser);
        }


        joinBtn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {

//                final CustomDialog CD = new CustomDialog(_enter,
//                        new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent intent = new Intent(enter.this, MainActivity.class);
//
//                        String key = et.getText().toString();
//
//                        if(key.equals("")){
//                            Toast.makeText(_enter, "PIN번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//
//                        commSock.kick(commSock.ENTER,key);
//
//                        String message = commSock.read();
//                        SocketMessage msg = gson.fromJson(message,SocketMessage.class);
//
//                        Title t = gson.fromJson(msg.message, Title.class);
//
//                        if(t.pincode.equals("false")){
//                            Toast.makeText(_enter, "존재하지 않는 PIN번호입니다.", Toast.LENGTH_SHORT).show();
//                        }
//                        else {
//                            Intent intent2 = new Intent(_enter, MainActivity.class);
//
//                            intent2.putExtra("isHost", false);
//                            intent2.putExtra("pin", key);
//                            intent2.putExtra("title", t.title);
//
//                            if(t.pincode.equals("running")) intent2.putExtra("running", true);
//                            else intent2.putExtra("running", false);
//
//                            startActivity(intent2);
//
//                            finish();
//                        }
//                    }
//                }
//                        ,
//                        new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                            }            }
//                            );




                final EditText et = new EditText(_enter);

                final AlertDialog.Builder ad = new AlertDialog.Builder(_enter);
                ad.setTitle("참여 코드 입력");
                ad.setMessage("회의방의 참여 코드를 입력하세요.");



                et.setInputType(InputType.TYPE_CLASS_TEXT);
                et.setText("참여코드");
                et.setPadding(10,10,10,20);
                et.setEms(20);





                ad.setPositiveButton("입장", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(enter.this, MainActivity.class);

                        String key = et.getText().toString();

                        if(key.equals("")){
                            Toast.makeText(_enter, "PIN번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        commSock.kick(commSock.ENTER,key);

                        String message = commSock.read();
                        SocketMessage msg = gson.fromJson(message,SocketMessage.class);

                        Title t = gson.fromJson(msg.message, Title.class);

                        if(t.pincode.equals("false")){
                            Toast.makeText(_enter, "존재하지 않는 PIN번호입니다.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Intent intent2 = new Intent(_enter, MainActivity.class);

                            intent2.putExtra("isHost", false);
                            intent2.putExtra("pin", key);
                            intent2.putExtra("title", t.title);

                            if(t.pincode.equals("running")) intent2.putExtra("running", true);
                            else intent2.putExtra("running", false);

                            startActivity(intent2);

                            finish();
                        }

                        dialogInterface.dismiss();
                    }
                });
                ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                ad.setView(et);

                final AlertDialog alert = ad.create();
                et.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        //Enter key Action
                        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                            alert.getButton(AlertDialog.BUTTON_POSITIVE).callOnClick();
                            return true;
                        }
                        return false;
                    }
                });

                alert.show();

            }
        });

        makeBtn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) { //make_room
                final AlertDialog.Builder ad = new AlertDialog.Builder(_enter);
                ad.setTitle("회의 제목");
                ad.setMessage("회의 제목을 정해주세요.");

                final EditText et = new EditText(_enter);

                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yy_MM_dd");

                et.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                et.setText(sdf.format(cal.getTime()));
                et.setPadding(10,10,10,20);
                et.setEms(20);

                ad.setView(et);

                ad.setPositiveButton("설정", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(enter.this, MainActivity.class);

                        try {

                            commSock.kick(commSock.PINCODE, et.getText().toString());
                            String msg = commSock.read();
                            SocketMessage key = gson.fromJson(msg, SocketMessage.class);

                            Title t = gson.fromJson(key.message, Title.class);

                            intent.putExtra("isHost",true);
                            intent.putExtra("pin", t.pincode);
                            intent.putExtra("title", t.title);
                            intent.putExtra("running", false);

                            startActivity(intent);

                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        dialogInterface.dismiss();
                    }
                });
                ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog alert = ad.create();
                alert.show();


            }
        });
        pageBtn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                Intent intent = new Intent(enter.this, mypage.class);
                startActivity(intent);
            }
        });
    }

    protected void onStop() {
        super.onStop();
    }
    protected void onDestroy(){
        Toast.makeText(this,"onDestroy", Toast.LENGTH_SHORT).show();
        super.onDestroy();

        try {
            commSock.socket.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
