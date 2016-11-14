package com.nan.recordbutton.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.nan.recordbutton.R;
import com.nan.recordbutton.utils.AudioRecordManager;
import com.nan.recordbutton.utils.VibratorUtils;

import java.io.File;


/**
 * Created by 焕楠 2016-6-27
 */
public class RecorderButton extends Button implements AudioRecordManager.AudioStateListener {

    private static final String TAG = "AudioRecorderButton";

    private static final int DISTANCE_CANCEL = 50;
    private static final int STATE_NORMAL = 1;
    private static final int STATE_RECORDING = 2;
    private static final int STATE_WANT_TO_CANCEL = 3;

    private int mCurState = STATE_NORMAL;
    private boolean isRecording = false; // 已经开始录音

    private AudioRecordManager mAudioRecordManager;

    private float mTime;
    // 是否触发longclick
    private boolean mReady;
    private String str_recorder_normal;
    private String str_recorder_recording;
    private String str_recorder_want_cancel;
    private int bg_recorder_normal;
    private int bg_recorder_recording;
    private int bg_recorder_cancel;
    private float max_record_time;
    private float min_record_time;
    private int max_voice_level;
    private MediaPlayer mMediaPlayer;
    private Context mCtx;

    public RecorderButton(Context context) {
        this(context, null, 0);
    }

    public RecorderButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecorderButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.RecorderButton);

        mCtx = context;

        str_recorder_normal = a.getString(R.styleable.RecorderButton_txt_normal);
        str_recorder_recording = a.getString(R.styleable.RecorderButton_txt_recording);
        str_recorder_want_cancel = a.getString(R.styleable.RecorderButton_txt_want_cancel);

        bg_recorder_normal = a.getResourceId(R.styleable.RecorderButton_bg_normal, 0);
        bg_recorder_recording = a.getResourceId(R.styleable.RecorderButton_bg_recording, 0);
        bg_recorder_cancel = a.getResourceId(R.styleable.RecorderButton_bg_want_cancel, 0);

        //最大录音时间，默认为15秒
        //最小录音时间，默认为10秒
        max_record_time = a.getFloat(R.styleable.RecorderButton_max_record_time, 15);
        min_record_time = a.getFloat(R.styleable.RecorderButton_min_record_time, 10);

        max_voice_level = a.getInt(R.styleable.RecorderButton_max_voice_level, 5);

        a.recycle();

        String dir = Environment.getExternalStorageDirectory() + File.separator + "sxbb" + File.separator + "record";
        mAudioRecordManager = AudioRecordManager.getInstance(dir);
        mAudioRecordManager.setOnAudioStateListner(this);

        setText(str_recorder_normal);
        setBackgroundResource(bg_recorder_normal);

//        setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        mReady = true;
//                        Log.e(TAG, "OnLongClick");
//
//                        //播放提示音
//                        MediaPlayer.create(mCtx, R.raw.fx).start();
//                        mAudioRecordManager.prepareAudio();
//                    }
//                }, 1000);
//            }
//        });

        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                mReady = true;
                Log.e(TAG, "OnLongClick");

                VibratorUtils.vibrate(mCtx, 60);

                //播放提示音
                MediaPlayer.create(mCtx, R.raw.fx).start();
                mAudioRecordManager.prepareAudio();

                return false;
            }
        });

