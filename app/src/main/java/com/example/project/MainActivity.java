package com.example.project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private String introduce;
    private TTSAdapter tts; //TTS 사용하고자 한다면 1) 클래스 객체 선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);

    }

    //Button-> 앱 도움말 버튼 클릭
    public void onTTSButtonClicked(View view) {

        introduce="시각 장애인을 대상으로 \n" +
                "편의점을 쉽게 사용할 수 있도록 만든 어플 니편 내편 입니다. 아래 버튼에는 행사 상품을 알려주는 상품 인식. \n" +
                "멤버십 정보. 가까운 편의점 찾기 순서로 배치되어 있습니다.";

        tts = new TTSAdapter(this,introduce); //TTS 사용하고자 한다면 2) 클래스 객체 생성
    }

    //Button2-> 사물인식_카메라 촬영 버튼 클릭
    public void onButtonCameraClicked(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    //Button3-> 멤버십 안내 버튼 클릭
    public void onMembershipButtonClicked(View view) {
        Intent intent = new Intent(this, MembershipActivity.class);
        startActivity(intent);

    }

    //Button4-> 근처 편의점 찾기 버튼 클릭
    public void onLocationButtonClicked(View view) {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);

    }


    //TTS 사용하고자 한다면 3) 액티비티가 꺼졌을 시 TTS 음성도 꺼지게 해주기 > 아래 '두' 메소드 복사 붙여넣기.

//    //어플이 꺼지거나 중단 된다면 TTS 어댑터의 ttsShutdown() 메소드 호출하기
//    protected void onDestroy() {
//        super.onDestroy();
//        //tts.ttsShutdown();
//        Toast.makeText(this, "Main]] onDestroy 실행", Toast.LENGTH_SHORT).show();
//    }
//
//    //오류 발생 > main 액티비티에서 다른 액티비티로 이동할 때 당연히 tts를 꺼야 하는데 오류가 나서...참
//    protected void onStop(){
//        super.onStop();
//        Toast.makeText(this, "Main]] onStop 실행", Toast.LENGTH_SHORT).show();
//    }


}
