package com.example.project;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class CUMembershipCategoryActivity extends AppCompatActivity {

    Button button1, button2, button3, button4;
    TTSAdapter tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cumembership_category);

        tts = TTSAdapter.getInstance(this);

        //로고 터치
        button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //음성 설명
                tts.speak("씨유 멤버십입니다. 제휴멤버십, 제휴카드, 자체멤버십 " +
                        "순으로 배치되어 있습니다.");
            }
        });

        //제휴 멤버십 터치
        button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Log.d("상황: ","버튼 터치함");
                retrofitGet("제휴멤버십");
            }
        });

        //제휴 카드 터치
        button3 = (Button) findViewById(R.id.button2);
        button3.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                retrofitGet("제휴카드");
            }
        });

        //자체 멤버십 터치
        button4 = (Button) findViewById(R.id.button2);
        button4.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                retrofitGet("자체멤버십");
            }
        });

    }

    private void retrofitGet(String b_type) {
    }
}
