package com.demo.facerecognition.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.demo.facerecognition.R;
import com.demo.facerecognition.util.ScreenUtils;

/**
 * <pre>
 * 文件名：	FaceView
 * 作　者：	zj
 * 时　间：	2019/1/2 18:27
 * 描　述：人脸识别界面UI，这里可以接视频流和UI，也可以单纯作为UI展示
 * </pre>
 */
public class FaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private final String TAG = "FaceView";
    private SurfaceHolder mSurfaceHolder;
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

    /**
     * 设置默认转动角度0
     */
    float currentAngle = 0;

    public FaceView(Context context) {
        this(context, null);
    }

    public FaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //获取xml里面的属性值
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.FaceView);
        mTipText = array.getString(R.styleable.FaceView_tip_text);
        mTipTextColor = array.getColor(R.styleable.FaceView_tip_text_color, Color.WHITE);
        mTipTextSize = array.getDimensionPixelSize(R.styleable.FaceView_tip_text_size, ScreenUtils.sp2px(context, 12));
        array.recycle();
        Log.d(TAG, "FaceView构造");
        initHolder(context);
    }

    /**
     * 初始化控件View
     */
    private void initHolder(Context context) {
        //获得SurfaceHolder对象
        mSurfaceHolder = getHolder();
        //设置透明背景
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        //添加回调
        mSurfaceHolder.addCallback(this);
        //显示顶层
        setZOrderOnTop(true);
        //防止遮住控件
        setZOrderMediaOverlay(true);
        //屏蔽界面焦点
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

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mStart = true;
        Log.d(TAG, "surfaceCreated()");
    }


    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.d(TAG, "surfaceChanged()");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mStart = false;
        Log.d(TAG, "surfaceDestroyed()");
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

        //进度条颜色 -mStartAngle将位置便宜到原处
        mSweepGradient = new SweepGradient(mCenterPoint.x - mStartAngle, mCenterPoint.y - mStartAngle, getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.colorPrimaryDark));
    }

    @Override
    public void run() {
        //循环绘制画面内容
        while (true) {
            if (mStart) {
                drawView();
            }
        }
    }

    private void drawView() {
        Canvas canvas = null;
        try {
            //获得canvas对象
            canvas = mSurfaceHolder.lockCanvas();
            //清除画布上面里面的内容
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            //绘制画布内容
            drawContent(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null) {
                //释放canvas锁，并且显示视图
                mSurfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
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
        // 圆形，放大效果
        currentRadius += 20;
        if (currentRadius > mRadius)
            currentRadius = mRadius;

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
        //判断是否正在运行
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