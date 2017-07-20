package com.eyecool.face.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

/**
 * Created date: 2017/3/10
 * Author:  Leslie
 */

public class FourAnglesFrameView extends View {
    private float left;
    private float top;
    private float right;
    private float bottom;
    private float lineLength;
    private RectF rectF1 = new RectF();
    private RectF rectF2 = new RectF();
    private RectF rectF3 = new RectF();
    private RectF rectF4 = new RectF();
    private RectF rectF5 = new RectF();
    private RectF rectF6 = new RectF();
    private RectF rectF7 = new RectF();
    private RectF rectF8 = new RectF();

    public FourAnglesFrameView(Context context, float left, float top, float right, float bottom, float lineLength) {
        super(context);
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.lineLength = lineLength;
    }

    public FourAnglesFrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //画布设置为透明色
        canvas.drawColor(Color.TRANSPARENT);
        Paint p = new Paint();
        // 设置Paint为无锯齿
        p.setAntiAlias(true);

        // 设置Paint的颜色
        int max = 0xFFFFFFFF;
        int min = 0xFF000000;
        Random random = new Random();
        int randomValue = random.nextInt( -min) % (min - max + 1) + min;
//        p.setColor(Color.rgb(250,128,10));
//        p.setColor(randomValue);
        p.setColor(Color.GREEN );

        // 设置paint的颜色和Alpha值(a,r,g,b)
        p.setAlpha(220);

        // 这里可以设置为另外一个paint对象
        // p.set(new Paint());
        // 设置字体的尺寸
        p.setTextSize(14);

        // 设置paint的风格为“空心”
        // 当然也可以设置为"实心"(Paint.Style.FILL)
//        p.setStyle(Paint.Style.STROKE);

        // 设置“空心”的外框的宽度
        p.setStrokeWidth(1);

        //画8个线段，实际上是画8个矩形，只不过这个矩形的厚度小于画笔的宽度
        int lineThick = 4;
//        lineLength = 50;
        //第1条线
        rectF1.set(left, top, left + lineLength, top + lineThick);
        //第2条线
        rectF2.set(left, top, left + lineThick, top + lineLength);
        //第3条线
        rectF3.set(right - lineLength, top, right, top + lineThick);
        //第4条线
        rectF4.set(right - lineThick, top, right, top + lineLength);
        //第5条线
        rectF5.set(left, bottom - lineThick, left + lineLength, bottom);
        //第6条线
        rectF6.set(left, bottom - lineLength, left + lineThick , bottom);
        //第7条线
        rectF7.set(right - lineLength, bottom - lineThick, right , bottom);
        //第8条线
        rectF8.set(right - lineThick, bottom - lineLength, right , bottom);

        // 绘制一空心个矩形
        canvas.drawRect(rectF1, p);
        canvas.drawRect(rectF2, p);
        canvas.drawRect(rectF3, p);
        canvas.drawRect(rectF4, p);
        canvas.drawRect(rectF5, p);
        canvas.drawRect(rectF6, p);
        canvas.drawRect(rectF7, p);
        canvas.drawRect(rectF8, p);
    }

    public void setLocation(float left, float top, float right, float bottom,float lineLength) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.lineLength = lineLength;
        postInvalidate();
    }
}
