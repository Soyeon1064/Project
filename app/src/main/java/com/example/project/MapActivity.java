package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.AlertDialog;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.skt.Tmap.TMapCircle;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapData.FindAroundNamePOIListenerCallback;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//뒤로가기 버튼을 누를 때 화면이 꺼지는 문제 발생
//TTS가 안 되는 문제 발생
public class MapActivity extends AppCompatActivity {

    private TTSAdapter tts;
    String itemInfoString=null;

    //권한 관련
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};



    TMapView tMapView = null;
    double latitude;;
    double longitude;
    Context context;
    //원 크기 지정
    double minRadius = 500; // 500 meters
    double radius100 = 100; // 100 meters
    GpsTracker gps_tracker = null;

    //마커 핀 이미지
    TMapMarkerItem markerItem1;
    Bitmap bitmap, bitmap2;
    TMapPoint tmappoint; //현재 위치 포인트
    TextView textView;


    @Override
    //액티비티가 처음 실행될 때
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        context = getApplicationContext();

        tts = TTSAdapter.getInstance(this);

        if(checkPermission()==false){
            tts.speak("위치 권한을 허용해 주세요.");
        }


        //마커 아이콘 bitmap은 현재 위치 마커 / bitmap2는 편의점 위치 마커
        bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.pin1);
        bitmap2 = BitmapFactory.decodeResource(this.getResources(), R.drawable.pin2);

        tMapView = new TMapView(context);

        //지도 초기 설정
        tMapView.setSKTMapApiKey("l7xx8af54a909a6e4bb8a498c7628aae0720");
        tMapView.setCompassMode(false); //이 부분을 true로 하면 나침반 모드가 적용되어 핸드폰이 움직이는 방향대로 지도도 따라 움직임.
        tMapView.setIconVisibility(true);
        tMapView.setZoomLevel(16);
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);
        //tMapView.setTrackingMode(true);
        tMapView.setSightVisible(true);


        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.linearLayoutTmap);
        textView = (TextView)findViewById(R.id.textView5); //가장 가까운 편의점이 어디인지 알려주는 텍스트뷰

        //현재 위치, 주변 편의점 위치를 알려주는 메소드
        findCVS();

        ////리니어 레이아웃에 지도 연결
        linearLayout.addView(tMapView);
    }

    //메인 화면에서 권한 받아 왔는지 확인하는 메소드
    private boolean checkPermission() {

        //카메라, 저장 권한 확인할 String 값
        String tmp="";

        //위치 권한1 확인 > 권한 승인되지 않으면 tmp에 권한 내용 저장
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            tmp += Manifest.permission.ACCESS_FINE_LOCATION+" ";
        }

        //위치 권한2 확인
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            tmp += Manifest.permission.ACCESS_COARSE_LOCATION;
        }

        //권한이 허용되지 않았다면
        if(TextUtils.isEmpty(tmp)==false){
            //권한 요청하기
            //tts.speak("위치 권한을 허용해 주세요.");
            ActivityCompat.requestPermissions(this, tmp.trim().split(" "), 1);
            return false;
        }else{
            Log.d("상황: ","가장 가까운 편의점 메뉴로 들어와서 권한 허용됨을 확인함.");
            return true;
        }
    }

    //권한에 대한 응답이 있을때 자동 작동하는 함수
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //권한 허용했을 경우 어차피 권한 하나라서 for 문 돌리고 그럴 필요가 없음.
        if(requestCode == 1){
            //MapActivity 새로고침
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }

    //원 그리기
    protected void drawCircle() {

        TMapPoint tMapPoint = new TMapPoint(latitude, longitude);

        //위치 옮기고 원을 다시 그려주기 전에 이전의 원 지우기
        tMapView.removeAllTMapCircle();

        //500m 원
        TMapCircle tMapCircle = new TMapCircle();
        tMapCircle.setCenterPoint(tMapPoint);
        tMapCircle.setRadius(minRadius);
        tMapCircle.setCircleWidth(1);
        tMapCircle.setLineColor(Color.GRAY);
        tMapCircle.setAreaColor(Color.GRAY);
        tMapCircle.setAreaAlpha(50);
        tMapView.addTMapCircle("circle1", tMapCircle);

        //100m 원
        TMapCircle tMapCircleSmall = new TMapCircle();
        tMapCircleSmall.setCenterPoint(tMapPoint);
        tMapCircleSmall.setRadius(radius100);
        tMapCircleSmall.setCircleWidth(1);
        tMapCircle.setLineColor(Color.LTGRAY);
        tMapCircleSmall.setAreaColor(Color.LTGRAY);
        tMapCircleSmall.setAreaAlpha(50);
        tMapView.addTMapCircle("circle2", tMapCircleSmall);

    }

    //편의점 찾기
    protected void findCVS() {
        TMapData tMapData = new TMapData();



        //지도를 내 현재위치로, 지도의 센터포인트를 내 현재위치로
        gps_tracker = new GpsTracker(this, tMapView);
        latitude = gps_tracker.getLatitude();
        longitude = gps_tracker.getLongitude();
        tmappoint = new TMapPoint(latitude, longitude);

        markerItem1 = new TMapMarkerItem();
        markerItem1.setIcon(bitmap); //마커핀 이미지 연결
        markerItem1.setPosition(0.5f ,1.0f); //마커핀 위치 조정
        markerItem1.setTMapPoint(tmappoint); //마커핀 위치 연결
        tMapView.addMarkerItem("현재 나의 위치", markerItem1);

        //지도 중심 좌표 조정
        tMapView.setCenterPoint(longitude, latitude, false);
        tMapView.setLocationPoint(longitude, latitude);

        //500m, 100m 원 그리는 메소드
        drawCircle();


        //"편의점" 키워드로 검색
        tMapData.findAroundNamePOI(tmappoint, "편의점", new FindAroundNamePOIListenerCallback() {
            @Override
            public void onFindAroundNamePOI(ArrayList poiItem) {
                if (poiItem == null) return;

                TMapPoint tMapPointStart = new TMapPoint(latitude, longitude); // 출발지
                tMapView.removeAllMarkerItem();
                double minDistance = Double.POSITIVE_INFINITY;
                TMapPoint minDistancePoint = null;


                //텍스트 뷰에 넣을 편의점 정보
                TMapPOIItem itemInfo = new TMapPOIItem();

                for (int i = 0; i < poiItem.size(); i++) {
                    TMapPOIItem item = (TMapPOIItem) poiItem.get(i);
                    double distance = item.getDistance(tMapPointStart);


                    //500m 안에 있는 편의점들 마커핀으로 표시
                    if (distance < minRadius) {
                        TMapMarkerItem markerItem = new TMapMarkerItem();
                        markerItem.setIcon(bitmap2);
                        markerItem.setPosition(0.5f ,1.0f);
                        markerItem.setTMapPoint(item.getPOIPoint()); // 마커의 좌표 지정
                        markerItem.setName(item.getPOIName().toString());
                        markerItem.setCanShowCallout(true);
                        markerItem.setCalloutTitle(item.getPOIName().toString());
                        tMapView.addMarkerItem("poi_" + i, markerItem);


                        TMapPoint tMapPointEnd = item.getPOIPoint();
                        if (distance < minDistance) {
                            minDistance = distance;
                            minDistancePoint = tMapPointEnd;
                            itemInfo = (TMapPOIItem) poiItem.get(i);

                        }
                    }
                }

                //내 위치 마커핀이 사라져서 다시 설정함.
                markerItem1.setTMapPoint(tmappoint);
                tMapView.addMarkerItem("현재 나의 위치", markerItem1);

                try {
                    TMapPolyLine minDistancePolyLine = (new TMapData()).findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, tMapPointStart, minDistancePoint);

                    // //내 위치 마커핀이 사라져서 다시 설정함.
                    markerItem1.setTMapPoint(tmappoint);
                    tMapView.addMarkerItem("현재 나의 위치", markerItem1);

                    //가까운 거리 인식 되면 선 그리고 편의점 정보 텍스트뷰에 올리기, 음성 출력하기
                    if (minDistancePolyLine != null) {

                        minDistancePolyLine.setLineColor(R.color.lineColor);
                        minDistancePolyLine.setOutLineColor(R.color.lineColor);
                        minDistancePolyLine.setLineWidth(5);
                        tMapView.addTMapPolyLine("minDistanceLine", minDistancePolyLine);
                        itemInfoString = itemInfo.getPOIName();

                        try {
                            textView.setText(itemInfoString);   // exception 발생함; 이유는 모름
                        } catch(Exception e) {
                            e.printStackTrace();
                        }

                        tts.speak("가장 가까운 편의점은 " + itemInfoString + "입니다.");
                    }
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public String getCurrentAddress( double latitude, double longitude) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }



        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 활성화하시겠습니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "onActivityResult : GPS 활성화 되어있음");
                        if(checkPermission()==true) return;
                    }
                }

                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    //어플이 꺼지거나 중단 된다면 TTS 어댑터의 ttsShutdown() 메소드 호출하기
    protected void onDestroy() {
        super.onDestroy();
        tts.stop();
    }

    //액티비티 중지되면 실행되는 메소드
    //다른 액티비티 화면에 가려졌을시 음성 종료
    protected void onPause() {
        super.onPause();
        tts.stop();
    }

    protected void onStop() {
        super.onStop();
        tts.stop();
    }


}




