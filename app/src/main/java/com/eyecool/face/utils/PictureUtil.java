package com.eyecool.face.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

/**
 * Created date: 2017/4/26
 * Author:  Leslie
 * 处理图片的工具类
 */

public class PictureUtil {

    public static Bitmap mirrorPic(Bitmap srcBitmap){
        Bitmap mirrorBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), srcBitmap.getConfig());
        Canvas canvas = new Canvas(mirrorBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        Matrix matrix = new Matrix();
        //matrix.setRotate(90, bm.getWidth()/2, bm.getHeight()/2);
        //matrix.setTranslate(20, 20);
        //镜子效果
        matrix.setScale(-1, 1);
        matrix.postTranslate(srcBitmap.getWidth(), 0);

        canvas.drawBitmap(srcBitmap, matrix, paint);
        return mirrorBitmap;
    }

    /**
     * 把SD卡中的图片文件解析成缩略（根据现实图片的控件宽高）的bitmap对象
     *
     * @param filePath
     * @param viewWidth
     * @param viewHeight
     * @return
     */
    public static Bitmap decodeThumbBitmapFormSDPicsFile(String filePath, int viewWidth, int viewHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;                                                       //设置为true，表示解析Bitmap对象，该对象不占内存
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        BitmapFactory.decodeFile(filePath, options);
        //设置缩放比例
        options.inSampleSize = computeScale(options, viewWidth, viewHeight);
        //设置为false，解析Bitmap对象加入到内存中
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 计算缩放比例(根据View对象的宽高)
     *
     * @param options
     * @param viewWidth
     * @param viewHeight
     * @return
     */
    public static int computeScale(BitmapFactory.Options options, int viewWidth, int viewHeight) {
        int inSampleSize = 1;
        if (viewWidth == 0 || viewHeight == 0) {
            return inSampleSize;
        }
        int bitmapWidth = options.outWidth;
        int bitmapHeight = options.outHeight;

        //假如Bitmap的宽度或者高度大于我们设置图片的View的宽高，则计算缩放比例
        if (bitmapWidth > viewWidth || bitmapHeight > viewHeight) {
            int widthScale = Math.round((float) bitmapWidth / (float) viewWidth);
            int heightScale = Math.round((float) bitmapHeight / (float) viewHeight);

            //为了保证图片不缩放变形，取宽高比例最小的那个
            inSampleSize = widthScale < heightScale ? widthScale : heightScale;
        }
        Log.d("zkx", "-------------------------------------------缩放值：" + inSampleSize);
        return inSampleSize;
    }
}
