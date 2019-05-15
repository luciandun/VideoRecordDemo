package com.example.videorecorddemo.helper;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.File;
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

    private Activity mActivity;
    /**
     * 默认相机ID取后置相机
     */
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    private Camera mCamera;
    private MediaRecorder mRecorder;

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    private boolean isFrontCamera = true;
    private boolean isFlashOn;
    private boolean isRecording;

    private String videoPath;
    private int videoDuration = 5 * 60 * 1000; //默认5分钟


    private long lastRecordTimestamp;

    public boolean isRecording() {
        return isRecording;
    }

    public String getVideoPath() {
        return videoPath;
    }

    private CameraHelper(Activity activity, SurfaceView surfaceView) {
        this.mActivity = activity;
        this.surfaceView = surfaceView;
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
    }

    public static CameraHelper get(Activity activity, SurfaceView surfaceView) {
        return new CameraHelper(activity, surfaceView);
    }

    @Override
    public void openCamera() {
        if (mCamera == null) {
            if (!mActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                showToast("未发现相机");
                return;
            }
            int cameras = Camera.getNumberOfCameras();
            log("cameraCount:" + cameras);
            mCameraId = cameras >= 2 && isFrontCamera ?
                    Camera.CameraInfo.CAMERA_FACING_FRONT :
                    Camera.CameraInfo.CAMERA_FACING_BACK;
            try {
                mCamera = Camera.open(mCameraId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mCamera == null) {
            showToast("相机功能不可用");
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

    @Override
    public void startPreview() {
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(surfaceHolder);
                mCamera.startPreview();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopPreview() {
        try {
            if (mCamera != null)
                mCamera.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void startRecord() {
        if (mRecorder == null)
            mRecorder = new MediaRecorder();
        try {
            if (mCamera != null) {
                mCamera.unlock();
                mRecorder.setCamera(mCamera);
                mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
                mRecorder.setProfile(profile);

                mRecorder.setMaxDuration(videoDuration);
                mRecorder.setPreviewDisplay(surfaceHolder.getSurface());
                mRecorder.setOrientationHint(getVideoOrientation());

                File filePath = new File(videoPath);
                if (!filePath.exists()) {
                    boolean success = filePath.mkdirs();
                    if (!success) {
                        showToast("视频路径创建失败");
                        return;
                    }
                }
                videoPath = filePath.getAbsolutePath() + File.separator + "video_" + System.currentTimeMillis() + ".mp4";
                mRecorder.setOutputFile(videoPath);
                log("videoPath = " + videoPath);

                mRecorder.prepare();
                mRecorder.start();
                isRecording = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopRecord() {
        if (mRecorder != null) {
            try {
                mRecorder.stop();
                mRecorder.reset();
                isRecording = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 供外部调用
     */
    public void startOrStopRecord() {
        //频繁点击会导致Camera crash
        long now = System.currentTimeMillis();
        if (now - lastRecordTimestamp < 2000)
            return;
        lastRecordTimestamp = now;

        if (isRecording) {
            isRecording = false;
            stopRecord();
        } else {
            isRecording = true;
            startRecord();
        }
    }


    @Override
    public void releaseRecorder() {
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
    }

    @Override
    public void toggleFlash() {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            isFlashOn = !isFlashOn;
            parameters.setFlashMode(isFlashOn ?
                    Camera.Parameters.FLASH_MODE_ON :
                    Camera.Parameters.FLASH_MODE_OFF);

            mCamera.setParameters(parameters);
        }
    }

    @Override
    public void changeCamera() {
        int cameras = Camera.getNumberOfCameras();
        if (cameras < 2) {
            showToast("该设备只有一个摄像头");
            return;
        }
        isFrontCamera = !isFrontCamera;
        new Thread(new Runnable() {
            @Override
            public void run() {
                releaseCamera();
                openCamera();
                startPreview();
            }
        }).start();
    }

    @Override
    public void setVideoPath(String path) {
        this.videoPath = path;
    }

    @Override
    public void setVideoDuration(int duration) {
        this.videoDuration = duration;
    }

    @Override
    public void destroy() {
        releaseCamera();
        releaseRecorder();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                openCamera();
                startPreview();
            }
        }).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (surfaceView.getHolder() == null) return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                stopPreview();
                startPreview();
            }
        }).start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseRecorder();
        releaseCamera();
    }

    /**
     * 根据当前摄像头调整预览图像方向
     */
    private void setPreviewOrientation() {
        if (mCamera != null) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraId, info);
            int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
            //获取摄像头角度
            int degree = 0;
            switch (rotation) {
                case Surface.ROTATION_0:
                    degree = 0;
                    break;
                case Surface.ROTATION_90:
                    degree = 90;
                    break;
                case Surface.ROTATION_180:
                    degree = 180;
                    break;
                case Surface.ROTATION_270:
                    degree = 270;
                    break;
            }
            int result;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                //前置摄像头
                result = (info.orientation + degree) % 360;
                result = (360 - result) % 360; //修正镜像
            } else {
                //后置摄像头
                result = (info.orientation - degree + 360) % 360;
            }
            mCamera.setDisplayOrientation(result);
        }
    }

    /**
     * 获取视频的旋转角度
     */
    private int getVideoOrientation() {
        if (isFrontCamera) return 270;
        else return 90;
    }

    private void showToast(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            int duration = msg.length() > 6 ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
            Toast.makeText(mActivity.getApplicationContext(), msg, duration).show();
        }
    }

    private void log(String msg) {
        Log.d(TAG, msg);
    }
}
