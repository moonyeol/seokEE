package com.naver.naverspeech.client;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.util.JsonReader;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class commSock {
    public static Socket socket;
    public static BufferedWriter netWriter;
    public static BufferedReader netReader;

    public static final int MSG = 0;
    public static final int PINCODE = 1;
    public static final int ENTER = 2;
    public static final int START = 3;
    public static final int END = 4;
    public static final int SET_NICK = 5;
    public static final int EXIT = 6;
    public static final int ENROLL = 7;
    public static final int PASTLOG = 8;
    public static final int LOGIN = 9;
    public static final int DUPLICATE = 10;
    public static final int REQUEST_FILE = 11;

    public static void setSocket(){
        Log.i("my","Try SetSocket");

        try{
            socket = new Socket("18.223.143.140", 9000);
            netWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            netReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }catch(IOException e){
            e.printStackTrace();
        }

        Log.i("my","setSocket Finished");
    }
    public static void sendMessage(String msg){
        PrintWriter out = new PrintWriter(netWriter, true);
        out.println(msg);
    }

    public static void kick(int func, String strings)
    {
        JSONObject send = new JSONObject();// JSONObject 생성

        try {
            send.put("func", func);
            send.put("time", new Date().toString());
            send.put("message", strings);

        }catch(Exception e){
            e.printStackTrace();
        }

        commSock.sendMessage(send.toString());
    }

    public static JSONArray read(){
        try {
            String jsonString = netReader.readLine();
            JSONArray jsonArray = new JSONObject(jsonString).getJSONArray("server");

            // usage

            /*JSONArray arr = read();
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            String id = jsonObject.optString("id");
            String nickname = jsonObject.optString("nickname");*/

            return jsonArray;
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}