package com.example.project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class MembershipActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_membership);
    }

    //GS 눌렀을 때 -> GSMembershipCategoryActivity로 이동
    public void onButtonGSClicked(View view) {
        Intent intent = new Intent(this,GSMembershipCategoryActivity.class);
        startActivity(intent);
    }

    //CU 눌렀을 때 -> CUMembershipCategoryActivity로 이동
    public void onButtonCUClicked(View view) {
        Intent intent = new Intent(this,CUMembershipCategoryActivity.class);
        startActivity(intent);
    }

    //SevenEleven 눌렀을 때 -> SevenElevenMembershipCategoryActivity로 이동
    public void onButtonSevenElevenClicked(View view) {
        Intent intent = new Intent(this,SevenElevenMembershipCategoryActivity.class);
        startActivity(intent);
    }

    //Emart 눌렀을 때 -> EmartMembershipCategoryActivity로 이동
    public void onButtonEmartClicked(View view) {
        Intent intent = new Intent(this,EmartMembershipCategoryActivity.class);
        startActivity(intent);
    }



}
