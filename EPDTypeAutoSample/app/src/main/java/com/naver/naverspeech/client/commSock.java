package com.naver.naverspeech.client;


import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

public class commSock {
    public static Socket socket;
    public static BufferedWriter netWriter;
    public static BufferedReader netReader;

    public static Gson gson = new Gson();

    public static final int MSG = 0;
    public static final int PINCODE = 1;
    public static final int ENTER = 2;
    public static final int START = 3;
    public static final int SET_NICK = 4;
    public static final int EXIT = 5;
    public static final int ENROLL = 6;
    public static final int LOGIN = 7;
    public static final int DUPLICATE = 8;
    public static final int REQUEST_FILE = 9;
    public static final int REQUEST_USERINFO = 10;
    public static final int REQUEST_USERLIST = 11;
    public static final int REQUEST_RESULT = 12;

    public static void setSocket(){
        Log.i("my","Try SetSocket");

        try{
            socket = new Socket("13.209.64.113", 9000);
            netWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            netReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }catch(IOException e){
            e.printStackTrace();
        }

        Log.i("my","setSocket Finished");
    }

    public static void kick(int func, String msg)
    {
        PrintWriter out = new PrintWriter(netWriter, true);
        SocketMessage sMsg = new SocketMessage(func, msg);

        out.println(gson.toJson(sMsg));
    }

    public static String read(){
        try {
            String readValue = null;
            while(readValue == null) readValue = netReader.readLine();

            return readValue;
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}