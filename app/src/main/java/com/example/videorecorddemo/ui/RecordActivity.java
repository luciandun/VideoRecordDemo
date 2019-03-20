package com.example.videorecorddemo.ui;

import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import com.example.videorecorddemo.R;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 录制视频页面
 * 该页面仅实现了视频录制功能，以后会逐步实现：
 * 1.摄像头切换
 * 2.闪光灯开关
 * 3.手动对焦
 * 4.自定义视频清晰度
 */
public class RecordActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final String TAG = "RecordActivity";

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Chronometer chronometer;
    private Button btnCapture;

    private Camera mCamera;
    private Camera.Parameters parameters;
    private MediaRecorder mRecorder;
    private boolean isRecording;

    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private long recordTime = 0;

    private String videoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        surfaceView = findViewById(R.id.surface_view);
        chronometer = findViewById(R.id.chronometer);
        btnCapture = findViewById(R.id.btn_capture);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
    }

    /**
     * 初始化相机及参数配置，在预览前应该完成该操作
     */
    private void initCamera() {
        if (mCamera == null)
            mCamera = Camera.open(mCameraId);
        if (mCamera == null) {
            showToast("未发现相机");
            return;
        }
        parameters = mCamera.getParameters();

        //设置聚焦模式
        List<String> focusModules = parameters.getSupportedFocusModes();
        log("focusModules = " + focusModules.toString());
        if (focusModules.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        } else if (focusModules.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }

        //设置预览尺寸
        List<Camera.Size> supportedPreviewSize = parameters.getSupportedPreviewSizes();
        log("supportedPreviewSize = " + supportedPreviewSize.toString());
        parameters.setPreviewSize(supportedPreviewSize.get(0).width, supportedPreviewSize.get(0).height);

        parameters.setRecordingHint(true);

        //是否支持影像稳定能力，支持则开启
        if (parameters.isVideoStabilizationSupported()) {
            parameters.setVideoStabilization(true);
            log("支持影像稳定能力");
        }

        mCamera.setDisplayOrientation(90);

        log("Camera.Parameters = " + parameters);
//        mCamera.setParameters(parameters);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
        releaseRecorder();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        releaseCamera();
        releaseRecorder();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                initCamera();
                try {
                    mCamera.setPreviewDisplay(surfaceHolder);
                    mCamera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mCamera == null) return;
        if (surfaceHolder.getSurface() == null) return;
        mCamera.stopPreview();
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (isRecording && mCamera != null) {
            mCamera.lock();
        }
        surfaceView = null;
        surfaceHolder = null;
        releaseRecorder();
        releaseCamera();
    }

    /**
     * 释放相机资源
     */
    private void releaseCamera() {
        try {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
                mCamera.release();
                mCamera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放视频录制资源
     */
    private void releaseRecorder() {
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
    }

    public void recordOrPause(View view) {
        if (isRecording) {
            isRecording = false;
            stopRecord();
        } else {
            startRecord();
            isRecording = true;
        }
    }

    /**
     * 停止录制并重置
     */
    private void stopRecord() {
        if (mRecorder != null) {
            try {
                mRecorder.stop();
                mRecorder.reset();
                btnCapture.setText("录制");
                recordTime = 0;
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.stop();
                isRecording = false;

                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("videoPath", videoPath);
                setResult(RESULT_OK, intent);
                onBackPressed();

            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 配置信息开始录制
     */
    private void startRecord() {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
        }
        try {
            mCamera.unlock();
            mRecorder.setCamera(mCamera);

            mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

//            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

            CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
            mRecorder.setProfile(profile);

            mRecorder.setMaxDuration(60 * 1000);
            mRecorder.setPreviewDisplay(surfaceHolder.getSurface());
            mRecorder.setOrientationHint(270);

            String path = Environment.getExternalStorageDirectory().getPath();
            File file = new File(path + "/AAA/video");
            if (!file.exists()) file.mkdirs();
            videoPath = file + "/" + "capture_" + System.currentTimeMillis() + ".mp4";
            mRecorder.setOutputFile(videoPath);
            log("videoPath = " + videoPath);

            mRecorder.prepare();
            mRecorder.start();

            isRecording = true;
            btnCapture.setText("停止");
            chronometer.setBase(SystemClock.elapsedRealtime() - recordTime);
            chronometer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void showToast(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            int duration = msg.length() >= 8 ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
            Toast.makeText(this, msg, duration).show();
        }
    }

    private void log(String msg) {
        Log.d(TAG, msg);
    }
}
