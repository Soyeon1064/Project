package com.example.project;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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

        introduce="안녕하세요. 니 편의점과 내 편의점 모두가 편리한 니편내편 입니다. 우리 어플은 시각 장애인을 대상으로 \n" +
                "편의점을 쉽게 사용할 수 있도록 만든 어플로, 행사상품 찾기,, 근처에 어떤 편의점이 있는지 등을 알려주고 있습니다. 아래 버튼에는\n" +
                "사물 인식. 멤버십 정보. 근처 편의점 찾기 순서로 배치되어 있습니다. 시각 장애인 분들이 더욱 편리한 서비스를 이용할 수 있도록 노력하겠습니다. ";

        tts = new TTSAdapter(this,introduce); //TTS 사용하고자 한다면 2) 클래스 객체 생성
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


}
