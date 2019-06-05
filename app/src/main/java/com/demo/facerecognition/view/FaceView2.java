package com.demo.facerecognition.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.demo.facerecognition.R;
import com.demo.facerecognition.util.ScreenUtils;

/**
 * <pre>
 * 文件名：	FaceView
 * 作　者：	zj
 * 时　间：	2019/1/2 18:27
 * 描　述：人脸识别界面UI
 * </pre>
 */
public class FaceView2 extends View implements Runnable {
    private final String TAG = "FaceView";
    /**
     * 是否可以开始绘制了
     */
    private boolean mStart = false;
    /**
     * 默认中间圆的半径从0开始
     */
    private float currentRadius = 0;
    /**
     * 控件的宽度（默认）
     */
    private int mViewWidth = 400;
    /**
     * 控件高度
     */
    private int mViewHeight = 400;
    /**
     * 中心圆屏幕边距
     */
    private int margin;
    /**
     * 圆圈画笔
     */
    private Paint mPaint;
    /**
     * 提示文本
     */
    private String mTipText;
    /**
     * 提示文本颜色
     */
    private int mTipTextColor;
    /**
     * 提示文本颜色
     */
    private int mTipTextSize;
    /**
     * 内圆半径
     */
    private int mRadius;
    /**
     * 背景弧宽度
     */
    private float mBgArcWidth;

    /**
     * 圆心点坐标
     */
    private Point mCenterPoint = new Point();
    /**
     * 圆弧边界
     */
    private RectF mBgRectF = new RectF();

    /**
     * 开始角度
     */
    private int mStartAngle = 105;

    /**
     * 结束角度
     */
    private int mEndAngle = 330;

    /**
     * 设置默认转动角度0
     */
    float currentAngle = 0;

    /**
     * 圆弧背景画笔
     */
    private Paint mBgArcPaint;
    /**
     * 提示语画笔
     */
    private Paint mTextPaint;

    /**
     * 圆弧画笔
     */
    private Paint mArcPaint;
    /**
     * 渐变器
     */
    private SweepGradient mSweepGradient;

    /**
     * 是否开始
     */
    private boolean isRunning = true;

    /**
     * 是否后退
     */
    private boolean isBack = false;
    /**
     * 绘制速度
     */
    private int speed = 5;


    public FaceView2(Context context) {
        this(context, null);
    }

