package com.eyecool.face;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.eyecool.face.utils.CameraUtil;
import com.eyecool.face.utils.SsDuckFaceRecognition;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        //1.初始化控件
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        FrameLayout flVideoContainer = (FrameLayout) findViewById(R.id.fl_container);
        ImageView ivFace = (ImageView) findViewById(R.id.iv_face);

        //初始化算法和相机
        SsDuckFaceRecognition.init(this);
        CameraUtil.init(flVideoContainer, surfaceView, getApplicationContext(), null, ivFace);
    }
}
