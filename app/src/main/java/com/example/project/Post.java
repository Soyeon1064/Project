package com.example.project;

import com.google.gson.annotations.SerializedName;

//JSON 데이터를 class로 받아오기 위한 작업.
//데이터 key값들을 변수화 한다.
public class Post {
    //JSON 객체를 매칭해 준다.
    @SerializedName("userId")
    private int userId;

    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("body")
    private String body;

    public int getUserId(){
        return userId;
    }
    public void setUserId(){
        this.userId = userId;
    }

    public int getId(){
        return id;
    }
    public void setId(){
        this.id = id;
    }

    public String getTitle(){
        return title;
    }
    public void setTitle(){
        this.title = title;
    }

    public String getBody(){
        return body;
    }
    public void setBody(String body){
        this.body = body;
    }

}
