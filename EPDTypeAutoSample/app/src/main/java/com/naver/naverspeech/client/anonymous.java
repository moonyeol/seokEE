package com.naver.naverspeech.client;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class anonymous extends Activity {
    public static String nickname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anonymous);


        Button button = (Button) findViewById(R.id.button4);
        button.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                EditText edittext = (EditText) findViewById(R.id.editText2);
                nickname = edittext.getText().toString();
                commSock.kick(5,nickname);


                Intent intent = new Intent(anonymous.this, enter.class);
                loginActivity lo = (loginActivity)loginActivity._login;
                startActivity(intent);
                lo.finish();
                finish();

            }
        });
    }




}
