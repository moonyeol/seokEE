package com.naver.naverspeech.client;

import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;


public class AndroidBridge {
    private final Handler handler = new Handler();
    private WebView mWebView;
    private StringBuilder keywords;

    public AndroidBridge(WebView mWebView, StringBuilder keywords) {
        this.mWebView = mWebView;
        this.keywords = keywords;
    }

    @JavascriptInterface
    public String requestStr() {
        return keywords.toString();
    }


}



