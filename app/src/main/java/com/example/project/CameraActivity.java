package com.example.project;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

//상품 인식 activity
public class CameraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

    }


    public void ButtonChecked(View view) {
        Toast.makeText(this, "버튼이 눌렸다.", Toast.LENGTH_SHORT).show();
    }

}
