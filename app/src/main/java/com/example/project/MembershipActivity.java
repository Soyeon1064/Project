package com.example.project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class MembershipActivity extends AppCompatActivity {

    //TTS 사용하고자 한다면 1) 클래스 객체 선언
    private TTSAdapter tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_membership);

        tts = TTSAdapter.getInstance(this);  // onCreate 에서 한 번만 호출하면 됨
        tts.speak("지에쓰25. 씨유. 세븐 일레븐. 이마트 24 순으로 배치되어 있습니다.");
    }

    //GS 눌렀을 때 -> GSMembershipCategoryActivity로 이동
    public void onButtonGSClicked(View view) {
        Intent intent = new Intent(this,GSMembershipCategoryActivity.class);
        startActivity(intent);
    }

    //CU 눌렀을 때 -> CUMembershipCategoryActivity로 이동
    public void onButtonCUClicked(View view) {
        Intent intent = new Intent(this,CUMembershipCategoryActivity.class);
        startActivity(intent);
    }

    //SevenEleven 눌렀을 때 -> SevenElevenMembershipCategoryActivity로 이동
    public void onButtonSevenElevenClicked(View view) {
        Intent intent = new Intent(this,SevenElevenMembershipCategoryActivity.class);
        startActivity(intent);
    }

    //Emart 눌렀을 때 -> EmartMembershipCategoryActivity로 이동
    public void onButtonEmartClicked(View view) {
        Intent intent = new Intent(this,EmartMembershipCategoryActivity.class);
        startActivity(intent);
    }



    //액티비티 중지되면 실행되는 메소드
    //다른 액티비티 화면에 가려졌을시 음성 종료
    protected void onPause() {
        super.onPause();
        tts.stop();
    }

    protected void onStop() {
        super.onStop();
        tts.stop();
    }

}
