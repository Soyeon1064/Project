package com.example.project;

import com.google.gson.annotations.SerializedName;

//멤버십 정보를 가져올 때 사용하는 클래스
//@GET 장고 프로젝트 Benefits2 페이지 데이터 변수와 똑같이 맞추는 클래스
public class Benefits2 {

    //JSON 객체를 매칭해 준다.
    //id: 데이터 id인데 여기에서 쓰이지는 않음.
    @SerializedName("id")
    private String id;

    //conv_type: 편의점 분류
    @SerializedName("conv_type")
    private String conv_type;

    //b_type: 멤버십 분류
    @SerializedName("b_type")
    private String b_type;

    //b_name: 분류된 멤버십별 내용
    @SerializedName("b_name")
    private String b_name;

    //b_ex: b_name의 최종 결과
    @SerializedName("b_ex")
    private String b_ex;

    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id = id;
    }

    public String getConv_type(){
        return conv_type;
    }
    public void setConv_type(String conv_type){
        this.conv_type = conv_type;
    }

    public String getB_type(){
        return b_type;
    }
    public void setB_type(String b_type){
        this.b_type = b_type;
    }


    public String getB_name(){
        return b_name;
    }
    public void setB_name(String b_name){
        this.b_name = b_name;
    }


    public String getB_ex(){
        return b_ex;
    }
    public void setB_ex(String b_ex){
        this.b_name = b_ex;
    }



}
