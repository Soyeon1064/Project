package com.example.project;

import android.graphics.Bitmap;
import android.media.Image;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

//웹서버와 통신을 하는 REST API를 정의하기 위한 interface
public interface RetrofitService {
    //서버에서 데이터를 얻는 GET: 상품 정보
    @GET("/posts")
    Call<List<Post>> getData(@Query("userId") String userId);

//    product productInfo와 같은 변수명은 장고 쪽이랑 동일해야 함.
//    @GET("/admin")
//    Call<List<POST>> getData(@Query("product") String productInfo);



    //서버에게 데이터를 전송하는 UT > 이미지를 그대로 전달해보기
    //POST는 새로운 정보를 서버에 등록시킨다.
    // @FormUrlEncoded Field 형식 사용 시 Form이 Encoding 되어야 하기 때문에 사용한다.
    @FormUrlEncoded
    @POST("/posts")
    Call<Post> postData(@FieldMap HashMap<String, Object> param);
}
