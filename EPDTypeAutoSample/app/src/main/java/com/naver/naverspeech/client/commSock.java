package com.naver.naverspeech.client;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Date;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

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
    public static final int REQUEST_USERINFO = 12;
    public static final int DEBUG = 13;
    public static final int REQUEST_USERLIST = 14;

    private static Key serverPublicKey = null;
    private static Key publicKey = null;
    private static Key privateKey = null;


    public static void setSocket(){
        Log.i("my","Try SetSocket");

        try{
            socket = new Socket("18.223.143.140", 9000);

            netWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            netReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            /*byte[] encodedKey = netReader.readLine().getBytes();
            serverPublicKey = new SecretKeySpec(encodedKey,0,encodedKey.length, "RSA");

            KeyPairGenerator keyPairGenerator;
            KeyPair keyPair;

            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);

            keyPair = keyPairGenerator.genKeyPair();
            publicKey = keyPair.getPublic(); // 공개키, 클라이언트에게 전달할 키
            privateKey = keyPair.getPrivate(); // 개인키

            sendMessage(publicKey.getEncoded().toString());*/
        }catch(Exception e){
            e.printStackTrace();
        }

        Log.i("my","setSocket Finished");
    }
    public static void sendMessage(String msg){
        try {
            PrintWriter out = new PrintWriter(netWriter, true);
            out.println(msg);
            //out.println(new String(incode(msg)));

        } catch(Exception e){
            e.printStackTrace();
        }
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
            //String jsonString = decode(netReader.readLine().getBytes());
            String jsonString = netReader.readLine();
            JSONArray jsonArray = new JSONObject(jsonString).getJSONArray("server");
            // usage

//             JSONArray arr = read();
//             JSONObject jsonObject = jsonArray.getJSONObject(0);
//
//             int func = jsonObject.optInt("func");
//             JSONObject message = new JSONObject(jsonObject.optString("message"));
//
//             normal
//             String s = message.optString("content");
//
//             request_USERINFO
//             String id = message.optString("id");
//             String nickName = message.optString("nickname");

            return jsonArray;
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private static String decode(byte[] arrCipherData) throws Exception {
        // 복호화
        Cipher cipher = Cipher.getInstance("RSA");

        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] arrData = cipher.doFinal(arrCipherData);
        String strResult = new String(arrData);

        return strResult;
    }

    private static byte[] incode(String inputStr) throws Exception {
        // 암호화
        Cipher cipher = Cipher.getInstance("RSA");

        cipher.init(Cipher.ENCRYPT_MODE, serverPublicKey);
        byte[] arrCipherData = cipher.doFinal(inputStr.getBytes()); // 암호화된 데이터(byte 배열)

        return arrCipherData;
    }
}