/*package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.AlertDialog;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.skt.Tmap.TMapCircle;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapData.FindAroundNamePOIListenerCallback;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//뒤로가기 버튼을 누를 때 화면이 꺼지는 문제 발생
//TTS가 안 되는 문제 발생
public class MapActivity extends AppCompatActivity {

    private TTSAdapter tts;
    String itemInfoString=null;

    //권한 관련
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};



    TMapView tMapView = null;
    double latitude=0;
    double longitude=0;
    Context context;
    //원 크기 지정
    double minRadius = 500; // 500 meters
    double radius100 = 100; // 100 meters
    GpsTracker gps_tracker = null;

    //마커 핀 이미지
    TMapMarkerItem markerItem1;
    Bitmap bitmap, bitmap2;
    TMapPoint tmappoint; //현재 위치 포인트
    TextView textView;

    @Override
    //액티비티가 처음 실행될 때
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        context = getApplicationContext();

        //권한 확인 메소드 호출
        if(checkPermission()==false) {
            //tts = new TTSAdapter(this,"상품 진열대에서 반 발자국 물러서서 촬영을 진행해 주세요. 화면을 터치하면 촬영이 진행됩니다."); //TTS 사용하고자 한다면 2) 클래스 객체 생성
            tts = new TTSAdapter(this, "위치 권한을 모두 허용해 주세요."); //TTS 사용하고자 한다면 2) 클래스 객체 생성
        }

        //마커 아이콘 bitmap은 현재 위치 마커 / bitmap2는 편의점 위치 마커
        bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.pin1);
        bitmap2 = BitmapFactory.decodeResource(this.getResources(), R.drawable.pin2);

        tMapView = new TMapView(context);

        //지도 초기 설정
        tMapView.setSKTMapApiKey("l7xx8af54a909a6e4bb8a498c7628aae0720");
        tMapView.setCompassMode(false); //이 부분을 true로 하면 나침반 모드가 적용되어 핸드폰이 움직이는 방향대로 지도도 따라 움직임.
        tMapView.setIconVisibility(true);
        tMapView.setZoomLevel(16);
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);
        //tMapView.setTrackingMode(true);
        tMapView.setSightVisible(true);


        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.linearLayoutTmap);
        textView = (TextView)findViewById(R.id.textView5); //가장 가까운 편의점이 어디인지 알려주는 텍스트뷰


        //현재 위치, 주변 편의점 위치를 알려주는 메소드
        findCVS();

        //아직 값이 안 들어오면 계속 반복 >계속 기다리고자 함.
        while(itemInfoString==null){}
        tts = new TTSAdapter(context, "가장 가까운 편의점은 "+itemInfoString+"입니다.");
        Log.d("상황: ","while 끝나고 나서 이제 음성 출력");

        ////리니어 레이아웃에 지도 연결
        linearLayout.addView(tMapView);
    }

    //메인 화면에서 권한 받아 왔는지 확인하는 메소드
    private boolean checkPermission() {
        MainActivity m = new MainActivity();
        Context mContext = m.getMainContext();

        //카메라, 저장 권한 확인할 String 값
        String tmp="";

        //카메라 권한 확인 > 권한 승인되지 않으면 tmp에 권한 내용 저장
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            tmp += Manifest.permission.ACCESS_FINE_LOCATION+" ";
        }

        //카메라 저장 권한 확인
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            tmp += Manifest.permission.ACCESS_COARSE_LOCATION;
        }

        //권한이 허용되지 않았다면
        if(TextUtils.isEmpty(tmp)==false){
            //권한 요청하기
            ActivityCompat.requestPermissions(this, tmp.trim().split(" "), 1);
            return false;
        }else{
            Log.d("상황: ","가장 가까운 편의점 메뉴로 들어와서 권한 허용됨을 확인함.");
            return true;
        }
    }

    //원 그리기
    protected void drawCircle() {

        TMapPoint tMapPoint = new TMapPoint(latitude, longitude);

        //위치 옮기고 원을 다시 그려주기 전에 이전의 원 지우기
        tMapView.removeAllTMapCircle();

        //500m 원
        TMapCircle tMapCircle = new TMapCircle();
        tMapCircle.setCenterPoint(tMapPoint);
        tMapCircle.setRadius(minRadius);
        tMapCircle.setCircleWidth(1);
        tMapCircle.setLineColor(Color.GRAY);
        tMapCircle.setAreaColor(Color.GRAY);
        tMapCircle.setAreaAlpha(50);
        tMapView.addTMapCircle("circle1", tMapCircle);

        //100m 원
        TMapCircle tMapCircleSmall = new TMapCircle();
        tMapCircleSmall.setCenterPoint(tMapPoint);
        tMapCircleSmall.setRadius(radius100);
        tMapCircleSmall.setCircleWidth(1);
        tMapCircle.setLineColor(Color.LTGRAY);
        tMapCircleSmall.setAreaColor(Color.LTGRAY);
        tMapCircleSmall.setAreaAlpha(50);
        tMapView.addTMapCircle("circle2", tMapCircleSmall);

    }

    //편의점 찾기
    protected void findCVS() {
        TMapData tMapData = new TMapData();



        //지도를 내 현재위치로, 지도의 센터포인트를 내 현재위치로
        gps_tracker = new GpsTracker(this, tMapView);
        latitude = gps_tracker.getLatitude();
        longitude = gps_tracker.getLongitude();
        tmappoint = new TMapPoint(latitude, longitude);

        //만약 현재위치가 안 뽑히면
        if(latitude==0){
            tts=new TTSAdapter(this, "위치를 키고 다시 실행해 주세요");
        }

        markerItem1 = new TMapMarkerItem();
        markerItem1.setIcon(bitmap); //마커핀 이미지 연결
        markerItem1.setPosition(0.5f ,1.0f); //마커핀 위치 조정
        markerItem1.setTMapPoint(tmappoint); //마커핀 위치 연결
        tMapView.addMarkerItem("현재 나의 위치", markerItem1);

        //지도 중심 좌표 조정
        tMapView.setCenterPoint(longitude, latitude, false);
        tMapView.setLocationPoint(longitude, latitude);

        //500m, 100m 원 그리는 메소드
        drawCircle();


        //"편의점" 키워드로 검색
        tMapData.findAroundNamePOI(tmappoint, "편의점", new FindAroundNamePOIListenerCallback() {
            @Override
            public void onFindAroundNamePOI(ArrayList poiItem) {
                if (poiItem == null) return;

                TMapPoint tMapPointStart = new TMapPoint(latitude, longitude); // 출발지
                tMapView.removeAllMarkerItem();
                double minDistance = Double.POSITIVE_INFINITY;
                TMapPoint minDistancePoint = null;


                //텍스트 뷰에 넣을 편의점 정보
                TMapPOIItem itemInfo = new TMapPOIItem();

                for (int i = 0; i < poiItem.size(); i++) {
                    TMapPOIItem item = (TMapPOIItem) poiItem.get(i);
                    double distance = item.getDistance(tMapPointStart);


                    //500m 안에 있는 편의점들 마커핀으로 표시
                        if (distance < minRadius) {
                        TMapMarkerItem markerItem = new TMapMarkerItem();
                        markerItem.setIcon(bitmap2);
                        markerItem.setPosition(0.5f ,1.0f);
                        markerItem.setTMapPoint(item.getPOIPoint()); // 마커의 좌표 지정
                        markerItem.setName(item.getPOIName().toString());
                        markerItem.setCanShowCallout(true);
                        markerItem.setCalloutTitle(item.getPOIName().toString());
                        tMapView.addMarkerItem("poi_" + i, markerItem);


                        TMapPoint tMapPointEnd = item.getPOIPoint();
                        if (distance < minDistance) {
                            minDistance = distance;
                            minDistancePoint = tMapPointEnd;
                            itemInfo = (TMapPOIItem) poiItem.get(i);

                        }
                    }
                }

                //내 위치 마커핀이 사라져서 다시 설정함.
                markerItem1.setTMapPoint(tmappoint);
                tMapView.addMarkerItem("현재 나의 위치", markerItem1);

                try {
                    TMapPolyLine minDistancePolyLine = (new TMapData()).findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, tMapPointStart, minDistancePoint);

                    // //내 위치 마커핀이 사라져서 다시 설정함.
                    markerItem1.setTMapPoint(tmappoint);
                    tMapView.addMarkerItem("현재 나의 위치", markerItem1);

                    //가까운 거리 인식 되면 선 그리고 편의점 정보 텍스트뷰에 올리기, 음성 출력하기
                    if (minDistancePolyLine != null) {

                        minDistancePolyLine.setLineColor(R.color.lineColor);
                        minDistancePolyLine.setOutLineColor(R.color.lineColor);
                        minDistancePolyLine.setLineWidth(5);
                        tMapView.addTMapPolyLine("minDistanceLine", minDistancePolyLine);
                        Log.d("상황: ","getPOIName 되기 전");
                        itemInfoString = itemInfo.getPOIName();
                        Log.d("상황: ","getPOIName 된 후에");
                        textView.setText(itemInfoString);
                        Log.d("상황: ",itemInfoString+"텍스트뷰 설정 끝");

                    }
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public String getCurrentAddress( double latitude, double longitude) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }



        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }

//
//    //여기부터는 GPS 활성화를 위한 메소드들
//    private void showDialogForLocationServiceSetting() {
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("위치 서비스 비활성화");
//        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
//                + "위치 설정을 활성화하시겠습니까?");
//        builder.setCancelable(true);
//        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int id) {
//                Intent callGPSSettingIntent
//                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
//            }
//        });
//        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int id) {
//                dialog.cancel();
//            }
//        });
//        builder.create().show();
//    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되어있음");
                        if(checkPermission()==true) return;
                    }
                }

                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    //전체 화면 버튼을 눌렀을 경우 음성으로 출력하는 메소드
    public void OnResultButtonClicked(View view) {

//        if(itemInfoString!=null){
//            tts = new TTSAdapter(context, "가장 가까운 편의점은 "+itemInfoString+"입니다.");
//        }else{
//            tts = new TTSAdapter(context, "오류입니다. 다시 한 번 실행해 주세요.");
//        }
    }

//    //어플이 꺼지거나 중단 된다면 TTS 어댑터의 ttsShutdown() 메소드 호출하기
//    protected void onDestroy() {
//        super.onDestroy();
//        //tts.ttsShutdown();
//    }
//
//
//    //오류 발생 > main 액티비티에서 다른 액티비티로 이동할 때 당연히 tts를 꺼야 하는데 오류가 나서...참
//    protected void onStop(){
//        super.onStop();
//        Toast.makeText(this, "Map]] onStop 실행", Toast.LENGTH_SHORT).show();
//    }

}*/

