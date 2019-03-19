package com.example.videorecorddemo;

import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Chronometer;

public class RecordActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final String TAG = "RecordActivity";

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Chronometer chronometer;

    private Camera mCamera;
    private boolean isRecording;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        surfaceView = findViewById(R.id.surface_view);
        chronometer = findViewById(R.id.chronometer);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
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
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

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

    private void stopRecord() {
    }

    private void startRecord() {

    }
}
