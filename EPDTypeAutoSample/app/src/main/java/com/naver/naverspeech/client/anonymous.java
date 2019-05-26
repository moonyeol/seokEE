package com.naver.naverspeech.client;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class anonymous extends Activity {
    public static String nickname;
    public boolean is_login;
    public EditText edittext;
    public Button loginBtn;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anonymous);

        edittext = findViewById(R.id.nickInput);
        loginBtn = findViewById(R.id.button3);

        edittext.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //Enter key Action
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    loginBtn.callOnClick();
                    return true;
                }
                return false;
            }
        });

        loginBtn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                nickname = edittext.getText().toString();

                if(nickname.equals("")){
                    Toast.makeText(anonymous.this, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                commSock.kick(commSock.SET_NICK,nickname);

                // nickname 을 서버에 보내고 메인메뉴화면으로 넘어감.
                Intent intent = new Intent(anonymous.this, enter.class);
                is_login = false;
                intent.putExtra("is_login",is_login);
                loginActivity lo = (loginActivity)loginActivity._login;
                startActivity(intent);

                lo.finish();
                finish();
            }
        });
    }




}
