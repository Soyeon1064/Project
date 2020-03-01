package com.example.project;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;

import java.util.Locale;

import static android.util.Log.ERROR;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private String introduce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);

        tts = new TextToSpeech(this,this);
    }

    //Button-> 앱 도움말 버튼 클릭
    public void onTTSButtonClicked(View view) {

        introduce="안녕하세요. 니 편의점과 내 편의점 모두가 편리한 니편내편 입니다. 우리 어플은 시각 장애인을 대상으로 \n" +
                "편의점을 쉽게 사용할 수 있도록 만든 어플로, 행사상품 찾기,, 근처에 어떤 편의점이 있는지 등을 알려주고 있습니다. 아래 버튼에는\n" +
                "사물 인식. 멤버십 정보. 근처 편의점 찾기 순서로 배치되어 있습니다. 시각 장애인 분들이 더욱 편리한 서비스를 이용할 수 있도록 노력하겠습니다. ";

        tts.setPitch(0.9f);
        tts.setSpeechRate(1.0f); //읽는 속도 기본 설정
        tts.speak(introduce, TextToSpeech.QUEUE_FLUSH,null);

    }

    //Button2-> 사물인식_카메라 촬영 버튼 클릭
    public void onButtonCameraClicked(View view) {
        Intent intent = new Intent(this,CameraActivity.class);
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





    //앱 설명서 관련 ->tts
    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.shutdown();
    }


    @Override
    public void onInit(int status) {
        if(status != ERROR){
            //언어 선택
            tts.setLanguage(Locale.KOREAN);
        }
    }

}
