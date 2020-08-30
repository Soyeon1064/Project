package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SevenElevenMembershipCategoryActivity extends AppCompatActivity implements GetMembershipInfo{

    Button button1, button2, button3;
    TTSAdapter tts;
    //하나의 문장으로 합칠 문자열 변수
    String s1=""; String s2="";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seven_eleven_membership_category);

        tts = TTSAdapter.getInstance(this);
        getRetrofit();

        //로고 터치
        button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //음성 설명
                tts.speak("세븐일레븐 멤버십입니다. 제휴멤버십, 제휴카드 " +
                        "순으로 배치되어 있습니다.");
            }
        });

        //제휴 멤버십 터치
        button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String s="";
                Log.d("상황: ","*****************제휴멤버십 클릭");
                String[] result = getInfo("제휴멤버십").split("///");
                for(int i=0; i<result.length; i++){
                    Log.d("상황: ",(i+1)+"번. "+result[i]);
                    s+=(i+1)+"번. "+result[i];
                }
                tts.speak(s);
            }
        });

        //제휴 카드 터치
        button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String s="";
                Log.d("상황: ","*****************제휴카드 클릭");
                String[] result = getInfo("제휴카드").split("///");
                for(int i=0; i<result.length; i++){
                    Log.d("상황: ",(i+1)+"번. "+result[i]);
                    s+=(i+1)+"번. "+result[i];
                }
                tts.speak(s);
            }
        });

    }

    //각 멤버십 액티비티가 열리면 아래 메소드를 실행한다.
    @Override
    public void getRetrofit(){
        Log.d("상황: ","retrofitGet 메소드에 진입");

        Retrofit retrofit = new Retrofit.Builder(). baseUrl("http://18.222.224.247:8000/myapp/").addConverterFactory(GsonConverterFactory.create()).build();


        //@GET/@POST 설정해 놓은 인터페이스와 연결
        RetrofitService retrofitService = retrofit.create(RetrofitService.class);

        //데이터 가져오기
        //GS라고 입력해도 전체 정보를 다 끌어오더라. 이상한 문구를 넣어도 그냥 뽑아옴
        retrofitService.getData("GS").enqueue(new Callback<List<Benefits2>>() {

            //응답 성공했을 때- json 파일 가져오기
            @Override
            public void onResponse(@NonNull Call<List<Benefits2>> call, @NonNull Response<List<Benefits2>> response) {
                if(response.isSuccessful()){

                    List<Benefits2> data = response.body();
                    Log.d("상황: ","GET 성공");

                    //제휴멤버십,제휴카드
                    for(int i=0; i<data.size(); i++){
                        if(data.get(i).getConv_type().equals("seven")){
                            if(data.get(i).getB_type().equals("제휴멤버십")){
                                s1 +=data.get(i).getB_name()+", "+data.get(i).getB_ex()+"///";
                            }else{ //제휴카드
                                s2 +=data.get(i).getB_name()+", "+data.get(i).getB_ex()+"///";
                            }
                        }else{
                            continue;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Benefits2>> call, Throwable t) {
                Log.d("상황: ","seven GET 실패");
                t.printStackTrace();
            }
        });
    }

    //seven 멤버십 정보 중에서 어떤 정보를 보내줄지 분류
    @Override
    public String getInfo(String b_type) {
        if(b_type.equals("제휴멤버십")){
            return s1;
        }else { //제휴카드
            return s2;
        }
    }
}
