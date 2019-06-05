package com.demo.facerecognition;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.demo.facerecognition.view.FaceView;

import java.io.IOException;

/**
 * 人脸识别预览视频由相机预览显示
 * 这个可以根据SDK决定
 * <p>
 * 预览界面
 */
public class PreviewActivity extends AppCompatActivity {
    private final String TAG = "PreviewActivity";
    private Camera camera;
    private boolean isPreview = false;

    static final String[] PERMISSION = new String[]{
            //获取照相机权限
            Manifest.permission.CAMERA,
    };

    /**
     * 设置Android6.0的权限申请
     */
    private void setPermissions() {
        if (ContextCompat.checkSelfPermission(PreviewActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //Android 6.0申请权限
            ActivityCompat.requestPermissions(this, PERMISSION, 1);
        } else {
            Log.i(TAG, "权限申请ok");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        //初始化布局
        ConstraintLayout constraintLayout = findViewById(R.id.cl_root);
        final FaceView faceView = findViewById(R.id.fv_title);
        ImageView imageView = findViewById(R.id.iv_close);
        //申请手机的权限
        setPermissions();

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = (int) (1 + Math.random() * 4);
                switch (i) {
                    case 1:
                        faceView.resetPositionStart();
                        faceView.updateTipsInfo("没有检测人脸");
                        break;
                    case 2:
                        faceView.backAnimator();
                        faceView.updateTipsInfo("请露正脸");
                        break;
                    case 3:
                        faceView.pauseAnimator();
                        faceView.updateTipsInfo(" 眨眨眼");
                        break;
                    case 4:
                        faceView.startAnimator();
                        faceView.updateTipsInfo("离近一点");
                        break;
                    default:
                        break;
                }
            }
        });

        //添加布局
        SurfaceView mSurfaceView = new SurfaceView(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mSurfaceView.setLayoutParams(params);
        constraintLayout.addView(mSurfaceView, 0);
        //得到getHolder实例
        SurfaceHolder mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        // 添加 Surface 的 callback 接口
        mSurfaceHolder.addCallback(mSurfaceCallback);
    }

    private SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            try {
                //打开硬件摄像头，这里导包得时候一定要注意是android.hardware.Camera
                // Camera,open() 默认返回的后置摄像头信息
                //设置角度，此处 CameraId 我默认 为 1 （前置）
                if (Camera.getNumberOfCameras() > 1) {
                    camera = Camera.open(1);
                } else {
                    camera = Camera.open(0);
                }
                //设置相机角度
                camera.setDisplayOrientation(90);
                //通过SurfaceView显示取景画面
                camera.setPreviewDisplay(surfaceHolder);
                //开始预览
                camera.startPreview();
                //设置是否预览参数为真
                isPreview = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            if (camera != null) {
                if (isPreview) {//正在预览
                    try {
                        camera.stopPreview();
                        camera.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        if (camera != null) {
            if (isPreview) {//正在预览
                try {
                    camera.stopPreview();
                    camera.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        super.onDestroy();
    }
}