package com.witcher.autoscrollview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import java.util.ArrayList;

public class AutoScrollView extends ViewGroup {

    private ArrayList<View> mViewList = new ArrayList<>();
    private AutoScrollAdapter mAdapter;
    private int mShowCount = 2;//最多同时出现view的数量
    private int mViewCount = mShowCount;

    private float mSpeed = 100.0f;//每秒滚动的像素
    private int mChildHeight;
    private int mCurrentShow;
    private Scroller mScroller;
    private boolean mNeedLayoutChild = true;

    private int mIndex;


    public AutoScrollView(Context context) {
        super(context);
        init();
    }

    public AutoScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AutoScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        /*
        1.创建一组view 添加进来
        2.view自动滚动
        3.view从底部滚上来时绑定数据
        4.适配器notify的内容
         */
        /*
        每次滚动一个条目 滚完了再滚下一个条目
         */
        mScroller = new Scroller(getContext(), new LinearInterpolator());
    }

    public void startScroll() {// 0 0 to -138 -138 | -138 138 to -276 0 | 0 0 to -138 -138
        int start = 0;
        int end = -mChildHeight;
        mCurrentShow++;
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setIntValues(start, end);
        valueAnimator.setDuration(getDuration());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            int lastValue = 0;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                int size = mViewList.size();
                for (int i = 0; i < size; ++i) {
                    View view = mViewList.get(i);
//                    view.setTranslationY(value);
                    view.offsetTopAndBottom(value - lastValue);
                }
                lastValue = value;
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //到底了就把第一个挪下来然后绑定数据
                if (mCurrentShow == mViewCount - 1) {
                    View topView = mViewList.remove(0);
                    mViewList.add(topView);
                    topView.offsetTopAndBottom(mChildHeight + getHeight());//去父view的下边呆着
                    int count = mAdapter.getCount();
                    if(mIndex == count){
                        //说明到最后一个数据了
                        mIndex = 0;
                    }
                    mAdapter.bindView(mIndex, topView);//绑定数据 这里还得做判断有没有下一条数据 没有的话就循环回0
                    //这里会触发一次layout 0,138
                    mIndex++;
                    mCurrentShow = 0;
//                    //继续滚动 0 0 to -138 -138 | -138 138 to -276 0 | 0 0 to -138 -138
                    startScroll();
                } else {
                    startScroll();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        valueAnimator.start();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            //这里对每个view修改top
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        L.i("onLayout");
        /*
        所有view竖向排列下去
         */
//        if(mNeedLayoutChild){
        int size = mViewList.size();
        int childViewCountHeight = 0;
        for (int i = 0; i < size; ++i) {
            View childView = mViewList.get(i);
            int childHeight = childView.getMeasuredHeight();
            L.i("childHeight:" + childHeight);
            int childViewTop = childViewCountHeight;
            childView.layout(left, childViewTop, right, childViewTop + childHeight);
            childViewCountHeight = childViewCountHeight + childHeight;
            mChildHeight = childHeight;
        }
//            mNeedLayoutChild = false;
//        }
    }

    private int getDuration() {
        return (int) (mChildHeight / mSpeed * 1000);
    }

    private void bindAdapter() {
        L.i("bindAdapter");
        if (mAdapter == null) {
            return;
        }
        removeAllViews();
        mViewList.clear();
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        int count = mAdapter.getCount();
        for (int i = 0; i < mViewCount; ++i) {// 0 1
            View view = layoutInflater.inflate(mAdapter.getLayoutId(), null);
            //这里高度给一个具体数字  子view就是那个宽度了
            addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            mViewList.add(view);
            if (i < count) {
                mAdapter.bindView(mIndex, view);
                mIndex++;
            } else {//数据不足
                view.setVisibility(View.INVISIBLE);
            }
        }

    }

    public void setAdapter(AutoScrollAdapter adapter) {
        this.mAdapter = adapter;
        bindAdapter();
        this.mAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                L.i("onChanged");
            }
        });
    }

    public void test1() {
        startScroll();
    }

    public void test2() {
        for (View view : mViewList) {
            L.i("getTranslationY:" + view.getTranslationY());
        }
    }
}
