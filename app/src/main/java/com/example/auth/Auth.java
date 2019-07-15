package com.example.auth;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import javax.mail.MessagingException;
import javax.mail.SendFailedException;

public class Auth extends AppCompatActivity implements View.OnClickListener,Dialog.OnCancelListener {

    String randomCode; // 테스트할 6자리 인증번호
    String user_answer;

    EditText authEmail;
    Button authBtn;

    LayoutInflater dialog; //LayoutInflater
    View dialogLayout; //layout을 담을 View
    Dialog authDialog; //dialog 객체

    /*카운트 다운 타이머에 관련된 필드*/

    TextView time_counter; //시간을 보여주는 TextView
    EditText emailAuth_number; //인증 번호를 입력 하는 칸
    Button emailAuth_btn; // 인증버튼
    CountDownTimer countDownTimer;
    final int MILLISINFUTURE = 30 * 1000; //총 시간 (300초 = 5분)
    final int COUNT_DOWN_INTERVAL = 1000; //onTick 메소드를 호출할 간격 (1초)

    TextView message = null;
    String id = "QTUQtdrgoPZtX3SFCb2y/+i/WI4LwCuUnXx1V+1ElBU=";
    String password = "YKLGhjNLknNxE9R9kv9A3Q==";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        authEmail = (EditText) findViewById(R.id.authEmail);
        authBtn = (Button) findViewById(R.id.authBtn);
        authBtn.setOnClickListener(this);

        //이메일 전송을 위해 인터넷 접근 권한 허용
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .permitDiskReads()
                .permitDiskWrites()
                .permitNetwork().build());

        message = (TextView) findViewById(R.id.message);
    }

    public void countDownTimer() { //카운트 다운 메소드

        time_counter = (TextView) dialogLayout.findViewById(R.id.emailAuth_time_counter);
        //줄어드는 시간을 나타내는 TextView
        emailAuth_number = (EditText) dialogLayout.findViewById(R.id.emailAuth_number);
        //사용자 인증 번호 입력창
        emailAuth_btn = (Button) dialogLayout.findViewById(R.id.emailAuth_btn);
        //인증하기 버튼


        countDownTimer = new CountDownTimer(MILLISINFUTURE, COUNT_DOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) { //(300초에서 1초 마다 계속 줄어듬)

                long emailAuthCount = millisUntilFinished / 1000;
                Log.d("Alex", emailAuthCount + "");

                if ((emailAuthCount - ((emailAuthCount / 60) * 60)) >= 10) { //초가 10보다 크면 그냥 출력
                    time_counter.setText((emailAuthCount / 60) + " : " + (emailAuthCount - ((emailAuthCount / 60) * 60)));
                } else { //초가 10보다 작으면 앞에 '0' 붙여서 같이 출력. ex) 02,03,04...
                    time_counter.setText((emailAuthCount / 60) + " : 0" + (emailAuthCount - ((emailAuthCount / 60) * 60)));
                }

                //emailAuthCount은 종료까지 남은 시간임. 1분 = 60초 되므로,
                // 분을 나타내기 위해서는 종료까지 남은 총 시간에 60을 나눠주면 그 몫이 분이 된다.
                // 분을 제외하고 남은 초를 나타내기 위해서는, (총 남은 시간 - (분*60) = 남은 초) 로 하면 된다.

            }


            @Override
            public void onFinish() { //시간이 다 되면 다이얼로그 종료

                authDialog.cancel();

            }
        }.start();

        emailAuth_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.authBtn:

                try {
                    GMailSender gMailSender = new GMailSender("your_email_id", "your_email_password");
                    randomCode = gMailSender.getEmailCode();
                    //GMailSender.sendMail(제목, 본문내용, 받는사람);
                    gMailSender.sendMail("인증번호입니다.", randomCode, authEmail.getText().toString());
                    Toast.makeText(getApplicationContext(), "이메일을 성공적으로 보냈습니다.", Toast.LENGTH_SHORT).show();
                } catch (SendFailedException e) {
                    Toast.makeText(getApplicationContext(), "이메일 형식이 잘못되었습니다.", Toast.LENGTH_SHORT).show();
                } catch (MessagingException e) {
                    Toast.makeText(getApplicationContext(), "인터넷 연결을 확인해주십시오", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                dialog = LayoutInflater.from(this);
                dialogLayout = dialog.inflate(R.layout.auth_dialog, null); // LayoutInflater를 통해 XML에 정의된 Resource들을 View의 형태로 반환 시켜 줌
                authDialog = new Dialog(this); //Dialog 객체 생성
                authDialog.setContentView(dialogLayout); //Dialog에 inflate한 View를 탑재 하여줌
                authDialog.setCanceledOnTouchOutside(false); //Dialog 바깥 부분을 선택해도 닫히지 않게 설정함.
                authDialog.setOnCancelListener(this); //다이얼로그를 닫을 때 일어날 일을 정의하기 위해 onCancelListener 설정
                authDialog.show(); //Dialog를 나타내어 준다.
                countDownTimer();
                break;

            case R.id.emailAuth_btn: //다이얼로그 내의 인증번호 인증 버튼을 눌렀을 시

                user_answer = emailAuth_number.getText().toString();
                if (user_answer.equals(randomCode) && !user_answer.equals("")) {
                    Toast.makeText(this, "이메일 인증 성공" + user_answer + "/" + randomCode, Toast.LENGTH_SHORT).show();
                    authDialog.dismiss();
                    Intent intent = new Intent(
                            getApplicationContext(), // 현재 화면의 제어권자
                            RegisterFragment.class); // 다음 넘어갈 클래스 지정
                    startActivity(intent); // 다음 화면으로 넘어간다

                } else {
                    Toast.makeText(this, "이메일 인증 실패" + user_answer + "/" + randomCode, Toast.LENGTH_SHORT).show();
                    authDialog.dismiss();
                }
                break;
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        countDownTimer.cancel();
    } //다이얼로그 닫을 때 카운트 다운 타이머의 cancel()메소드 호출

}