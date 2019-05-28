package com.naver.naverspeech.client;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class CustomDialog extends Dialog {

    public Button mPositiveButton;
    public Button mNegativeButton;

    public View.OnClickListener mPositiveListener;
    public View.OnClickListener mNegativeListener;
    public EditText et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //다이얼로그 밖의 화면은 흐리게 만들어줌
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.8f;
        getWindow().setAttributes(layoutParams);

        setContentView(R.layout.custom_dialog);


        //셋팅
        mPositiveButton=(Button)findViewById(R.id.nbutton); // 나가기 취소
        mNegativeButton=(Button)findViewById(R.id.pbutton);  // 나가기

        //클릭 리스너 셋팅 (클릭버튼이 동작하도록 만들어줌.)
        mPositiveButton.setOnClickListener(mPositiveListener);
        mNegativeButton.setOnClickListener(mNegativeListener);
    }
    public void setEdit(EditText et){
        this.et = et;
    }


    //생성자 생성
    public CustomDialog(@NonNull Context context, View.OnClickListener positiveListener, View.OnClickListener negativeListener) {
        super(context);
        this.mPositiveListener = positiveListener;
        this.mNegativeListener = negativeListener;
    }
}

