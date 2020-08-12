package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
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
import android.media.Image;
import android.media.ImageReader;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CameraActivity extends AppCompatActivity {

    //TTS 사용하고자 한다면 1) 클래스 객체 선언
    private TTSAdapter tts;

    //버튼 효과음 관련 변수
    SoundPool soundPool;
    SoundManager soundManager;
    boolean play;
    int playSoundId;

    //버튼, 텍스쳐뷰 레이아웃 변수
    private Button btnCapture;
    private TextureView textureView;

    //이미지 출력 상태 확인
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static{
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
    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback(){

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

        //tts = new TTSAdapter(this,"상품 진열대에서 반 발자국 물러서서 촬영을 진행해 주세요. 화면을 터치하면 촬영이 진행됩니다."); //TTS 사용하고자 한다면 2) 클래스 객체 생성
        tts = new TTSAdapter(this,"화면을 터치하면 촬영이 진행됩니다."); //TTS 사용하고자 한다면 2) 클래스 객체 생성

        //효과음 설정
        //롤리팝 이상 버전일 경우 > 우리는 여기에 해당
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder().build(); //SoundPool 객체 빌더에 빌드하기
        }else{ //롤리팝 이하 버전일 경우
            // new SoundPool(1번,2번,3번) / 1번 - 음악 파일 갯수 / 2번 - 스트림 타입 / 3번 - 음질
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC,0);
        }

        //SoundManager 객체> 효과음을 발생시킬 현재 액티비티와 SoundPool 객체 보내서 생성
        soundManager = new SoundManager(this, soundPool);
        //효과음 등록
        soundManager.addSound(0,R.raw.pianonotification);

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
                    tts.ttsShutdown();
                    soundManager.playSound(0);

//                //효과음 설정
//                //맨 처음 play 변수가 false 라면
//                if(!play){
//                    //효과음 출력해야 되는데 tts가 흘러나오면 안 되니까 tts 중단
//                    tts.ttsShutdown();
//                    //Id 넣어줘서 재생시키기
//                    playSoundId=soundManager.playSound(0);
//                    play = true;
//                }else{
//                    soundManager.resumeSound(0);
//                }

                takePicture(); //사진을 촬영 설정하는 메소드 호출
                retrofitTest();



            }
        });
    }

    private void retrofitTest() {
        Retrofit retrofit = new Retrofit.Builder(). baseUrl("http://jsonplaceholder.typicode.com").addConverterFactory(GsonConverterFactory.create()).build();

        //@GET/@POST 설정해 놓은 인터페이스와 연결
        RetrofitService retrofitService = retrofit.create(RetrofitService.class);

        //userId가 1이라는 데이터의 정보를 얻어온다.
        retrofitService.getData("1").enqueue(new Callback<List<Post>>() {

            //응답 성공했을 때
            @Override
            public void onResponse(@NonNull Call<List<Post>> call, @NonNull Response<List<Post>> response) {
                if(response.isSuccessful()){
                    List<Post> data = response.body();
                    Log.d("상황: ","GET 성공");
                    //userId가 1인 정보들 중에서 첫 번째 title을 출력시켜본다.
                    Log.d("상황: ", data.get(0).getTitle());
                }
            }

            //응답 실패했을 때
            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                Log.d("상황: ","GET 실패");
                t.printStackTrace();
            }
        });

        HashMap<String, Object> input = new HashMap<>();
        input.put("userId", 1);
        input.put("title", "타이틀 POST");
        input.put("body", "바디 POST");
        retrofitService.postData(input).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(@NonNull Call<Post> call, @NonNull Response<Post> response) {
                if (response.isSuccessful()) {
                    Post data = response.body();
                    if (data != null) {
                        Log.d("상황: ", data.getUserId() + "");
                        Log.d("상황: ", data.getId() + "");
                        Log.d("상황: ", data.getTitle()+"");
                        Log.d("상황: ", data.getBody()+"");
                        Log.e("상황: ", "======================================");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Post> call, @NonNull Throwable t) {
                        Log.d("상황: ", "POST 실패");
            }
        });

    }



    //사진 촬영 후 상품 정보를 불러오는 메소드
    private void getProductInfo() {
    }

    //사진 찍는 메소드
    private void takePicture() {
        //장치가 비어있으면 사진을 찍을 수 없으므로 return
        if(cameraDevice == null) return;
        //장치 잘 있으면 카메라 서비스 연결
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try{
            //많은 카메라 중 현재 연결된 camera의 특징을 받아온다.
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            //일단 사진 크기는 null값
            Size[] jpegSizes = null;
            //특징 값이 있다면
            if(characteristics != null){
                //카메라 특징에 맞게 사진 크기 설정
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }

            //캡처 이미지 사이즈 설정
            int width = textureView.getWidth();
            int height = textureView.getHeight();
            //int width = 640;
            //int height = 480;

            if(jpegSizes != null && jpegSizes.length>0){
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }

            //이미지 읽어들이기
            ImageReader reader = imagerReader.newInstance(width, height, ImageFormat.JPEG,1);
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
            file = new File(Environment.getExternalStorageDirectory()+"/"+UUID.randomUUID().toString()+".jpg");
            //이미지 읽어들이는 리스너
            ImageReader.OnImageAvailableListener readerListender = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try{
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes() [0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    }catch(FileNotFoundException e){
                        e.printStackTrace();
                    }catch(IOException e){
                        e.printStackTrace();
                    }finally{
                        if(image != null) image.close();
                    }
                }
                //이미지 파일 저장 메소드
                private void save(byte[] bytes) throws IOException {
                    OutputStream outputStream = null;
                    try{
                        outputStream = new FileOutputStream(file);
                        outputStream.write(bytes);
                    }finally{
                        if(outputStream != null)
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
                    Toast.makeText(CameraActivity.this, "Saved "+file, Toast.LENGTH_SHORT).show();
                    //다시 촬영할 수 있도록 카메라 화면 보여주는 메소드 호출 > 하지만 우리는 이 부분을 수정해야겠지..
                    //1) 사진을 촬영하면 음성이 출력되기까지 사진 촬영을 못 하게 해야함.
                    createCameraPreview();
                }
            };

            //카메라 입장에서 사진 촬영
            cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback(){

                //설정
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try{
                        cameraCaptureSession.capture(captureBuilder.build(), captureListener,mBackgroundHandler);
                    }catch (CameraAccessException e){
                        e.printStackTrace();
                    }
                }

                //설정 실패
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            },mBackgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    //카메라 화면이 보이도록 설정하는 메소드
    private void createCameraPreview() {
        try{
            SurfaceTexture texture = textureView.getSurfaceTexture();
            //자바의 검증 기능 assert문. 참, 거짓을 검증한다.
            assert texture != null;
            //이미지 크기 설정
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(cameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            //카메라 입장에서 사진촬영 위와 동일함.
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if(cameraDevice==null) return;
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
        if(cameraDevice == null) //카메라를 연결하고 업데이트 메소드를 호출했는데, 만약 카메라 장치가 null값이면 에러 출력
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        //다시 빌더 셋팅
        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        try{
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        }catch(CameraAccessException e){
            e.printStackTrace();
        }
    }


    //카메라 장치를 여는 것 설정하는 메소드
    private void openCamera() {
        //카메라 관리하는 매니저 객체
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try{
            //모든 카메라 종류 중에서 가장 기본 카메라인 0번째 카메라 설정
            cameraId = manager.getCameraIdList()[0];
            //0번째 카메라 특성 변수
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];

            //카메라 권한 관련
            //Check realtime permission if run higher API 23
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[] {
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },REQUEST_CAMERA_PERMISSION);
                return;
            }
            //카메라 열기
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    //Ctrl+o
    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener(){

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
        if(requestCode == REQUEST_CAMERA_PERMISSION){
            //권한 허가 안 받았으면
            if(grantResults [0] == PackageManager.PERMISSION_DENIED){
                //음성 출력
                tts = new TTSAdapter(this,"카메라 권한을 켜야 상품 인식 메뉴를 이용할 수 있습니다."); //TTS 사용하고자 한다면 2) 클래스 객체 생성
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
        if(textureView.isAvailable()) openCamera();
        else textureView.setSurfaceTextureListener(textureListener);
    }

    //뒤에서 다 멈추게 설정
    private void stopBackgoundThread() {
        mBackgroundThread.quitSafely();
        try{
            mBackgroundThread.join();
            mBackgroundThread=null;
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
        tts.ttsShutdown();
    }

    //액티비티 중지되면 실행되는 메소드
    //다른 액티비티 화면에 가려졌을시 음성 종료
    protected void onPause() {
        super.onPause();
        stopBackgoundThread(); //이건 CameraActivity에서만 쓰는 메소드
        tts.ttsShutdown();
    }

    protected void onStop() {
        super.onStop();
        tts.ttsShutdown();
    }

}
