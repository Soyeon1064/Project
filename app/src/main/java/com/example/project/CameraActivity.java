package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.Image;
import android.media.ImageReader;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class CameraActivity extends AppCompatActivity {

    //권한 관련
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    double latitude;
    double longitude;
    GpsTracker gps_tracker = null;
    TMapPoint tmappoint; //현재 위치 포인트
    TMapView tMapView = null;
    String cvs_name = "";
    boolean cvs_found = false;

    //서버로 이미지 보낼 때 정상적인 각도로 변환하기 위해 사용하는 메소드
    Bitmap rotatedBitmap;
    File serverFile;
    String filepath;
    BaseApplication base;


    //TTS 사용하고자 한다면 1) 클래스 객체 선언
    private TTSAdapter tts;

    //버튼 효과음 관련 변수
    SoundManager sManager;

    //버튼, 텍스쳐뷰 레이아웃 변수
    private Button btnCapture;
    private TextureView textureView;
    private String TAG = "Camera Activity";

    //이미지 출력 상태 확인
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private String cameraId; //카메라 아이디- 어떤 카메라를 쓸 것인가?
    private CameraDevice cameraDevice; //카메라 장치
    private CameraCaptureSession cameraCaptureSessions; //사진 찍을 때 사용할 변수
    private CaptureRequest.Builder captureRequestBuilder; //사진 찍기 요청 빌더 변수
    private Size imageDimension; //이미지 치수를 받아오고 전달하는 변수
    private ImageReader imagerReader; //이미지 저장할 때 사용하는 변수

    //파일 저장 관련 변수들
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    //카메라 상태 콜백
    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {

        //카메라 장치가 잘 열렸을 때 실행되는 메소드 > 카메라 장치를 TextureView에 연결해서 사용자 화면에 보이게 하기.
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera; //카메라 장치 설정
            createCameraPreview(); //카메라 화면이 보이도록 설정하는 메소드 호출
        }

        //카메라 장치가 연결이 안 됐을 때 실행되는 메소드 > 카메라 장치를 닫는다.
        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        //카메라 장치에 오류가 났을 때 실행되는 메소드 > 카메라 장치를 닫는다. 장치를 비운다.
        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    //CameraActivity가 열리면 기본 실행되는 메소드
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //로딩 관련
        base = new BaseApplication();

        tMapView = new TMapView(this);
        tMapView.setHttpsMode(true);

        //지도 초기 설정
        tMapView.setSKTMapApiKey("l7xx8af54a909a6e4bb8a498c7628aae0720");  // H

        tts = TTSAdapter.getInstance(this);
        tts.speak("화면을 터치하면 촬영이 진행됩니다.");

        sManager = SoundManager.getInstance();

        // SoundMaanger 초기화, 사운드 등록
        sManager.init(this);
        sManager.addSound(0, R.raw.pianonotification);

        //activity_main.xml textureView 연결
        textureView = (TextureView) findViewById(R.id.textureView);
        assert textureView != null; //boolean과 비슷한 용도로 쓰이는 assert -검증문

        //텍스쳐뷰에 리스너 달기
        textureView.setSurfaceTextureListener(textureListener);
        //activity_main.xml btnCapture 연결
        btnCapture = (Button) findViewById(R.id.btnCapture);

        //촬영 버튼을 눌렀을 때
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //효과음 출력해야 되는데 tts가 흘러나오면 안 되니까 tts 중단
                tts.stop();
                sManager.play(0);

                takePicture(); //사진을 촬영 설정하는 메소드 호출

            }
        });
    }

    //사진 찍는 메소드
    private void takePicture() {
        //장치가 비어있으면 사진을 찍을 수 없으므로 return
        if (cameraDevice == null) return;
        //장치 잘 있으면 카메라 서비스 연결
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            //많은 카메라 중 현재 연결된 camera의 특징을 받아온다.
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            //일단 사진 크기는 null값
            Size[] jpegSizes = null;
            //특징 값이 있다면
            if (characteristics != null) {
                //카메라 특징에 맞게 사진 크기 설정
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }

            //캡처 이미지 사이즈 설정
            int width = textureView.getWidth();
            int height = textureView.getHeight();
            //int width = 640;
            //int height = 480;

            if (jpegSizes != null && jpegSizes.length > 0) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }

            //이미지 읽어들이기
            ImageReader reader = imagerReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurface = new ArrayList<>(2);
            outputSurface.add(reader.getSurface());
            outputSurface.add(new Surface(textureView.getSurfaceTexture()));

            //캡처 빌더 설정 > 사진 컨트롤, 초점 설정하는 것임.
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            //기본 장치 확인
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            //파일 저장 설정 고유 식별자.jpg로 파일 이름 설정해서 생성
            file = new File(Environment.getExternalStorageDirectory() + "/" + UUID.randomUUID().toString() + ".jpg");
            filepath = file.getPath();

            //이미지 읽어들이는 리스너
            ImageReader.OnImageAvailableListener readerListender = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                        //upload(file);
                        uploadImage(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) image.close();
                    }
                }

                //이미지 파일 저장 메소드
                private void save(byte[] bytes) throws IOException {
                    OutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(file);
                        outputStream.write(bytes);
                    } finally {
                        if (outputStream != null)
                            outputStream.close();
                    }
                }
            };


            reader.setOnImageAvailableListener(readerListender, mBackgroundHandler);

            //사진 촬영 콜백
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                //사진 촬영 성공 메소드
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
//                    tts.ttsShutdown();
//                    playSoundId=soundManager.playSound(0);
                    //저장 되었습니다. 토스트 창
                    //Toast.makeText(CameraActivity.this, "Saved "+file, Toast.LENGTH_SHORT).show();
                    //다시 촬영할 수 있도록 카메라 화면 보여주는 메소드 호출 > 하지만 우리는 이 부분을 수정해야겠지..
                    //1) 사진을 촬영하면 음성이 출력되기까지 사진 촬영을 못 하게 해야함.
                    createCameraPreview();
                }
            };

            //카메라 입장에서 사진 촬영
            cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {

                //설정
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try {
                        cameraCaptureSession.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                //설정 실패
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, mBackgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    //편의점 이름을 cvs_code로 변환(키워드로 변환)
    private String get_cvs_code(String cvs_info) {
        String code;
        if (cvs_info.trim().startsWith("세븐일레븐")) code = "seven";
        else if (cvs_info.trim().startsWith("이마트24")) code = "emart";
        else if (cvs_info.trim().startsWith("GS25")) code = "GS";
        else if (cvs_info.trim().startsWith("CU")) code = "CU";
        else code = "none";
        Log.d("CameraActivity", "cvs_info:" + cvs_info);
        Log.d("CameraActivity", "code: " + code);
        return code;
    }

    //                                myapp/imageupload version
    private void uploadImage(final File file) throws java.io.IOException {

        //로딩 다이얼로그 화면 구현
        startProgress();

        // FILE: device에서 storage access 권한 허용
        final String BASE_URL = "http://18.222.224.247:8000";  // aws
        //final String BASE_URL = "http://10.0.2.2:8000";   // local

        //1) 이미지 회전 최종 파일 serverFile
        //File을 Bitmap으로 변환
        try{
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
            //서버에 이미지 전송시 회전 정렬하는 메소드 호출
            rotatedBitmap = fixOrientation(bitmap, file.getPath());
        }catch(Exception e){
            e.printStackTrace();
            Log.d("상황: ","file 비트맵 변환 실패");
        }

        //반환된 rotatedBitmap을 다시 파일로 반환
        try{
            //serverFile = new File(Environment.getExternalStorageDirectory()+"/"+UUID.randomUUID().toString()+".jpg");
            serverFile = new File(filepath);
            OutputStream os = new BufferedOutputStream(new FileOutputStream(serverFile));
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.close();
        }catch(Exception e){
            e.printStackTrace();
            Log.d("상황: ", "서버로 보낼 이미지 파일 생성실패");
        }

        Log.d("상황: file.getName()과 file.getPath()는 ",serverFile.getName()+","+serverFile.getPath());


        //2) 위치
        gps_tracker = new GpsTracker(this);
        latitude = gps_tracker.getLatitude();
        longitude = gps_tracker.getLongitude();
//        latitude = 37.3400;
//        longitude = 127.1153;
        tmappoint = new TMapPoint(latitude, longitude);

        //"편의점" 키워드로 검색
        TMapData tMapData = new TMapData();
        tMapData.findAroundNamePOI(tmappoint, "편의점", new TMapData.FindAroundNamePOIListenerCallback() {
            @Override
            public void onFindAroundNamePOI(ArrayList poiItem) {
                if (poiItem == null) return;
                TMapPoint my_point = new TMapPoint(latitude, longitude); // 현재 위치

                //제일 가까운 편의점 찾기
                double min_distance = Double.POSITIVE_INFINITY;
                int min_index = -1;
                TMapPOIItem item;
                for (int i = 0; i < poiItem.size(); i++) {
                    item = (TMapPOIItem) poiItem.get(i);
                    double distance = item.getDistance(my_point);
                    if (distance < min_distance) {
                        min_distance = distance;
                        min_index = i;
                    }
                }

                //제일 가까운 편의점에서 20m 이내에 있으면 동일 편의점으로 간주
                if (min_index >= 0 && min_distance <= 20) { // 20 meters
                    item = (TMapPOIItem) poiItem.get(min_index);
                    cvs_name = item.getPOIName().toString();
                } else
                    cvs_name = "not_found";
                Log.d("CameraActivity", "편의점이름: " + cvs_name);
                // String title = cvs_name + "@(" + latitude + "," + longitude + ")";

                //편의점 이름을 cvs_code로 변환해서 title에 저장
                String title = get_cvs_code(cvs_name);
                Log.d("CameraActivity", "CVS name: " + title);

                Date now = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("title", title)
                        .addFormDataPart("image", serverFile.getName(), RequestBody.create(MultipartBody.FORM, serverFile))
                        .addFormDataPart("upload", format.format(now))
                        .build();

                Request request = new Request.Builder()
                        .url(BASE_URL + "/myapp/imageupload/")
                        .post(requestBody)
                        .build();

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .retryOnConnectionFailure(true)
                        .build();

                client.newCall(request).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(okhttp3.Call call, IOException e) {
                        Log.d(TAG, "POST: Connection error " + e.toString());
                        final String error_msg = e.toString();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(CameraActivity.this, error_msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                        String TAG = "Camera Activity";
                        final String response_body = response.body().string();
                        if (response.isSuccessful()) {
                            base.progressOFF();
                            Log.d(TAG, "등록 완료");
                            //Log.d(TAG, "onResponse: " + response.body().string());

                            //인식된 text를 tts로 말하기
                            tts.speak(response_body);
                        } else {
                            Log.d(TAG, "Server Response Code : " + response.code());
                            Log.d(TAG, response.toString());
                            //Log.d(TAG, call.request().body().toString());
                            //오류 발생시에 재촬영 요청
                            tts.speak("오류입니다. 다시 한 번 촬영해 주세요.");
                        }
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(CameraActivity.this, response_body, Toast.LENGTH_SHORT).show();
                            }
                        });
                        System.out.println(response_body);
                        response.body().close();
                    }
                });
            }
        });
    }

    //로딩 화면 실행하는 메소드
    private void startProgress() {
        base.progressON(this, "상품 정보 추출중");
        tts.speak("상품 정보 추출 중입니다. 잠시만 기다려 주세요.");
        //boolean result=true;
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                base.progressOFF();
//            }
//        },0);

    }


    //서버로 보내기 전 이미지 회전에 쓰이는 메소드
    private Bitmap fixOrientation(Bitmap bitmap, String filePath) {
        ExifInterface ei = null;
        Bitmap rotatedBitmap;

        try{
            ei = new ExifInterface(filePath);
        }catch(Exception e){
            e.printStackTrace();
            //Log.d("상황: ","서버로 보낼 이미지 회전 정렬 실패");
        }

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);

        switch(orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;
            case ExifInterface.ORIENTATION_NORMAL:
                rotatedBitmap = bitmap;
                break;
            default:
                rotatedBitmap = bitmap;
        }
        return rotatedBitmap;

    }

    //이미지 회전 관련
    public Bitmap rotateImage(Bitmap source, float angle){
        Matrix mat = new Matrix();
        mat.postRotate(angle);
        Bitmap bitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), mat, true);
        return bitmap;
    }

    //  myapp/upload와 함께 잘 작동함