    public FaceView2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FaceView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //获取xml里面的属性值
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.FaceView);
        mTipText = array.getString(R.styleable.FaceView_tip_text);
        mTipTextColor = array.getColor(R.styleable.FaceView_tip_text_color, Color.WHITE);
        mTipTextSize = array.getDimensionPixelSize(R.styleable.FaceView_tip_text_size, ScreenUtils.sp2px(context, 12));
        array.recycle();

        Log.d(TAG, "FaceView构造");
        initPaint(context);
    }

    /**
     * 初始化控件View
     */
    private void initPaint(Context context) {
        //获取界面焦点
        setFocusable(true);
        //保持屏幕长亮
        setKeepScreenOn(true);

        //初始化值
        margin = ScreenUtils.dp2px(context, 60);
        mBgArcWidth = ScreenUtils.dp2px(context, 5);

        //初始化画笔
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(getResources().getColor(R.color.colorAccent));
        mPaint.setStyle(Paint.Style.FILL);

        //绘制文字画笔
        mTextPaint = new Paint();
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setStrokeWidth(8);
        mTextPaint.setColor(mTipTextColor);
        mTextPaint.setTextSize(mTipTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        // 圆弧背景
        mBgArcPaint = new Paint();
        mBgArcPaint.setAntiAlias(true);
        mBgArcPaint.setColor(getResources().getColor(R.color.circleBg));
        mBgArcPaint.setStyle(Paint.Style.STROKE);
        mBgArcPaint.setStrokeWidth(mBgArcWidth);
        mBgArcPaint.setStrokeCap(Paint.Cap.ROUND);

        // 圆弧
        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(mBgArcWidth);
        mArcPaint.setStrokeCap(Paint.Cap.ROUND);

        //开启线程检测
        new Thread(this).start();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //测量view的宽度
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            mViewWidth = MeasureSpec.getSize(widthMeasureSpec);
        }

        //测量view的高度
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.EXACTLY) {
            mViewHeight = MeasureSpec.getSize(heightMeasureSpec);
        }

        setMeasuredDimension(mViewWidth, mViewHeight);
        Log.d(TAG, "onMeasure  mViewWidth : " + mViewWidth + "  mViewHeight : " + mViewHeight);

        //获取圆的相关参数
        mCenterPoint.x = mViewWidth / 2;
        mCenterPoint.y = mViewHeight / 2;

        //外环圆的半径
        mRadius = mCenterPoint.x - margin;

        //绘制背景圆弧的边界
        mBgRectF.left = mCenterPoint.x - mRadius - mBgArcWidth / 2;
        mBgRectF.top = mCenterPoint.y - mRadius - mBgArcWidth / 2;
        mBgRectF.right = mCenterPoint.x + mRadius + mBgArcWidth / 2;
        mBgRectF.bottom = mCenterPoint.y + mRadius + mBgArcWidth / 2;

        //进度条颜色 -mStartAngle/2将位置到原处
        mSweepGradient = new SweepGradient(mCenterPoint.x - mStartAngle / 2,
                mCenterPoint.y - mStartAngle / 2, getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.colorPrimaryDark));
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        mStart = (visibility == VISIBLE);
    }

    @Override
    public void run() {
        //循环绘制画面内容
        while (true) {
            if (mStart) {
                try {
                    changeValue();
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 动态检测改变值
     */
    private void changeValue() {
        // 内圆形，放大效果
        currentRadius += 20;
        if (currentRadius > mRadius)
            currentRadius = mRadius;

        //外部圈的动画效果
        if (isRunning) {
            if (isBack) {
                currentAngle -= speed;
                if (currentAngle <= 0)
                    currentAngle = 0;
            } else {
                currentAngle += speed;
                if (currentAngle >= mEndAngle)
                    currentAngle = mEndAngle;
            }
        }
        //重绘view
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制画布内容
        drawContent(canvas);
    }

    /**
     * 跟新提示信息
     *
     * @param title
     */
    public void updateTipsInfo(String title) {
        mTipText = title;
    }

    private void drawContent(Canvas canvas) {
        //防止save()和restore()方法代码之后对Canvas执行的操作，继续对后续的绘制会产生影响
        canvas.save();
        //先画提示语
        drawHintText(canvas);
        //绘制正方形的框内类似人脸识别
//        drawFaceRectTest(canvas);
        //绘制人脸识别部分
        drawFaceCircle(canvas);
        //画外边进度条
        drawRoundProgress(canvas);
        canvas.restore();
    }

    private void drawFaceCircle(Canvas canvas) {
        //设置画板样式
        Path path = new Path();
        //以（400,200）为圆心，半径为100绘制圆 指创建顺时针方向的矩形路径
        path.addCircle(mCenterPoint.x, mCenterPoint.y, currentRadius, Path.Direction.CW);
        // 是A形状中不同于B的部分显示出来
        canvas.clipPath(path, Region.Op.DIFFERENCE);
        // 半透明背景效果
        canvas.clipRect(0, 0, mViewWidth, mViewHeight);
        //绘制背景颜色
        canvas.drawColor(getResources().getColor(R.color.viewBgWhite));
    }


    /**
     * 绘制人脸识别界面进度条
     *
     * @param canvas canvas
     */
    private void drawRoundProgress(Canvas canvas) {
        // 逆时针旋转105度
        canvas.rotate(mStartAngle, mCenterPoint.x, mCenterPoint.y);
        // 设置圆环背景
        canvas.drawArc(mBgRectF, 0, mEndAngle, false, mBgArcPaint);
        // 设置渐变颜色
        mArcPaint.setShader(mSweepGradient);
        canvas.drawArc(mBgRectF, 0, currentAngle, false, mArcPaint);
    }

    /**
     * 从头位置开始动画
     */
    public void resetPositionStart() {
        currentAngle = 0;
        isBack = false;
    }

    /**
     * 动画直接完成
     */
    public void finnishAnimator() {
        currentAngle = mEndAngle;
        isBack = false;
    }

    /**
     * 停止动画
     */
    public void pauseAnimator() {
        isRunning = false;
    }

    /**
     * 开始动画
     */
    public void startAnimator() {
        isRunning = true;
    }

    /**
     * 动画回退
     */
    public void backAnimator() {
        isRunning = true;
        isBack = true;
    }

    /**
     * 动画前进
     */
    public void forwardAnimator() {
        isRunning = true;
        isBack = false;
    }

    /**
     * 绘制人脸识别提示
     *
     * @param canvas canvas
     */
    private void drawHintText(Canvas canvas) {
        //圆视图宽度 （屏幕减去两边距离）
        int cameraWidth = mViewWidth - 2 * margin;
        //x轴起点（文字背景起点）
        int x = margin;
        //宽度（提示框背景宽度）
        int width = cameraWidth;
        //y轴起点
        int y = (int) (mCenterPoint.y - mRadius);
        //提示框背景高度
        int height = cameraWidth / 4;
        Rect rect = new Rect(x, y, x + width, y + height);
        canvas.drawRect(rect, mPaint);

        //计算baseline
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float distance = (fontMetrics.bottom - fontMetrics.top) / 4;
        float baseline = rect.centerY() + distance;
        canvas.drawText(mTipText, rect.centerX(), baseline, mTextPaint);
    }

    /**
     * 绘制人脸识别矩形区域
     *
     * @param canvas canvas
     */
    private void drawFaceRectTest(Canvas canvas) {
        int cameraWidth = mViewWidth - 2 * margin;
        int x = margin + cameraWidth / 6;
        int width = cameraWidth * 2 / 3;
        int y = mCenterPoint.x + (width / 2);
        int height = width;
        Rect rect = new Rect(x, y, x + width, y + height);
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(rect, mPaint);
    }
}