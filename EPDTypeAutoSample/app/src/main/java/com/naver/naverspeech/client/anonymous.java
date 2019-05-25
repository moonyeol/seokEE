package com.naver.naverspeech.client;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class anonymous extends Activity {
    public static String nickname;
    public boolean is_login;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anonymous);


        Button button = findViewById(R.id.button3);
        button.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                EditText edittext = findViewById(R.id.nickInput);
                nickname = edittext.getText().toString();

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
