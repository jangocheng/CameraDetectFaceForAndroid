package com.eyecool.face.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.eyecool.face.view.FourAnglesFrameView;
import com.smartshino.face.FaceAttr;
import com.smartshino.face.SsDuck;

import static com.smartshino.face.SsDuck.hOptCfg;
import static com.smartshino.face.SsDuck.phEnvSet;


/**
 * Created date: 2017/4/28
 * Author:  Leslie
 * 人脸识别的工具类，基于SsDuck
 * 使用前提：1.armeabi-v7a下有libSsDuck.so;2./src/main/assets/下有Duck.dat文件3.类：FaceAttr.java 和 SsDuck.java 必须放在 com.smartshino.face 包下.
 * 使用步骤：1.SsDuckFaceRecognition.init();2.SsDuckFaceRecognition.drawFaceFrame()
 * 注意： SsDuck.SsMobiDinit(phEnvSet, rightWidth, rightHeight, 1, bytes, hOptCfg); SsDuck.ENV_SET = phEnvSet[0];
 * 这两个方法在图片分辨率变化的时候(已经初始化过了)才调用。默认分辨率是 640 * 480。这样处理解决了重大 bug。
 */

public class SsDuckFaceRecognition {

    private static final String TAG = "SsNowFaceRecognition";

    private static Activity faceContext;
    private static FourAnglesFrameView rectView;
    public static SsDuck ssDuck;
    private static Bitmap currentBitmap;
    private static FaceAttr faceAttr = new FaceAttr();
    private static int sumCount = 0;
    private static long sumTime;
    public static int[] faceRect;
    private static byte[] bytes = new byte[1];
    private static int rightWidth = 480;
    private static int rightHeight = 640;

    /**
     * 人脸识别初始化，这个调用一次就行了
     *
     * @param context
     */
    public static void init(Activity context) {
        faceContext = context;
        SsDuck.setDetectType(SsDuck.DETECT_RECT);
        ssDuck = SsDuck.getInstance();
        ssDuck.init(faceContext);

        SsDuck.SsMobiDinit(phEnvSet, rightWidth, rightHeight, 1, bytes, hOptCfg);
        SsDuck.ENV_SET = phEnvSet[0];
//        currentBitmap = bitmap;
    }

    /**
     * 初始化识别图片尺寸
     *
     * @parbitmap 这步耗时一般在 1ms 以内
     */
    private static void initBitmapSize(Bitmap bitmap) {
        SsDuck.SsMobiDinit(phEnvSet, bitmap.getWidth(), bitmap.getHeight(), 1, bytes, hOptCfg);
        SsDuck.ENV_SET = phEnvSet[0];
        currentBitmap = bitmap;
    }

    /**
     * 获取人脸矩形框数据
     * 这步耗时 100ms 以内。
     *
     * @return
     */
    private static int[] getFaceRect() {

        byte[] rgbValuesFromBitmap = BitmapUtil.getRgbValuesFromBitmap(currentBitmap);
        int faceCount = SsDuck.SsMobiFrame(rgbValuesFromBitmap, 0, 0, SsDuck.ENV_SET);
        if (faceCount > 0) {

            faceRect = faceAttr.getFaceRect();
            if (SsDuck.SsMobiIsoGo(SsDuck.TD_RECT, faceRect, 0, 0, SsDuck.ENV_SET) >= 0) {
                return faceRect;
            }
        }
        return null;
    }

    /**
     * 画人脸框
     *
     * @param parentView 视频控件的父控件，调用此方法，必须有父控件，而且父控件是 FrameLayout 类型。
     * @param targetView 画框目标控件即视频控件
     * @return ture 找到人脸，画框成功; false 没有找到人脸，画框失败;
     */
    public static boolean drawFaceFrame(ViewGroup parentView, View targetView, Bitmap bitmap) {
        currentBitmap = bitmap;
        if (parentView.getChildCount() > 2) {
            if (rectView != null) {
                parentView.removeViewAt(2);
            }
        }
        //1.初始化图片尺寸
        if (bitmap.getWidth() == rightWidth && bitmap.getHeight() == rightHeight) {

        } else {
            rightWidth = bitmap.getWidth();
            rightHeight = bitmap.getHeight();
            SsDuck.SsMobiDinit(phEnvSet, rightWidth, rightHeight, 1, bytes, hOptCfg);
            SsDuck.ENV_SET = phEnvSet[0];

        }

        //2.获取图片中人脸的矩形框数据
        long time1 = System.currentTimeMillis();

        int[] faceRect = getFaceRect();
        if (faceRect == null) {
            return false;
        }


        //注意，如果是使用后置摄像头，这个数组的值要做镜像处理！！！！！！！！！！！！！！！！！！！！！！！！！！
        int[] mirrorRects = new int[4];
        //-------------开发版外接USB摄镜头，并且设置为前置的时候，显示屏为横屏状态，需要镜像处理坐标，经验值是用 550处理
        //-------------开发版外接USB摄镜头，并且设置为前置的时候，显示屏为竖屏状态，需要镜像处理坐标，经验值是用 480 处理
        //-------------人证合一Android设备检测外面的要调用后置摄像头(设备做了镜像处理了)，需要镜像处理坐标，经验值是用 480 处理
        mirrorRects[0] = rightWidth - faceRect[0] - faceRect[2];
//        mirrorRects[0] = faceRect[0];
        mirrorRects[1] = faceRect[1];
        mirrorRects[2] = faceRect[2];
        mirrorRects[3] = faceRect[3];




        long time2 = System.currentTimeMillis();
        sumCount++;
        sumTime += (time2 - time1);
        Log.d(TAG, "drawFaceFrame: --------------------获取人脸坐标耗时：" + (time2 - time1) + "ms\n平均耗时: " + (sumTime / sumCount));
        Log.d(TAG, "drawFaceFrame: ----------图片宽：" + rightWidth);
        Log.d(TAG, "drawFaceFrame: ----------图片高：" + rightHeight);
        //3.画框
        for (int i = 0; i < faceRect.length; i++) {
            Log.d(TAG, "drawFaceFrame: -------------------faceRect[" + i + "] = " + faceRect[i]);
        }


        //2.计算出人脸框左上角的坐标

        //计算控件显示图片和原图片的横向和竖向比例

        //2.计算出人脸框左上角的坐标
        //计算控件显示图片和原图片的横向和竖向比例

        float ratioHorizontal = (float) targetView.getWidth() / bitmap.getWidth();
        float ratioPortrait = (float) targetView.getHeight() / bitmap.getHeight();
        //3.计算人脸框左上角坐标，这个是相对于父控件的坐标，不是相对于窗口或者屏幕的坐标。使用相对于父控件的坐标好(用parentView.addView(rectView,1)方式时)
        float faceStartPX = 0 + mirrorRects[0] * ratioHorizontal;
        float faceStartPY = 0 + mirrorRects[1] * ratioPortrait;

        //3.计算人脸框右下角坐标

        float faceEndPX = faceStartPX + mirrorRects[2] * ratioHorizontal;
        float faceEndPY = faceStartPY + mirrorRects[2] * ratioPortrait;

        float lineLength = (faceEndPX - faceStartPX) * 0.2f;

        rectView = new FourAnglesFrameView(faceContext, faceStartPX, faceStartPY, faceEndPX, faceEndPY, lineLength);

        parentView.addView(rectView);
        return true;
    }

    public static boolean isFullScreen(Activity activity) {
        int v = activity.getWindow().getAttributes().flags;
        // 全屏 66816 - 非全屏 65792
        if (v != 66816) {//非全屏
            return true;
        } else {
            return false;
        }
    }
}
