package com.naver.naverspeech.client;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class joinActivity extends AppCompatActivity {

    EditText et_id, et_pw, et_pw_chk, et_birth, et_gender, et_nick;
    TextView t_pwchk;
    String sId, sPw, sPw_chk, sBirth, sGender, sNick, s_tpwchk;
    Boolean re_chk = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        et_id = (EditText) findViewById(R.id.idInput);
        et_pw = (EditText) findViewById(R.id.passwordInput);
        et_pw_chk = (EditText) findViewById(R.id.Passwordcheck);
        et_birth = (EditText) findViewById(R.id.birthInput);
        et_gender = (EditText) findViewById(R.id.genderInput);
        et_nick = (EditText) findViewById(R.id.nickInput);
        t_pwchk = (TextView) findViewById(R.id.pw_chk_t);

        s_tpwchk = t_pwchk.getText().toString();


        class ThreadA extends Thread

        {

            public void run()

            {

                if(sPw.equals(sPw_chk));
            }

        }

    }

    // 중복 확인 버튼
    public void bt_ok(View view){

        commSock.kick(10,sId);
        String id_chk =  commSock.read();
        // true면 있는거, false면 없는거! -> true면 안됨!
        if(id_chk.equals("true")){
            new AlertDialog.Builder(this)
                    .setTitle("아이디 중복")
                    .setMessage("아이디가 중복됩니다. 다른 것을 입력해주세요.")
                    .setNeutralButton("닫기", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 창은 닫히고 추가 작업은 없음
                        }
                    })
                    .show();
        }
    }
    /**/
    public void bt_join(View view)
    {
        /* 버튼을 눌렀을 때 동작하는 소스 */
        sId = et_id.getText().toString();        //id
        sPw = et_pw.getText().toString();            //pw1
        sPw_chk = et_pw_chk.getText().toString();        //checking password
        sBirth = et_birth.getText().toString();
        sGender = et_gender.getText().toString();
        sNick = et_nick.getText().toString();



        /* equals 메소드를 통해 입력된 password와 chk가 같은 값인지 확인하고 다를 경우 에러를 리턴하도록 만들고, 같으면 서버에 보낸다.
         */
        // checking password가 꼭 필요할까?

        if(sPw.equals(sPw_chk))

        {

            // 회원가입 완료 시, 로그인 창으로 돌아가기

            //데이터를 추가하는 과정
            commSock.kick(7,sId+"&"+sPw+"&"+sGender+"&"+sBirth+"&"+sNick);

        }


        else {
            /*패스워드 확인이 불일치 함*/

            new AlertDialog.Builder(this)
                    .setTitle("비밀번호 불일치")
                    .setMessage("비밀번호가 다릅니다. 다시 입력해주세요.")
                    .setNeutralButton("닫기", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 창은 닫히고 추가 작업은 없음
                        }
                    })
                    .show();
        }    }

    // 회원가입 완료 후, 로그인 창으로 가기
    public void fin_join(View view) {
        Intent intent = new Intent(joinActivity.this, loginActivity.class);


        startActivity(intent);
    }
}