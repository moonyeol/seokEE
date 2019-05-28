package com.naver.naverspeech.client;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class CustomDialog extends Dialog {

    private TextView title;
    private TextView content;
    private Button mPositiveButton;
    private Button mNegativeButton;
    private EditText editText;

    private String titleString;
    private String contentString;
    private String positiveString;
    private String negativeString;
    private String editTextString = "";

    private View.OnClickListener mPositiveListener;
    private View.OnClickListener mNegativeListener;
    private View.OnKeyListener onKeyListener;

    private int type;
    public static int DIALOG = 1;
    public static int EDITTEXT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //다이얼로그 밖의 화면은 흐리게 만들어줌
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.8f;
        getWindow().setAttributes(layoutParams);

        if(type == DIALOG) setContentView(R.layout.custom_dialog);
        else if(type == EDITTEXT) setContentView(R.layout.custom_dialog_edittext);

        mPositiveButton = findViewById(R.id.pbutton);
        mNegativeButton = findViewById(R.id.nbutton);
        title = findViewById(R.id.custom_exit_head);
        content = findViewById(R.id.custom_exit_body);

        //클릭 리스너 셋팅 (클릭버튼이 동작하도록 만들어줌.)
        this.mPositiveButton.setOnClickListener(this.mPositiveListener);
        this.mNegativeButton.setOnClickListener(this.mNegativeListener);

        if(type == EDITTEXT) {
            this.editText = findViewById(R.id.dialogEditText);
            this.editText.setOnKeyListener(this.onKeyListener);
            this.editText.setText(this.editTextString);
            this.editText.setSelection(this.editTextString.length());
        }

        this.title.setText(titleString);
        this.content.setText(contentString);
        this.mPositiveButton.setText(positiveString);
        this.mNegativeButton.setText(negativeString);
    }


    //생성자 생성
    public CustomDialog(@NonNull Context context, int type) {
        super(context);
        this.type = type;
    }

    public Button getPositiveButton(){
        return this.mPositiveButton;
    }
    public void setPositiveListener(View.OnClickListener listener){
        this.mPositiveListener = listener;
    }
    public void setNegativeListener(View.OnClickListener listener){
        this.mNegativeListener = listener;
    }

    public void setOnKeyListener(View.OnKeyListener listener){
        this.onKeyListener = listener;
    }

    public void setTitleText(String title){
        this.titleString = title;
    }
    public void setContentText(String content){
        this.contentString = content;
    }

    public void setPositiveText(String text){
        this.positiveString = text;
    }
    public void setNegativeText(String text){
        this.negativeString = text;
    }

    public String getText(){
        return this.editText.getText().toString();
    }

    public void setText(String text){
        this.editTextString = text;
    }
}
