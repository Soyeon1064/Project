package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GSMembershipCategoryActivity extends AppCompatActivity {
    Button button1, button2, button3, button4, button5;
    TTSAdapter tts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gsmembership_category);

        tts = TTSAdapter.getInstance(this);

        //로고 터치
        button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //음성 설명
                tts.speak("지에스25 멤버십입니다. 제휴멤버십, 제휴카드, 상품권, 자체멤버십 " +
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

        //상품권 터치
        button4 = (Button) findViewById(R.id.button2);
        button4.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                retrofitGet("상품권");
            }
        });

        //자체 멤버십 터치
        button5 = (Button) findViewById(R.id.button2);
        button5.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                retrofitGet("자체멤버십");
            }
        });


    }

    //멤버십 정보 끌어오는 메소드
    public void retrofitGet(String b_type){
        Log.d("상황: ","retrofitGet 메소드에 진입");

        Retrofit retrofit = new Retrofit.Builder(). baseUrl("http://18.222.224.247:8000/myapp/").addConverterFactory(GsonConverterFactory.create()).build();

        //@GET/@POST 설정해 놓은 인터페이스와 연결
        RetrofitService retrofitService = retrofit.create(RetrofitService.class);

        //conv_type이 GS인 데이터들을 가져온다.
        retrofitService.getData("GS").enqueue(new Callback<List<Benefits2>>() {

            //응답 성공했을 때
            @Override
            public void onResponse(@NonNull Call<List<Benefits2>> call, @NonNull Response<List<Benefits2>> response) {
                if(response.isSuccessful()){
                    List<Benefits2> data = response.body();
                    Log.d("상황: ","GET 성공");

                    ArrayList<String> array = new ArrayList<>();

                    for(Benefits2 re : data){
                        array.add(re.getB_type());
                    }

                    for(int i=0; i<array.size(); i++){
                        Log.d("상황: ",array.get(i));
                    }

                    //userId가 1인 정보들 중에서 첫 번째 title을 출력시켜본다.
                    //Log.d("상황: ", data.toString());
                }
            }

            //data.get(0).getTitle()
            //응답에 실패했을 때
            @Override
            public void onFailure(Call<List<Benefits2>> call, Throwable t) {
                Log.d("상황: ","GS GET 실패");
                t.printStackTrace();
            }
        });

    }

}
