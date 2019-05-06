package com.example.videorecorddemo.helper;

/**
 * description:提取相机常用操作
 * author: dlx
 * date: 2019/03/20
 * version: 1.0
 */
public interface ICamera {

    void openCamera();

    void startPreview();

    void stopPreview();

    void releaseCamera();

    void startRecord();

    void stopRecord();

    void releaseRecorder();

    void toggleFlash();

    void changeCamera();

    void setVideoPath(String path);

    void setVideoDuration(int duration);

    void destroy();

}
