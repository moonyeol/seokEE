package com.naver.naverspeech.client;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import static com.naver.naverspeech.client.commSock.gson;


public class enter extends Activity {

    public static Activity _enter;
    public Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);
        _enter = enter.this;
        context = this;

        Intent intent = getIntent();

        ImageButton joinBtn = findViewById(R.id.enter_room);
        ImageButton makeBtn = findViewById(R.id.make_room);
        ImageButton pageBtn = findViewById(R.id.mypage);
        TextView tvmp = findViewById(R.id.tv_mypage);
        TextView tvmr = findViewById(R.id.textView5);
        TextView tver = findViewById(R.id.textView6);

        boolean is_login = intent.getBooleanExtra("is_login",false);

        if(!is_login) {
            pageBtn.setEnabled(false);
            tvmp.setEnabled(false);
            tvmp.setText(R.string.forUser);
        }


        joinBtn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                final CustomDialog dialog = new CustomDialog(context, CustomDialog.EDITTEXT);
                dialog.setTitleText("참여 코드 입력");
                dialog.setContentText("회의방의 참여코드를 입력하세요");
                dialog.setPositiveText("입장");
                dialog.setNegativeText("취소");

                dialog.setPositiveListener(new View.OnClickListener(){
                    public void onClick(View v){
                        String key = dialog.getText();

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
                            Intent intent = new Intent(_enter, MainActivity.class);

                            intent.putExtra("isHost", false);
                            intent.putExtra("pin", key);
                            intent.putExtra("title", t.title);

                            if(t.pincode.equals("running")) intent.putExtra("running", true);
                            else intent.putExtra("running", false);

                            dialog.dismiss();

                            startActivity(intent);
                        }
                    }
                });
                dialog.setNegativeListener(new View.OnClickListener(){
                    public void onClick(View v){
                        dialog.dismiss();
                    }
                });
                dialog.setOnKeyListener(new View.OnKeyListener(){
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        //Enter key Action
                        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                            dialog.getPositiveButton().callOnClick();
                            return true;
                        }
                        return false;
                    }
                });

                dialog.show();

            }
        });


        tver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                final CustomDialog dialog = new CustomDialog(context, CustomDialog.EDITTEXT);
                dialog.setTitleText("참여 코드 입력");
                dialog.setContentText("회의방의 참여코드를 입력하세요");
                dialog.setPositiveText("입장");
                dialog.setNegativeText("취소");

                dialog.setPositiveListener(new View.OnClickListener(){
                    public void onClick(View v){
                        String key = dialog.getText();

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
                            Intent intent = new Intent(_enter, MainActivity.class);

                            intent.putExtra("isHost", false);
                            intent.putExtra("pin", key);
                            intent.putExtra("title", t.title);

                            if(t.pincode.equals("running")) intent.putExtra("running", true);
                            else intent.putExtra("running", false);

                            dialog.dismiss();

                            startActivity(intent);
                        }
                    }
                });
                dialog.setNegativeListener(new View.OnClickListener(){
                    public void onClick(View v){
                        dialog.dismiss();
                    }
                });
                dialog.setOnKeyListener(new View.OnKeyListener(){
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        //Enter key Action
                        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                            dialog.getPositiveButton().callOnClick();
                            return true;
                        }
                        return false;
                    }
                });

                dialog.show();

            }
        });

        makeBtn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) { //make_room
                final CustomDialog dialog = new CustomDialog(context, CustomDialog.EDITTEXT);
                dialog.setTitleText("회의방 제목 입력");
                dialog.setContentText("회의방의 제목을 입력해주세요.");
                dialog.setPositiveText("만들기");
                dialog.setNegativeText("취소");

                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yy_MM_dd");
                dialog.setText(sdf.format(cal.getTime()));

                dialog.setPositiveListener(new View.OnClickListener(){
                    public void onClick(View v){
                        Intent intent = new Intent(enter.this, MainActivity.class);

                        try {
                            commSock.kick(commSock.PINCODE, dialog.getText());
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
                        dialog.dismiss();
                    }
                });
                dialog.setNegativeListener(new View.OnClickListener(){
                    public void onClick(View v){
                        dialog.dismiss();
                    }
                });
                dialog.setOnKeyListener(new View.OnKeyListener(){
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        //Enter key Action
                        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                            dialog.getPositiveButton().callOnClick();
                            return true;
                        }
                        return false;
                    }
                });

                dialog.show();
            }
        });

        tvmr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CustomDialog dialog = new CustomDialog(context, CustomDialog.EDITTEXT);
                dialog.setTitleText("회의방 제목 입력");
                dialog.setContentText("회의방의 제목을 입력해주세요.");
                dialog.setPositiveText("만들기");
                dialog.setNegativeText("취소");

                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yy_MM_dd");
                dialog.setText(sdf.format(cal.getTime()));

                dialog.setPositiveListener(new View.OnClickListener(){
                    public void onClick(View v){
                        Intent intent = new Intent(enter.this, MainActivity.class);

                        try {
                            commSock.kick(commSock.PINCODE, dialog.getText());
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
                        dialog.dismiss();
                    }
                });
                dialog.setNegativeListener(new View.OnClickListener(){
                    public void onClick(View v){
                        dialog.dismiss();
                    }
                });
                dialog.setOnKeyListener(new View.OnKeyListener(){
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        //Enter key Action
                        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                            dialog.getPositiveButton().callOnClick();
                            return true;
                        }
                        return false;
                    }
                });

                dialog.show();
            }
        });

        pageBtn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                Intent intent = new Intent(enter.this, mypage.class);
                startActivity(intent);
            }
        });


        tvmp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(enter.this, mypage.class);
                startActivity(intent);            }
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