package com.witcher.autoscrollview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class SuperBannerTextElement extends SuperBannerView.BannerElement {

    private Paint mPaint;
    private String mContent;

    public SuperBannerTextElement() {
        this(new Paint());
    }

    public SuperBannerTextElement(Paint paint) {
        super();
        this.mPaint = new Paint(paint);
    }

    public SuperBannerTextElement(float leftPadding, float rightPadding, Paint paint, String content) {
        super(leftPadding, rightPadding);
        this.mPaint = new Paint(paint);
        this.mContent = content;
    }

    public void setContent(String content) {
        mContent = content;
    }

    public void setTextSize(float textSize) {
        mPaint.setTextSize(textSize);
    }

    public void setTextColor(int textColor) {
        mPaint.setColor(textColor);
    }

    @Override
    protected void onDraw(Canvas canvas, RectF rectF) {
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        float top = fontMetrics.top; //为基线到字体上边框的距离
        float bottom = fontMetrics.bottom; //为基线到字体下边框的距离
        int baseLineY = (int) (rectF.centerY() - top / 2 - bottom / 2); //基线中间点的y轴计算公式
        canvas.drawText(mContent, rectF.left, baseLineY, mPaint);
    }

    @Override
    protected float getContentWidth() {
        return mPaint.measureText(mContent);
    }

    @Override
    protected void onClick() {

    }
}
