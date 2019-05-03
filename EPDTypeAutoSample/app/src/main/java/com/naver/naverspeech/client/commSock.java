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
import java.util.Date;
import java.util.List;

public class commSock {
    public static Socket socket;
    public static BufferedWriter netWriter;
    public static BufferedReader netReader;

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
        JSONArray jsonArray = new JSONArray();
        try {
            send.put("func", func);
            send.put("time", new Date().toString());
            send.put("message", strings);
        }catch(Exception e){
            e.printStackTrace();
        }
        // 메세지 보낼 때 commSock 클래스를 이용
        // send.toString으로 해야 됩니다.
        commSock.sendMessage(send.toString());
    }

    public static String convertChatMessage(JSONObject arg){
        StringBuilder sb = new StringBuilder();

        try {
            sb.append("[" + arg.getString("talker") + "] : ");
            sb.append(arg.getString("message") + "\n");
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
        return sb.toString();
    }
    public static String read(){
        try {
            String s = netReader.readLine();
            return s;
        } catch(Exception e){
            e.printStackTrace();
            return "";
        }
    }
}