package com.example.project;

// 참조한 사이트
// https://m.blog.naver.com/PostView.nhn?blogId=khs7515&logNo=20154846745&proxyReferer=https:%2F%2Fwww.google.com%2F

import java.util.HashMap;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

/*
 * 사운드 메니져 클래스를 만들어 놓고 필요할 때 편하게 쓰기 위해
 * 사운드풀이 쓰기가 불편해서 만들어두고 쓰는 방식을 사용한다.
 *
 * 언제든지 소리를 낼 수 있는 로딩이 사전에 필요하다.
 */

public class SoundManager {
    //필요한 필드 정의하기
    private static SoundManager sManager;
    private SoundPool soundPool;
    private HashMap<Integer, Integer> map;
    private Context context;

    //싱글톤 패턴
    private SoundManager(){}
    public static SoundManager getInstance(){
        if(sManager==null){
            sManager = new SoundManager();
        }
        return sManager;
    }

    //초기화하기
    public void init(Context context){
        this.context=context;
        //필요한 객체를 전달해서 soundpool 객체를 생성한다
        soundPool=new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        //사운드 리소스 id 값을 저장할 hashMap 객체 생성하기
        map = new HashMap<Integer, Integer>();
    }

    //음원을 추가하는 메소드(resourceID=R.raw.음원)
    public void addSound(int index, int resId){
        //인자로 전달된 resId 값을 이용해서 사운드를 로딩시키고 재생할 준비를 한다.
        int id = soundPool.load(context, resId, 1);
        //등록하고 리턴되는 아이디를 맵에 저장한다.
        map.put(index, id);
    }

    //음원을 재생하는 메소드
    public void play(int index){
        //인자로 전달된 인덱스 값을 이용해서 해당 음원을 재생하도록 한다.
        soundPool.play(map.get(index), 1, 1, 1, 0, 1);
    }

    //음원을 중지하는 메소드
    public void stopSound(int index){
        //인자로 전달된 인덱스 값을 이용해서 해당 음원을 정지시킨다.
        soundPool.stop(map.get(index));
    }
}






/*
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import java.util.HashMap;

//효과음을 나타내는 클래스 SoundManager
public class SoundManager {
    private SoundPool mSoundPool;
    private HashMap<Integer, Integer> mSoundPoolMap;
    private AudioManager mAudioManager;
    private Context mContext;

    //생성자 어떤 액티비티와 SoundPool 객체
    public SoundManager(Context mContext, SoundPool mSoundPool){
        this.mContext = mContext;
        this.mSoundPool = mSoundPool;
        mSoundPoolMap = new HashMap<Integer, Integer>();
        mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    //효과음 추가
    public void addSound(int index, int soundId){
        mSoundPoolMap.put(index, mSoundPool.load(mContext, soundId, 1));
    }

    //효과음 재생
    public int playSound(int index) {
        int streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        return mSoundPool.play(mSoundPoolMap.get(index),streamVolume,streamVolume,1,0,1f);
    }

    //효과음 정지
    public void stopSound(int streamId){
        mSoundPool.stop(streamId);
    }

    //효과음 일시정지
    public void pauseSound(int streamId){
        mSoundPool.pause(streamId);
    }

    //효과음 재시작
    public void resumeSound(int streamId){
        mSoundPool.resume(streamId);
    }
}
*/
