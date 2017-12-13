package com.eyecool.face.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.eyecool.face.Constants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created date: 2017/6/6
 * Author:  Leslie
 */

public class CameraUtil {

    private static Camera camera;
    private static Bitmap dstbmp;
    private static long currentTimeMillis = 0;
    private static long totalObtainFrameUsedTime = 0;
    private static int totalObtainFrameTimes = 0;
    private static ImageView iv;
    private static ImageView ivCorrect;
    private static FrameLayout mParentView;
    private static SurfaceView mSurfaceView;
    public static ArrayList<Bitmap> bitmaps = new ArrayList<>();
    public static List<String> base64List = new ArrayList<>();
    static int picNum = 0;
    private static Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Size size = camera.getParameters().getPreviewSize();
            long callBackUsedTimes;
            if (currentTimeMillis != 0) {
                callBackUsedTimes = System.currentTimeMillis() - currentTimeMillis;
                totalObtainFrameUsedTime += callBackUsedTimes;
                totalObtainFrameTimes++;
                long averageUsedTime = (totalObtainFrameUsedTime / totalObtainFrameTimes);
//                Log.d(TAG, "onPreviewFrame: -----------------------------最新回调之间间隔：" + callBackUsedTimes + "ms");
//                Log.d(TAG, "onPreviewFrame: -----------------------------回调之间平均间隔：" + averageUsedTime + "ms");
            }
            currentTimeMillis = System.currentTimeMillis();
            try {
                YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                if (image != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, stream);

                    Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());


                    Canvas canvas = new Canvas();
                    // 设置canvas画布背景为白色
                    canvas.drawColor(Color.BLACK);
                    // 在画布上绘制缩放之前的位图，以做对比
                    //屏幕上的位置坐标是0,0
                    canvas.drawBitmap(bmp, 0, 0, null);
                    // 定义矩阵对象
                    Matrix matrix = new Matrix();
                    // 缩放原图
                    matrix.postScale(1f, 1f);
                    // 向左旋转90度，参数为正则向右旋转,如果是横屏的话，就不需要转了。
                    if (!isLandscape) {

                        matrix.postRotate(-90);
                    }
                    //bmp.getWidth(), 500分别表示重绘后的位图宽高
                    dstbmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                    // 在画布上绘制旋转后的位图
                    //放在坐标为0,200的位置
                    canvas.drawBitmap(dstbmp, 0, 200, null);

                    //转化完摄像头的图像数据之后，要1.画人脸框;2.截取人脸图片，存到集合里面去
                    if (SsDuckFaceRecognition.drawFaceFrame(mParentView, mSurfaceView, dstbmp)) {
                        float[] faceLoc = new float[3];

                        faceLoc[0] = SsDuckFaceRecognition.faceRect[0];
                        faceLoc[1] = SsDuckFaceRecognition.faceRect[1];
                        faceLoc[2] = SsDuckFaceRecognition.faceRect[2];

                        //1.从截图中截取含有人脸的图片，如果摄像头设置成镜像的话，就需要对图片做镜像处理。。。。。。。。。。。。。。，然后再转换成base64
                        Bitmap cropBitmap = BitmapUtil.cropBitmap(dstbmp, faceLoc, 2f);
                        Bitmap mirrorBitmap = BitmapUtil.mirrorPic(cropBitmap);
                        ivCorrect.setImageBitmap(mirrorBitmap);
                    }

                    //TODO：此处可以对位图进行处理，如显示，保存等
                    stream.close();
                }
            } catch (Exception ex) {
                Log.e("Sys", "Error:" + ex.getMessage());
            }
        }
    };
    private static boolean isLandscape = true;


    public static void init(FrameLayout parentView, SurfaceView surfaceView, final Context context, ImageView srcView, ImageView correctView) {
        iv = srcView;
        ivCorrect = correctView;
        mParentView = parentView;
        mSurfaceView = surfaceView;
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                camera = Camera.open(Constants.CAMERA_FRONT_ID);
                try {
                    //设置预览监听
                    camera.setPreviewDisplay(holder);
                    Camera.Parameters parameters = camera.getParameters();
                    parameters.setPreviewSize(640, 480);
                    int orientation = context.getResources().getConfiguration().orientation;
                    Log.d(TAG, "surfaceCreated: -----------------------current orientation = " + orientation);

                    if (orientation != Configuration.ORIENTATION_LANDSCAPE) {
                        isLandscape = false;
                        parameters.set("orientation", "portrait");
                        camera.setDisplayOrientation(90);
                        parameters.setRotation(90);
                        Log.d(TAG, "initCamera1: -----------------------竖屏！");
                    } else {
                        isLandscape = true;
                        parameters.set("orientation", "landscape");
                        camera.setDisplayOrientation(0);
                        parameters.setRotation(0);
                        Log.d(TAG, "initCamera1: -----------------------横屏！");
                    }
                    List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
                    for (Camera.Size pictureSize : pictureSizes) {
                        int height = pictureSize.height;
                        int width = pictureSize.width;
                        Log.d(TAG, "surfaceCreated: ----------------picture width = " + width);
                        Log.d(TAG, "surfaceCreated: ----------------picture height = " + height);
                    }
                    camera.setParameters(parameters);
                    camera.setPreviewCallback(previewCallback);
                    //启动摄像头预览
                    camera.startPreview();
                    System.out.println("camera.startpreview");

                } catch (IOException e) {
                    e.printStackTrace();
                    camera.release();
                    System.out.println("camera.release");
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                holder.removeCallback(this);

//        cameraManager.releaseCamera();
                if (camera != null) {
                    camera.setPreviewCallback(null);
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                }
            }
        };
        surfaceHolder.addCallback(callback);
    }

}