/*
    //  myapp/upload와 함께 잘 작동함
    private void upload(File file) {
        // FILE: device에서 storage access 권한 허용
        final String BASE_URL = "http://18.222.224.247:8000";  // aws
        //final String BASE_URL = "http://10.0.2.2:8000";   // local


        //이미지 회전
        //File을 Bitmap으로 변환
        try{
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
            //서버에 이미지 전송시 회전 정렬하는 메소드 호출
            rotatedBitmap = fixOrientation(bitmap, file.getPath());
        }catch(Exception e){
            e.printStackTrace();
            Log.d("상황: ","file 비트맵 변환 실패");
        }

        //반환된 rotatedBitmap을 다시 파일로 반환
        try{
            //serverFile = new File(Environment.getExternalStorageDirectory()+"/"+UUID.randomUUID().toString()+".jpg");
            serverFile = new File(filepath);
            OutputStream os = new BufferedOutputStream(new FileOutputStream(serverFile));
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.close();
        }catch(Exception e){
            e.printStackTrace();
            Log.d("상황: ", "서버로 보낼 이미지 파일 생성실패");
        }

        Log.d("상황: file.getName()과 file.getPath()는 ",serverFile.getName()+","+serverFile.getPath());




        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                //.addFormDataPart("title", "nice photo")
                .addFormDataPart("file", serverFile.getName(), RequestBody.create(MultipartBody.FORM, serverFile))
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "/myapp/upload/")
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();


        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.d(TAG, "POST: Connection error " + e.toString());
                final String error_msg = e.toString();
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(CameraActivity.this, error_msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                String TAG = "Camera Activity";
                if (response.isSuccessful()) {
                    Log.d(TAG, "등록 완료");
                    //Log.d(TAG, "onResponse: " + response.body().string());
                } else {
                    Log.d(TAG, "Server Response Code : " + response.code());
                    Log.d(TAG, response.toString());
                    //Log.d(TAG, call.request().body().toString());
                }
                final String response_body = response.body().string();
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(CameraActivity.this, response_body, Toast.LENGTH_SHORT).show();
                    }
                });
                System.out.println(response_body);
                response.body().close();
            }
        });
    }
*/

    //카메라 화면이 보이도록 설정하는 메소드
    private void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            //자바의 검증 기능 assert문. 참, 거짓을 검증한다.
            assert texture != null;

            //이미지 크기 설정
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());

            Surface surface = new Surface(texture);

            captureRequestBuilder = cameraDevice.createCaptureRequest(cameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            //카메라 입장에서 사진촬영 위와 동일함.
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (cameraDevice == null) return;
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview(); //화면 업데이트 메소드 호출
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(CameraActivity.this, "Changed", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    //카메라 화면이 업데이트 될 때 실행되는 메소드
    private void updatePreview() {
        if (cameraDevice == null) //카메라를 연결하고 업데이트 메소드를 호출했는데, 만약 카메라 장치가 null값이면 에러 출력
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        //다시 빌더 셋팅
        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    //카메라 장치를 여는 것 설정하는 메소드
    private void openCamera() {
        //카메라 관리하는 매니저 객체
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            //모든 카메라 종류 중에서 가장 기본 카메라인 0번째 카메라 설정
            cameraId = manager.getCameraIdList()[0];
            //0번째 카메라 특성 변수
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];

            //카메라 권한 관련
            //Check realtime permission if run higher API 23
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_CAMERA_PERMISSION);
                return;
            }
            //카메라 열기
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    //Ctrl+o
    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {

        //텍스처뷰 이용 가능할 때 실행되는 메소드 > 카메라 여는 것 설정하는 openCamera 메소드 호출
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    //카메라 권한 결과
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            //권한 허가 안 받았으면
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                //음성 출력
                // tts = new TTSAdapter(this,"카메라 권한을 켜야 상품 인식 메뉴를 이용할 수 있습니다."); //TTS 사용하고자 한다면 2) 클래스 객체 생성
                tts.speak("카메라 권한을 켜야 상품 인식 메뉴를 이용할 수 있습니다.");
                //토스트창 출력
                Toast.makeText(this, "카메라 권한을 켜야 상품 인식 메뉴를 이용할 수 있습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    //다른 액티비티 활성화 되었다가 다시 this 액티비티 활성화 되면 호출되는 메소드
    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        //텍스쳐뷰가 이용가능하면 카메라 여는 것 설정하는 openCamera 메소드 호출
        if (textureView.isAvailable()) openCamera();
        else textureView.setSurfaceTextureListener(textureListener);
    }

    //뒤에서 다 멈추게 설정
    private void stopBackgoundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //뒤에서 실행되게 하는 메소드
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    //TTS 사용하고자 한다면 3) 액티비티가 꺼졌을 시 TTS 음성도 꺼지게 해주기 > 아래 '두' 메소드 복사 붙여넣기.

    //어플이 꺼지거나 중단 된다면 TTS 어댑터의 ttsShutdown() 메소드 호출하기
    protected void onDestroy() {
        super.onDestroy();
//        stopBackgoundThread(); //이건 CameraActivity에서만 쓰는 메소드
//        tts.ttsShutdown();
        tts.stop();
    }

    //액티비티 중지되면 실행되는 메소드
    //다른 액티비티 화면에 가려졌을시 음성 종료
    protected void onPause() {
        super.onPause();
        stopBackgoundThread(); //이건 CameraActivity에서만 쓰는 메소드
//        tts.ttsShutdown();
        tts.stop();
    }

    protected void onStop() {
        super.onStop();
        //상품인식 화면 나가면 카메라 끄기
        cameraDevice.close();
        //stopBackgoundThread(); //이건 CameraActivity에서만 쓰는 메소드
        tts.stop();
    }
}