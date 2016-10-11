/*
 * Copyright 2016 czy1121
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.czy1121.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.github.czy1121.segmentedview.R;

import java.util.ArrayList;
import java.util.List;

public class SegmentedView extends View {

    private int mMainColor;
    private int mSubColor;
    private int mCornerRadius;
    private int mStrokeWidth;
    private int mHPadding;
    private int mVPadding;

    float[] mRadiiFirst;
    float[] mRadiiLast;

    private int mTouchSlop;
    private boolean inTapRegion;
    private float mStartX;
    private float mStartY;
    private int mCurrentIndex;
    private int mItemWidth;
    private int mItemHeight;

    String[] mTexts = {};
    Rect[] mRects;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private TextPaint mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    GradientDrawable mRound = new GradientDrawable();

    private float mTextSize;


    public SegmentedView(Context context) {
        this(context, null);
    }

    public SegmentedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        int touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mTouchSlop = touchSlop * touchSlop;

        DisplayMetrics dm = getResources().getDisplayMetrics();
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SegmentedView);

        mTextSize = a.getDimension(R.styleable.SegmentedView_android_textSize, 14 * dm.scaledDensity);

        mMainColor = a.getColor(R.styleable.SegmentedView_svMainColor, 0);
        mSubColor = a.getColor(R.styleable.SegmentedView_svSubColor, Color.WHITE);
        mCornerRadius = a.getDimensionPixelSize(R.styleable.SegmentedView_svCornerRadius, -1);
        mStrokeWidth = a.getDimensionPixelSize(R.styleable.SegmentedView_svStrokeWidth, (int) (2 * dm.density));

        mHPadding = a.getDimensionPixelSize(R.styleable.SegmentedView_svHPadding, (int) (5 * dm.density));
        mVPadding = a.getDimensionPixelSize(R.styleable.SegmentedView_svVPadding, (int) (0 * dm.density));

        String texts = a.getString(R.styleable.SegmentedView_svTexts);

        a.recycle();

        if (!TextUtils.isEmpty(texts)) {
            setTexts(texts.split("\\|"));
        }

        mTextPaint.setColor(mSubColor);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(mTextSize);

        mPaint.setColor(mMainColor);
        mPaint.setStrokeWidth(mStrokeWidth);

        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setCornerRadius(mCornerRadius);
        gd.setColor(mSubColor);
        gd.setStroke(mStrokeWidth, mMainColor);
        setBackground(gd);

        mRound.setCornerRadius(mCornerRadius);
        mRound.setColor(mMainColor);
        mRound.setStroke(mStrokeWidth, 0);

        mRadiiFirst = new float[]{mCornerRadius, mCornerRadius, 0f, 0f, 0f, 0f, mCornerRadius, mCornerRadius};
        mRadiiLast = new float[]{0f, 0f, mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius, 0f, 0f};

    }


    private OnItemSelectedListener mListener;

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        mListener = listener;
    }

    public void setTexts(String... texts) {
        if (texts == null || texts.length == 0) {
            return;
        }
        List<String> list = new ArrayList<>();
        for (String s : texts) {
            if (TextUtils.isEmpty(s.replaceAll("[ ]*", ""))) {
                continue;
            }
            list.add(s);
        }
        mTexts = list.toArray(mTexts);
        mRects = new Rect[mTexts.length];
        for (int i = 0; i < mRects.length; i++) {
            mRects[i] = new Rect();
        }
        mCurrentIndex = 0;
        requestLayout();
    }

    Rect mTemp = new Rect();

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mTexts == null || mTexts.length == 0) {
            setMeasuredDimension(normalize(widthMeasureSpec, 0), normalize(heightMeasureSpec, 0));
            return;
        }

        final int count = mTexts.length;

        mItemHeight = 0;
        mItemWidth = 0;

        for (int i = 0; i < count; i++) {
            String text = mTexts[i];
            mTextPaint.getTextBounds(text, 0, text.length(), mTemp);

            int w = mTemp.width() + mHPadding * 2;
            int h = mTemp.height() + mVPadding * 2;
            if (mItemWidth < w) {
                mItemWidth = w;
            }
            if (mItemHeight < h) {
                mItemHeight = h;
            }
        }

        int width = normalize(widthMeasureSpec, mItemWidth * count);
        int height = normalize(heightMeasureSpec, mItemHeight);

        mItemWidth = width / count;
        mItemHeight = height;

        int extra = width % count;
        for (int i = 0; i < count; i++) {
            Rect rect = mRects[i];
            rect.set(0, 0, mItemWidth, mItemHeight);
            rect.offsetTo(i * mItemWidth, 0);
        }

        mRects[count - 1].right += extra;

        setMeasuredDimension(width, height);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mTexts == null || mTexts.length == 0) {
            return;
        }
        int count = mTexts.length;
        for (int i = 0; i < count; i++) {
            Rect rect = mRects[i];
            boolean selected = mCurrentIndex == i;
            if (i > 0) {
                canvas.drawLine(rect.left, 0, rect.left, rect.height(), mPaint);
            }
            if (selected) {
                if (i == 0 || i == count - 1) { // first or last
                    mRound.setBounds(rect);
                    if (count == 1) { // only one
                        mRound.setCornerRadius(mCornerRadius);
                    } else {
                        mRound.setCornerRadii(i == 0 ? mRadiiFirst : mRadiiLast);
                    }
                    mRound.draw(canvas);
                } else {
                    canvas.drawRect(rect, mPaint);
                }
            }

            mTextPaint.setColor(selected ? mSubColor : mMainColor);
            canvas.drawText(mTexts[i], rect.exactCenterX(), rect.exactCenterY() - (mTextPaint.ascent() + mTextPaint.descent()) / 2, mTextPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
            inTapRegion = true;
            mStartX = event.getRawX();
            mStartY = event.getRawY();
            break;
        case MotionEvent.ACTION_MOVE:
            int dx = (int) (event.getRawX() - mStartX);
            int dy = (int) (event.getRawY() - mStartY);
            int distance = dx * dx + dy * dy;

            if (distance > mTouchSlop) {
                inTapRegion = false;
            }
            break;

        case MotionEvent.ACTION_UP:
            if (inTapRegion) {
                int[] loc = new int[2];
                getLocationOnScreen(loc);
                mCurrentIndex = (int) (mStartX - loc[0]) / mItemWidth;

                if (mListener != null) {
                    mListener.onSelected(mCurrentIndex, mTexts[mCurrentIndex]);
                }

                invalidate();
            }
            break;
        default:
            break;
        }
        return true;
    }

    int normalize(int spec, int value) {
        int mode = MeasureSpec.getMode(spec);
        int size = MeasureSpec.getSize(spec);

        switch (mode) {
        case MeasureSpec.AT_MOST:
            return Math.min(size, value);
        case MeasureSpec.EXACTLY:
            return size;
        case MeasureSpec.UNSPECIFIED:
        default:
            return value;
        }
    }


    public interface OnItemSelectedListener {
        void onSelected(int index, String text);
    }
}