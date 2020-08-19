package com.example.project;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class EmartMembershipCategoryActivity extends AppCompatActivity {
    Button button1, button2, button3;
    TTSAdapter tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emart_membership_category);

        tts = TTSAdapter.getInstance(this);

        //로고 터치
        button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //음성 설명
                tts.speak("이마트24 멤버십입니다. 제휴멤버십, 자체멤버십 " +
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

        //자체 멤버십 터치
        button3 = (Button) findViewById(R.id.button2);
        button3.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                retrofitGet("자체멤버십");
            }
        });

    }

    private void retrofitGet(String b_type) {
    }
}
