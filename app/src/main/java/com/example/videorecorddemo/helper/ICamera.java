package com.example.videorecorddemo.helper;

/**
 * description:提取相机常用操作
 * author: dlx
 * date: 2019/03/20
 * version: 1.0
 */
public interface ICamera {

    void initCamera();

    void startRecord();

    void stopRecord();

    void releaseCamera();

    void releaseRecorder();

    void toggleFlash(boolean open);

    void changeCamera(boolean isFront);

    void setVideoPath(String path);

    void setVideoDuration(long duration);

    void destroy();

}
