package com.example.videorecorddemo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import com.example.videorecorddemo.R;

/**
 * 录制视频页面
 * 该页面仅实现了视频录制功能，以后会逐步实现：
 * 1.摄像头切换
 * 2.闪光灯开关
 * 3.手动对焦
 * 4.自定义视频清晰度
 */
public class RecordActivity extends AppCompatActivity implements RecordFragment.RecordingListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, RecordFragment.newInstance(Environment.getExternalStorageDirectory().getPath() + "/AAA/video"))
                .commit();
    }

    @Override
    public void onRecordFinish(String filePath) {
        Intent intent = new Intent();
        intent.putExtra("videoPath", filePath);
        setResult(RESULT_OK, intent);
        onBackPressed();
    }
}
