package com.naver.naverspeech.client;

import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
        import java.io.BufferedWriter;
        import java.io.IOException;
        import java.io.InputStreamReader;
        import java.io.OutputStreamWriter;
        import java.io.PrintWriter;
        import java.net.Socket;

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
}
