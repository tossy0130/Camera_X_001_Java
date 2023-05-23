package com.example.camera_x_001;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {

    // 定数
    private final int REQUEST_CODE_PERMISSIONS = 101;


    // UI
    private TextureView textureView;
    private Button captureButton;

    private TextView get_with, get_height, get_gyouretu;
    private String str_get_with, str_get_height;

    // === ピンチアウト用
    private ScaleGestureDetector scaleGestureDetector;
    private float scaleFactor = 0.05f;

    private int test_count = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        textureView = findViewById(R.id.texture_view);
        captureButton = findViewById(R.id.capture_button);

     //   scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());


        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // カメラ　起動
                startCamera();
            }
        });

    } // === END onCreate

    /**
     *   === カメラ　画面タッチ　イベント ===
     */

    /*
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }

     */

    /**
     *   === カメラ　画面タッチ　イベント ===
     */

    /*
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();

            // 最小・最大スケールを制限する場合は、適宜設定してください
            scaleFactor = Math.max(0.01f, Math.min(scaleFactor, 2.0f));

            // TextureViewのサイズを更新する
            int width = (int) (textureView.getWidth() * scaleFactor);
            int height = (int) (textureView.getHeight() * scaleFactor);
            textureView.setLayoutParams(new FrameLayout.LayoutParams(width, height));

            return true;
        }
    }

*/


        // カメラの開始
        private void startCamera() {
            // プレビューの表示
            PreviewConfig pConfig = new PreviewConfig.Builder().build();
            Preview preview = new Preview(pConfig);
            preview.setOnPreviewOutputUpdateListener(

                    output -> {
                        // SurfaceTextureの更新
                        ViewGroup parent = (ViewGroup)this.textureView.getParent();
                        parent.removeView(this.textureView);
                        parent.addView(this.textureView, 0);

                        // SurfaceTextureをTextureViewに指定
                        this.textureView.setSurfaceTexture(output.getSurfaceTexture());

                        // TextureViewのサイズの調整
                        int w = output.getTextureSize().getWidth();
                        int h = output.getTextureSize().getHeight();
                        int degree = output.getRotationDegrees();
                        if (degree == 90 || degree == 270) {
                            w = output.getTextureSize().getHeight();
                            h = output.getTextureSize().getWidth();
                        }
                        h = h * textureView.getWidth() / w;
                        w = textureView.getWidth();
                        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(w,h);
                        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                        textureView.setLayoutParams(params);;

                    });

                    // 画像の解析
                    ImageAnalysisConfig config = new ImageAnalysisConfig.Builder()
                            .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                            .build();
                    ImageAnalysis imageAnalysis = new ImageAnalysis(config);
                    imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(),
                            new ImageAnalysis.Analyzer() {
                                @Override
                                public void analyze(ImageProxy image, int rotationDegrees) {

                                    // === 横　、縦　取得
                                    str_get_with = String.valueOf(image.getWidth() * test_count);
                                    str_get_height = String.valueOf(image.getHeight() * test_count);

                                    // === 表示行列 取得
                                    Matrix transformMatrix = new Matrix();
                                    String Tmp_Matrix = String.valueOf(textureView.getTransform(transformMatrix));

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            get_with.setText(str_get_with); // 横
                                            get_height.setText(str_get_height); // 縦

                                            get_gyouretu.setText(Tmp_Matrix); // 表示行列
                                        }
                                    });

                                    android.util.Log.d("出力:::analyze:::",+image.getWidth()+"x"+image.getHeight());
                                    android.util.Log.d("出力:::analyze:::",Tmp_Matrix);

                                    test_count++;


                                }
                            });




                    // 画像のキャプチャ
                    ImageCaptureConfig cConfig = new ImageCaptureConfig.Builder()
                            .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                            .build();
                    ImageCapture imageCapture = new ImageCapture(cConfig);

                    // ボタンのイベントリスナー
                    captureButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 画像のキャプチャ
                            File file = new File(getFilesDir(), "captured.jpg");
                            imageCapture.takePicture(file, Executors.newSingleThreadExecutor(),
                                    new ImageCapture.OnImageSavedListener() {
                                        // 成功時に呼ばれる
                                        @Override
                                        public void onImageSaved(File file) {
                                            android.util.Log.d("debug","success フォト保存OKOKOK");
                                        }

                                        // エラー時に呼ばれる
                                        @Override
                                        public void onError(
                                                ImageCapture.ImageCaptureError imageCaptureError,
                                                String message, Throwable cause) {
                                            android.util.Log.d("debug","error");
                                        }
                                    });
                        }
                    });


            // カメラのライフサイクルのバインド
           // CameraX.bindToLifecycle(this, imageAnalysis,preview);
            CameraX.bindToLifecycle(this, imageCapture, imageAnalysis, preview);
        }


        private void init() {

            get_with = findViewById(R.id.get_with); // 横
            get_height = findViewById(R.id.get_height); // 縦

            get_gyouretu = findViewById(R.id.get_gyouretu); // 表示行列

        }


}