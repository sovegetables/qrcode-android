package cn.bingoogolapple.qrcode.zxingdemo;

import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zbar.ZBarView;

/**
 * @author albert
 */
public class TestZbarSpeedActivity extends AppCompatActivity {

    private static final String TAG = "TestSpeedActivity";

    private ZBarView mZXingView;

    private static long time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_speed_zbar);
        mZXingView = findViewById(R.id.zxingview);
        mZXingView.setDelegate(new QRCodeView.Delegate() {
            @Override
            public void onPreviewCallback() {
                time = System.currentTimeMillis();
            }

            @Override
            public void onScanQRCodeSuccess(String result) {
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
                time = System.currentTimeMillis() - time;
                Toast.makeText(getApplicationContext(), String.valueOf(time), Toast.LENGTH_LONG).show();
                vibrate();
                finish();
            }

            @Override
            public void onCameraAmbientBrightnessChanged(boolean isDark) {
                // 这里是通过修改提示文案来展示环境是否过暗的状态，接入方也可以根据 isDark 的值来实现其他交互效果
                String tipText = mZXingView.getScanBoxView().getTipText();
                String ambientBrightnessTip = "\n环境过暗，请打开闪光灯";
                if (isDark) {
                    if (!tipText.contains(ambientBrightnessTip)) {
                        mZXingView.getScanBoxView().setTipText(tipText + ambientBrightnessTip);
                    }
                } else {
                    if (tipText.contains(ambientBrightnessTip)) {
                        tipText = tipText.substring(0, tipText.indexOf(ambientBrightnessTip));
                        mZXingView.getScanBoxView().setTipText(tipText);
                    }
                }
            }

            @Override
            public void onScanQRCodeOpenCameraError() {
                Log.d(TAG, "onScanQRCodeOpenCameraError: ");
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mZXingView.startCamera(); // 打开后置摄像头开始预览，但是并未开始识别
        mZXingView.startSpotAndShowRect(); // 显示扫描框，并开始识别
    }

    @Override
    protected void onStop() {
        mZXingView.stopCamera(); // 关闭摄像头预览，并且隐藏扫描框
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mZXingView.onDestroy(); // 销毁二维码扫描控件
        super.onDestroy();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }
}