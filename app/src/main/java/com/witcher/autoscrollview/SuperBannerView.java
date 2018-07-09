package com.witcher.autoscrollview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.ArrayList;
import java.util.List;

public class SuperBannerView extends View {

    private static final String TAG = "SuperBannerView";

    public static abstract class BannerElement {

        // px
        public float leftPadding;
        public float rightPadding;

        public BannerElement() {
            this(0, 0);
        }

        public BannerElement(float leftPadding, float rightPadding) {
            this.leftPadding = leftPadding;
            this.rightPadding = rightPadding;
        }

        abstract protected void onDraw(Canvas canvas, RectF rectF);

        abstract protected float getContentWidth();

        abstract protected void onClick();
    }

    public interface OnBannerScrollListener {

        void onStart();

        boolean onEnd();

        void onCancel();
    }

    private final List<BannerElement> mElements = new ArrayList<>();
    private ValueAnimator mValueAnimator;
    private float mTranslateX = 0.0f;
    private RectF mDrawRect = new RectF();
    private long mDelayMillis = 0;
    private OnBannerScrollListener mScrollListener;

    private final ViewTreeObserver.OnPreDrawListener mOnPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            getViewTreeObserver().removeOnPreDrawListener(mOnPreDrawListener);
            startScroll();
            return true;
        }
    };

    public SuperBannerView(Context context) {
        super(context);
        init(context);
    }

    public SuperBannerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SuperBannerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SuperBannerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {

    }

    public void setOnBannerScrollListener(OnBannerScrollListener l) {
        mScrollListener = l;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver()
                .addOnPreDrawListener(mOnPreDrawListener);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight();
        float translateHeight = (float) height * mTranslateX;
        float left = 0.0f;
        canvas.save();
        canvas.translate(0, -translateHeight);
        for (BannerElement element : mElements) {
            if (left > getWidth()) {
                break;
            }
            left += element.leftPadding;
            float right = left + element.getContentWidth();
            mDrawRect.set(left, 0.0f, right, height);
            canvas.save();
            element.onDraw(canvas, mDrawRect);
            canvas.restore();
            left = right;
            left += element.rightPadding;
        }
        canvas.restore();
    }

    public void inserElement(BannerElement element) {
        mElements.add(element);
        postInvalidate();
    }

    public void insertElement(int position, BannerElement element) {
        mElements.add(position, element);
        postInvalidate();
    }

    public void clear() {
        mElements.clear();
        postInvalidate();
    }

    public void setAnimatorDelayed(long delayMillis) {
        mDelayMillis = delayMillis;
    }

    public boolean isRunning() {
        return mValueAnimator != null && (mValueAnimator.isRunning() || mValueAnimator.isStarted());
    }

    public void startScroll() {
        if (mElements.isEmpty()) {
            return;
        }
        if (isRunning()) {
            mValueAnimator.cancel();
            mValueAnimator = null;
            return;
        }
        mValueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        mValueAnimator.setDuration(1000);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mTranslateX = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        mValueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (mScrollListener != null) {
                    mScrollListener.onStart();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mTranslateX = 0.0f;
                if (mScrollListener != null) {
                    if (!mScrollListener.onEnd()) {
                        return;
                    }
                }
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startScroll();
                    }
                }, mDelayMillis);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                if (mScrollListener != null) {
                    mScrollListener.onCancel();
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mValueAnimator.start();
        Log.i(TAG, "Start scroll height=" + getHeight());
    }

}
