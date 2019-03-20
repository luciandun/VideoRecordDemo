package com.example.videorecorddemo.helper;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Chronometer;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

/**
 * description:用来管理相机录像、拍照的操作，减少Activity代码
 * author: dlx
 * date: 2019/03/20
 * version: 1.0
 */
public class CameraHelper implements ICamera, SurfaceHolder.Callback {

    private static final String TAG = "CameraHelper";
    private Context mContext;

    private Camera mCamera;
    private MediaRecorder mRecorder;

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Chronometer chronometer;

    private boolean isFrontCamera = true;
    private String videoPath;
    private long videoDuration = 60 * 1000; //默认1分钟

    private CameraHelper(Context context, SurfaceView surfaceView) {
        this.mContext = context;
        this.surfaceView = surfaceView;
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
    }

    public CameraHelper get(Context context, SurfaceView surfaceView) {
        return new CameraHelper(context, surfaceView);
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
        if (surfaceView.getHolder() == null) return;
        if (mCamera == null) return;
        try {
            mCamera.stopPreview();
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseRecorder();
        releaseCamera();
    }

    @Override
    public void initCamera() {
        if (mCamera == null) {
            if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                showToast("未发现相机");
                return;
            }
            openCamera();
        }

        if (mCamera == null) {
            showToast("未发现相机");
            return;
        }

        Camera.Parameters parameters = mCamera.getParameters();
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
        log("supportedPreviewSize = (" + supportedPreviewSize.get(0).width + "," + supportedPreviewSize.get(0).height + ")");
        parameters.setPreviewSize(supportedPreviewSize.get(0).width, supportedPreviewSize.get(0).height);

        //开启快速成像
        parameters.setRecordingHint(true);

        //是否支持影像稳定能力，支持则开启
        if (parameters.isVideoStabilizationSupported()) {
            parameters.setVideoStabilization(true);
            log("支持影像稳定能力");
        }

        setPreviewOrientation();
    }

    /**
     * 设置画面预览的旋转角度
     */
    private void setPreviewOrientation() {
        mCamera.setDisplayOrientation(90);
    }

    private void openCamera() {
        int cameraCount = Camera.getNumberOfCameras();
        log("cameraCount:" + cameraCount);
        //2个以上摄像头，则可前后切换，否则只有后摄
        int cameraId = cameraCount >= 2 && isFrontCamera ?
                Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;
        mCamera = Camera.open(cameraId);
    }

    @Override
    public void startRecord() {

    }

    @Override
    public void stopRecord() {

    }

    @Override
    public void releaseCamera() {

    }

    @Override
    public void releaseRecorder() {

    }

    @Override
    public void toggleFlash(boolean open) {

    }

    @Override
    public void changeCamera(boolean isFront) {
        isFrontCamera = isFront;
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            openCamera();
            setPreviewOrientation();
            try {
                mCamera.setPreviewDisplay(surfaceHolder);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setVideoPath(String path) {
        videoPath = path;
    }

    @Override
    public void setVideoDuration(long duration) {
        videoDuration = duration;
    }

    @Override
    public void destroy() {
        releaseRecorder();
        releaseCamera();
        mCamera = null;
    }

    private void showToast(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            int duration = msg.length() >= 8 ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
            Toast.makeText(mContext, msg, duration).show();
        }
    }

    private void log(String msg) {
        Log.d(TAG, msg);
    }
}
