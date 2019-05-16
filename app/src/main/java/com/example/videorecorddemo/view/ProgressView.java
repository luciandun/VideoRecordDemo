package com.example.videorecorddemo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * description:
 * author: dlx
 * date: 2019/05/15
 * version: 1.0
 */
public class ProgressView extends View {

    private int mWidth;
    private int mHeight;
    private int progress = 10;
    /**
     * 圆半径
     */
    private int mRadius;
    private Point centerPoint;

    private Paint mPaintProgress;
    private Paint mPaintText;

    private Rect mTextBounds;
    private RectF mProgressBounds;

    public ProgressView(Context context) {
        super(context);
        init();
    }

    public ProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaintProgress = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintProgress.setStyle(Paint.Style.STROKE);
        mPaintProgress.setColor(Color.GRAY);
        mPaintProgress.setStrokeCap(Paint.Cap.ROUND);
        mPaintProgress.setStrokeWidth(5);

        mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintText.setStyle(Paint.Style.FILL);
        mPaintText.setColor(Color.WHITE);
        mPaintText.setTextSize(30);

        mTextBounds = new Rect();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        calculateRadius();
    }

    private void calculateRadius() {
        mRadius = Math.min(mWidth, mHeight) / 3 * 2 / 2;
        centerPoint = new Point(mWidth / 2, mHeight / 2);
        mProgressBounds = new RectF(centerPoint.x - mRadius, centerPoint.y - mRadius,
                centerPoint.x + mRadius, centerPoint.y + mRadius);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        calculateRadius();
    }

    /**
     * @param progress 当前完成进度
     */
    public void setProgress(int progress) {
        if (progress < 0) progress = 0;
        if (progress > 100) progress = 100;
        this.progress = progress;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        String text = this.progress + "%";

        mPaintText.getTextBounds(text, 0, text.length(), mTextBounds);
        int tw = mTextBounds.width();
        int th = mTextBounds.height();

        //画文字
        canvas.drawText(text, centerPoint.x - tw / 2F, centerPoint.y + th / 2F, mPaintText);

        //画背景
        mPaintProgress.setColor(Color.GRAY);
        canvas.drawCircle(centerPoint.x, centerPoint.y, mRadius, mPaintProgress);

        //画进度前景
        mPaintProgress.setColor(Color.WHITE);
        canvas.drawArc(mProgressBounds, 0, progress * 360F / 100, false, mPaintProgress);
        canvas.restore();
    }
}