//        setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (mListener != null) {
//                    mListener.onCancel(true);
//                }
//            }
//        });
    }

    /**
     * 录音完成后的回调
     */
    public interface AudioStateRecorderListener {
        void onFinish(float seconds, String filePath);

        void onCancel(boolean isTooShort);

        void onVoiceChange(int voiceLevel);

        void onStart(float time);

        void onUpdateTime(float currentTime, float minTime, float maxTime);

        void onReturnToRecord();

        void onWantToCancel();
    }

    private AudioStateRecorderListener mListener;

    public void setAudioStateRecorderListener(AudioStateRecorderListener listener) {
        mListener = listener;
    }

    /**
     * 获取音量大小的Runnable
     */
    private Runnable mGetVoiceLevelRunnable = new Runnable() {

        @Override
        public void run() {
            while (isRecording) {
                try {
                    Thread.sleep(100);
                    mTime += 0.1f;
                    mHandler.sendEmptyMessage(MSG_UPDATE_TIME);
                    mHandler.sendEmptyMessage(MSG_VOICE_CHANGE);
                    if (mTime >= max_record_time) {
                        //如果时间到了最大的录音时间
                        mAudioRecordManager.release();
                        mHandler.sendEmptyMessage(MSG_TIME_LIMIT);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private static final int MSG_AUDIO_PREPARED = 0x110;
    private static final int MSG_VOICE_CHANGE = 0x111;
    private static final int MSG_UPDATE_TIME = 0x113;
    //最大的录音时间到了
    private static final int MSG_TIME_LIMIT = 0x114;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AUDIO_PREPARED:

                    // audio end prepared以后开始录音
                    isRecording = true;

                    if (mListener != null) {
                        mListener.onStart(mTime);
                    }

                    // 开启线程，监听音量变化
                    new Thread(mGetVoiceLevelRunnable).start();
                    break;
                case MSG_VOICE_CHANGE:
                    if (mListener != null) {
                        //根据用户设置的最大值去获取音量级别
                        mListener.onVoiceChange(mAudioRecordManager.getVoiceLevel(max_voice_level));
                    }
                    break;
                case MSG_UPDATE_TIME:
                    if (mListener != null) {
                        mListener.onUpdateTime(mTime, min_record_time, max_record_time);
                    }
                    break;
                case MSG_TIME_LIMIT:
                    if (mListener != null) {
                        //到达时间限制了
                        MediaPlayer.create(mCtx, R.raw.gj).start();
                        mListener.onFinish(mTime, mAudioRecordManager.getCurrentFilePath());
                    }
                    changeState(STATE_NORMAL);
                    reset();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void wellPrepared() {
        Log.d("LONG", "wellPrepared");
        mHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (mReady) {
                    changeState(STATE_RECORDING);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                // 根据x, y的坐标，判断是否想要取消
                if (mReady) {
                    if (wantToCancel(x, y)) {
                        changeState(STATE_WANT_TO_CANCEL);
                        if (mListener != null) {
                            mListener.onWantToCancel();
                        }
                    } else {
                        changeState(STATE_RECORDING);
                        if (mListener != null) {
                            mListener.onReturnToRecord();
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                /**
                 * 1. 未触发onLongClick
                 * 2. prepared没有完毕已经up
                 * 3. 录音时间小于预定的值，这个值我们设置为在onLongClick之前
                 */
                if (!mReady) { // 未触发onLongClick
                    changeState(STATE_NORMAL);
                    reset();
                    return super.onTouchEvent(event);
                }

                if (!isRecording || mTime < min_record_time) {  // prepared没有完毕 或 录音时间过短
                    isRecording = false;
                    mAudioRecordManager.cancel();
                    // 用户录音时间太短，取消
                    if (mListener != null) {
                        MediaPlayer.create(mCtx, R.raw.fy).start();
                        mListener.onCancel(true);
                    }
                } else if (STATE_RECORDING == mCurState) { // 正常录制结束
                    mAudioRecordManager.release();
                    if (mListener != null) {
                        MediaPlayer.create(mCtx, R.raw.gj).start();
                        mListener.onFinish(mTime, mAudioRecordManager.getCurrentFilePath());
                    }
                } else if (STATE_WANT_TO_CANCEL == mCurState) {
                    mAudioRecordManager.cancel();
                    if (mListener != null) {
                        MediaPlayer.create(mCtx, R.raw.fy).start();
                        mListener.onCancel(false);
                    }
                }
                changeState(STATE_NORMAL);
                reset();
                break;
        }

        return super.onTouchEvent(event);
    }

    /**
     * 恢复状态及标志位
     */
    private void reset() {
        isRecording = false;
        mReady = false;
        mTime = 0;
        mCurState = STATE_NORMAL;
    }

    /**
     * 根据坐标去判断是否应该取消
     *
     * @param x
     * @param y
     * @return
     */
    private boolean wantToCancel(int x, int y) {
        if (x < 0 || x > getWidth()) {
            return true;
        }
        if (y < -DISTANCE_CANCEL || y > getHeight() + DISTANCE_CANCEL) {
            return true;
        }
        return false;
    }

    /**
     * 按钮状态改变
     *
     * @param state
     */
    private void changeState(int state) {
        if (mCurState != state) {
            mCurState = state;
            switch (mCurState) {
                case STATE_NORMAL:
                    setBackgroundResource(bg_recorder_normal);
                    setText(str_recorder_normal);
                    break;
                case STATE_RECORDING:
                    setBackgroundResource(bg_recorder_recording);
                    setText(str_recorder_recording);
                    break;
                case STATE_WANT_TO_CANCEL:
                    setBackgroundResource(bg_recorder_cancel);
                    setText(str_recorder_want_cancel);
                    break;
            }
        }
    }


}
