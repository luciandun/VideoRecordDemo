package com.example.videorecorddemo.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.TextView;

import com.example.videorecorddemo.R;
import com.example.videorecorddemo.helper.CameraHelper;

/**
 * description:抽离录制页面
 * author: dlx
 * date: 2019/05/15
 * version: 1.0
 */
public class RecordFragment extends Fragment implements View.OnClickListener {

    private TextView btnSwitchCamera;
    private TextView btnStart;
    private Chronometer chronometer;

    private CameraHelper mCameraHelper;

    private RecordingListener mListener;

    public void setRecordingListener(RecordingListener listener) {
        mListener = listener;
    }

    public static RecordFragment newInstance(String videoPath) {
        RecordFragment fragment = new RecordFragment();
        Bundle args = new Bundle();
        args.putString("videoPath", videoPath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RecordingListener) {
            mListener = (RecordingListener) context;
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.record_fragment, container, false);
        initView(contentView);
        return contentView;
    }

    private void initView(View contentView) {
        SurfaceView surfaceView = contentView.findViewById(R.id.surface_view);
        btnSwitchCamera = contentView.findViewById(R.id.switch_camera);
        chronometer = contentView.findViewById(R.id.chronometer);
        btnStart = contentView.findViewById(R.id.btn_start);

        mCameraHelper = CameraHelper.get(getActivity(), surfaceView);
        btnSwitchCamera.setOnClickListener(this);
        btnStart.setOnClickListener(this);

        if (getArguments() != null) {
            String videoPath = getArguments().getString("videoPath");
            mCameraHelper.setVideoPath(videoPath);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.switch_camera) {
            mCameraHelper.changeCamera();
        } else if (v.getId() == R.id.btn_start) {
            if (!mCameraHelper.isRecording()) {
                mCameraHelper.startOrStopRecord();
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();
                btnStart.setText("停止");
                btnSwitchCamera.setVisibility(View.GONE);
            } else {
                mCameraHelper.startOrStopRecord();
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.stop();
                btnStart.setText("开始");
                btnSwitchCamera.setVisibility(View.VISIBLE);
                if (mListener != null) {
                    mListener.onRecordFinish(mCameraHelper.getVideoPath());
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mCameraHelper.destroy();
    }

    public interface RecordingListener {
        void onRecordFinish(String filePath);
    }
}
