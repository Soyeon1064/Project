package com.example.project;

//@GET 할 때 사용하는 인터페이스
public interface GetMembershipInfo {
    public void getRetrofit(); //Retrofit2 호출
    public String getInfo(String b_type); //멤버십 결과 정보 추출
}
