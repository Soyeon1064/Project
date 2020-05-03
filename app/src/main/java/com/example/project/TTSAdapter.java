package com.example.project;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

//TTS를 편하게 사용하기 위한 TTSAdapter 클래스
public class TTSAdapter extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private TextToSpeech tts; //TTS 객체
    private String content;  //출력물

    //생성자
    public TTSAdapter(Context context, String content){
        this.content = content;

        //TTS 객체 생성
        //this는 TextToSpeech.OnInitListener를 가리키며,
        //onInit을 호출한다.
        tts = new TextToSpeech(context, this);
    }

    //TTS 객체를 생성하면 호출되는 메소드
    @Override
    public void onInit(int status) {
        if (status != TextToSpeech.ERROR) { //TTS의 상태가 정상이라면
            tts.setLanguage(Locale.KOREAN); //언어-한국어 설정
            tts.setPitch(0.9f); //음성 톤 (1.0f 기본)
            tts.setSpeechRate(1.0f); //읽는 속도 (1.0f 기본)
            tts.speak(content, TextToSpeech.QUEUE_FLUSH, null, null); //음성을 출력
        }
    }

    //TTS 끝나면 TTS shutdown.
    protected void finalize() {
        tts.shutdown();
    }

    //어플이 꺼지거나 중단 된다면 TTS shutdown.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.shutdown();
    }
}
