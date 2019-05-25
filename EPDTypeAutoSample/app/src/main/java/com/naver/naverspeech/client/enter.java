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
import android.view.Gravity;
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

        pageBtn.setVisibility(is_login ? View.VISIBLE : View.GONE);
        joinBtn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                Intent intent = new Intent(enter.this, JoinRoom.class);
                startActivity(intent);
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

                et.setText(sdf.format(cal.getTime()));
                et.setPadding(10,10,10,15);
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
