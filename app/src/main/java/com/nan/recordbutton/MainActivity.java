package com.nan.recordbutton;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nan.recordbutton.widget.RecorderButton;
import com.nan.recordbutton.widget.HorVoiceView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private RecorderButton btn_record;
    private TextView tv_state;
    private TextView tv_time;
    private HorVoiceView hv_voice;
    private String str_record1;
    private String str_record2;

    private static final int MAX_RECORD_TIME = 15;

    private ImageView mWave1;
    private ImageView mWave2;
    private ImageView mWave3;
    private AnimationSet mAnimationSet1;
    private AnimationSet mAnimationSet2;
    private AnimationSet mAnimationSet3;
    private static final int OFFSET = 600;  //每个动画的播放时间间隔
    private static final int MSG_WAVE2_ANIMATION = 2;
    private static final int MSG_WAVE3_ANIMATION = 3;
    //  这个标志用于防止
    private boolean isShowingWave = false;

    private boolean isRecordEnable = false;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WAVE2_ANIMATION:
                    if (isShowingWave) {
                        mWave2.startAnimation(mAnimationSet2);
                    } else {
                        mWave2.clearAnimation();
                    }
                    break;
                case MSG_WAVE3_ANIMATION:
                    if (isShowingWave) {
                        mWave3.startAnimation(mAnimationSet3);
                    } else {
                        mWave3.clearAnimation();
                    }
                    break;
            }
        }
    };
    private ImageView iv_bg;
    private Context mCtx;
    private MediaRecorder mMediaRecorder;
    private TextView tv_txt0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findView();
        init();

    }

    public static final int CODE_REQUEST_RECORD = 100;

    /**
     * android6.0权限管理
     */
    private void checkRecordPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        CODE_REQUEST_RECORD);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == CODE_REQUEST_RECORD) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(MainActivity.this, "成功授权录音权限", Toast.LENGTH_SHORT).show();

            } else {
                // Permission Denied
                Toast.makeText(MainActivity.this, "没有录音权限", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mMediaRecorder != null) {
            mMediaRecorder.release();
        }
    }

    private void findView() {
        iv_bg = (ImageView) findViewById(R.id.iv_bg);
        mWave1 = (ImageView) findViewById(R.id.wave1);
        mWave2 = (ImageView) findViewById(R.id.wave2);
        mWave3 = (ImageView) findViewById(R.id.wave3);
        hv_voice = (HorVoiceView) findViewById(R.id.hv_voice);
        btn_record = (RecorderButton) findViewById(R.id.btn_record);
        tv_state = (TextView) findViewById(R.id.tv_state);
        tv_time = (TextView) findViewById(R.id.tv_time);
        tv_txt0 = (TextView) findViewById(R.id.tv_txt0);
    }

    private void init() {

        mCtx = this;

        checkRecordPermission();
//        TintBarUtils.setStatusBarTextStyle(this, true);
//        immerseLayout();

        mAnimationSet1 = initAnimationSet();
        mAnimationSet2 = initAnimationSet();
        mAnimationSet3 = initAnimationSet();

        str_record1 = getResources().getString(R.string.record_txt4);
        str_record2 = getResources().getString(R.string.record_txt5);
        updateTime(MAX_RECORD_TIME);

        btn_record.setAudioStateRecorderListener(new MyRecordListener());
    }

    /**
     * 上传录音
     * 需要修改URL
     *
     * @param seconds    语音的时间长度
     * @param recordFile 录音文件
     */
    private void uploadRecord(final int seconds, final File recordFile) {

        String ts = System.currentTimeMillis() + "";

        //......
    }


    /**
     * 更新时间
     *
     * @param time 更新显示的时间
     */
    private void updateTime(int time) {
        tv_time.setText(str_record1 + time + str_record2);
    }

    /**
     * 文案的还原
     */
    private void resetTextAndTime() {

        tv_state.setText(R.string.record_normal);
        updateTime(MAX_RECORD_TIME);

    }

    private AnimationSet initAnimationSet() {
        AnimationSet as = new AnimationSet(true);
        ScaleAnimation sa = new ScaleAnimation(1f, 3.5f, 1f, 3.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        sa.setDuration(OFFSET * 3);
        sa.setRepeatCount(Animation.INFINITE);// 设置循环
        AlphaAnimation aa = new AlphaAnimation(1, 0.1f);
        aa.setDuration(OFFSET * 3);
        aa.setRepeatCount(Animation.INFINITE);//设置循环
        as.addAnimation(sa);
        as.addAnimation(aa);
        return as;
    }

    private void startWaveAnimation() {
        mWave1.startAnimation(mAnimationSet1);
        mHandler.sendEmptyMessageDelayed(MSG_WAVE2_ANIMATION, OFFSET);
        mHandler.sendEmptyMessageDelayed(MSG_WAVE3_ANIMATION, OFFSET * 2);
        isShowingWave = true;
    }

    private void stopWaveAnimation() {
        mWave1.clearAnimation();
        mWave2.clearAnimation();
        mWave3.clearAnimation();
        isShowingWave = false;
    }

    class MyRecordListener implements RecorderButton.AudioStateRecorderListener {

        @Override
        public void onStart(float time) {

            tv_txt0.setVisibility(View.GONE);
            startWaveAnimation();
            tv_time.setVisibility(View.VISIBLE);
            tv_state.setText(R.string.record_ing);
//                Toast.makeText(AudioRecordActivity.this, "开始录制", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onUpdateTime(float currentTime, float minTime, float maxTime) {

            //保留一位小数
            int max = (int) maxTime;
            int time = (int) currentTime;
            hv_voice.setText(" " + (max - time) + " ");
            updateTime(max - time);

            if (time >= 10) {
                tv_txt0.setText(R.string.record_enougth);
                tv_txt0.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onReturnToRecord() {
            tv_state.setText(R.string.record_ing);
        }

        @Override
        public void onWantToCancel() {
            tv_state.setText(R.string.record_want_to_cancel);
        }

        @Override
        public void onFinish(float seconds, String filePath) {

            btn_record.setEnabled(false);
            stopWaveAnimation();
            tv_state.setText(R.string.record_success);
            tv_time.setVisibility(View.GONE);

            uploadRecord((int) seconds, new File(filePath));
//                Toast.makeText(AudioRecordActivity.this, (int) seconds + "\n" + filePath, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel(boolean isTooShort) {
            stopWaveAnimation();
            tv_state.setText(R.string.record_normal);
            if (isTooShort) {
                tv_time.setText(R.string.record_too_short);
            }

        }

        @Override
        public void onVoiceChange(int voiceLevel) {
            hv_voice.setVoice(voiceLevel);
        }
    }
}
