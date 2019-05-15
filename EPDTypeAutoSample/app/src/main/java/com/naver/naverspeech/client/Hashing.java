package com.naver.naverspeech.client;

import java.security.MessageDigest;

public class Hashing {
    public static String bytesToHex(byte[] bytes){
        StringBuilder builder = new StringBuilder();
        for(byte b : bytes){
            builder.append(String.format("%02x",b));
        }
        return builder.toString();
    }

    public static String SHA256(String msg){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(msg.getBytes());

            return bytesToHex(md.digest());
        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }
}
