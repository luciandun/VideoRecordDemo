package com.example.videorecorddemo.ui;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import com.example.videorecorddemo.R;
import com.example.videorecorddemo.helper.CameraHelper;

/**
 * 录制视频页面
 * 该页面仅实现了视频录制功能，以后会逐步实现：
 * 1.摄像头切换
 * 2.闪光灯开关
 * 3.手动对焦
 * 4.自定义视频清晰度
 */
public class Record2Activity extends AppCompatActivity {

    private static final String TAG = "RecordActivity";

    private CameraHelper mCameraHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        Chronometer chronometer = findViewById(R.id.chronometer);
        Button btnCapture = findViewById(R.id.btn_capture);
        TextView flashView = findViewById(R.id.text_flash);
        SurfaceView surfaceView = findViewById(R.id.surface_view);

        mCameraHelper = CameraHelper.get(this, surfaceView);
        mCameraHelper.setChronometer(chronometer);
        mCameraHelper.setRecordView(btnCapture);
        mCameraHelper.setFlashView(flashView);
        mCameraHelper.setVideoPath(Environment.getExternalStorageDirectory().getPath()+ "/AAA/video");

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraHelper.startOrStopRecord();
            }
        });

        findViewById(R.id.btn_switch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraHelper.changeCamera();
            }
        });

        flashView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraHelper.toggleFlash();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraHelper.destroy();
    }
}
