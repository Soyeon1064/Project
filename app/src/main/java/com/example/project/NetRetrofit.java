package com.example.project;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//인터페이스 RetrofitService를 이용하여 실질적인
//전송 역할을 처리하는 클래스
public class NetRetrofit {

    //baseUrl 설정
    private String BASE_URL = "http://18.222.224.247:8080";

    //싱글톤으로 구현하려면 private로
    private static NetRetrofit ourInstance = new NetRetrofit();
    public static NetRetrofit getInstance(){
        return ourInstance;
    }

    public NetRetrofit(){

    }

    Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();

    RetrofitService service = retrofit.create(RetrofitService.class);

    public RetrofitService getService(){
        return service;
    }


}
