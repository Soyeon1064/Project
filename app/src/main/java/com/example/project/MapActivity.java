package com.example.project;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.content.Context;
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
import java.util.ArrayList;

public class MapActivity extends AppCompatActivity {

    private TTSAdapter tts;
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

        //마커 아이콘 bitmap은 현재 위치 마커 / bitmap2는 편의점 위치 마커
        bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.pin1);
        bitmap2 = BitmapFactory.decodeResource(this.getResources(), R.drawable.pin2);

        tMapView = new TMapView(context);

        //지도 초기 설정
        tMapView.setSKTMapApiKey("l7xx8af54a909a6e4bb8a498c7628aae0720");
        tMapView.setCompassMode(true);
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

                        String itemInfoString = itemInfo.getPOIName();
                        textView.setText(itemInfoString);

                        //아직 안 되네.
                        tts = new TTSAdapter(MapActivity.this,"가장 가까운 편의점은 "); //TTS 사용하고자 한다면 2) 클래스 객체 생성
                    }
                }catch(Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }


    //어플이 꺼지거나 중단 된다면 TTS 어댑터의 ttsShutdown() 메소드 호출하기
    protected void onDestroy() {
        super.onDestroy();
        tts.ttsShutdown();
    }

}

