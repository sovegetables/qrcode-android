package cn.bingoogolapple.qrcode.core;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class QRCodeParsingTask {

    private final static QRCodeParsingTask qrCodeParsingTask = new QRCodeParsingTask();
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE_SECONDS = 30;

    private static final String TAG = "QRCodeParsingTask";

    private static final BlockingQueue<Runnable> sPoolWorkQueue =
            new LinkedBlockingQueue<Runnable>(50);

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "QRCodeParsingTask #" + mCount.getAndIncrement());
        }
    };

    private static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
            sPoolWorkQueue, sThreadFactory, new ThreadPoolExecutor.DiscardPolicy(){
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            BGAQRCodeUtil.d("rejectedExecution");
        }
    });

    public static QRCodeParsingTask getInstance() {
        return qrCodeParsingTask;
    }

    private final List<InnerTask> innerTasks = new ArrayList<>();
    private final OnParsingListener mOnParsingListener = new OnParsingListener() {
        @Override
        public void onSuccess() {
            cancel();
            innerTasks.clear();
        }
    };

    private interface OnParsingListener{
        void onSuccess();
    }

    private static class InnerTask implements Runnable{
        private String id;
        private Camera mCamera;
        private byte[] mData;
        private boolean mIsPortrait;
        private String mPicturePath;
        private Bitmap mBitmap;
        private WeakReference<QRCodeView> mQRCodeViewRef;
        private static long sLastStartTime = 0;
        private final Handler uiHandler = new Handler(Looper.getMainLooper());
        private OnParsingListener onParsingListener;

        public void setOnParsingListener(OnParsingListener onParsingListener) {
            this.onParsingListener = onParsingListener;
        }

        public InnerTask(Camera camera, byte[] data, QRCodeView qrCodeView, boolean portrait) {
            this.mCamera = camera;
            this.mData = data;
            mQRCodeViewRef = new WeakReference<>(qrCodeView);
            this.mIsPortrait = portrait;
            this.id = UUID.randomUUID().toString();
        }

        private boolean started;
        private boolean finished;
        private boolean canceled;
        private boolean successful;

        private ScanResult processData(QRCodeView qrCodeView) {
            if (mData == null) {
                return null;
            }

            int width = 0;
            int height = 0;
            byte[] data = mData;
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                Camera.Size size = parameters.getPreviewSize();
                width = size.width;
                height = size.height;

                if (mIsPortrait) {
                    data = new byte[mData.length];
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            data[x * height + height - y - 1] = mData[x + y * width];
                        }
                    }
                    int tmp = width;
                    width = height;
                    height = tmp;
                }

                return qrCodeView.processData(data, width, height, false);
            } catch (Exception e1) {
                e1.printStackTrace();
                try {
                    if (width != 0 && height != 0) {
                        BGAQRCodeUtil.d("识别失败重试");
                        return qrCodeView.processData(data, width, height, true);
                    } else {
                        return null;
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                    return null;
                }
            }
        }

        protected ScanResult doInBackground() {
            QRCodeView qrCodeView = mQRCodeViewRef.get();
            if (qrCodeView == null) {
                return null;
            }
            if (mPicturePath != null) {
                return qrCodeView.processBitmapData(BGAQRCodeUtil.getDecodeAbleBitmap(mPicturePath));
            } else if (mBitmap != null) {
                ScanResult result = qrCodeView.processBitmapData(mBitmap);
                mBitmap = null;
                return result;
            } else {
                if (BGAQRCodeUtil.isDebug()) {
                    BGAQRCodeUtil.d("两次任务执行的时间间隔：" + (System.currentTimeMillis() - sLastStartTime));
                    sLastStartTime = System.currentTimeMillis();
                }
                long startTime = System.currentTimeMillis();

                ScanResult scanResult = processData(qrCodeView);

                if (BGAQRCodeUtil.isDebug()) {
                    long time = System.currentTimeMillis() - startTime;
                    if (scanResult != null && !TextUtils.isEmpty(scanResult.result)) {
                        BGAQRCodeUtil.d("识别成功时间为：" + time);
                    } else {
                        BGAQRCodeUtil.e("识别失败时间为：" + time);
                    }
                }
                return scanResult;
            }
        }

        private void onPostExecute(ScanResult result) {
            QRCodeView qrCodeView = mQRCodeViewRef.get();
            if (qrCodeView == null) {
                return;
            }
            if (mPicturePath != null || mBitmap != null) {
                mBitmap = null;
                qrCodeView.onPostParseBitmapOrPicture(result);
            } else {
                qrCodeView.onPostParseData(result);
            }
        }

        @Override
        public void run() {
            started = true;
            if(!canceled){
                Log.d(TAG, "run: ");
                ScanResult scanResult = null;
                try {
                    scanResult = doInBackground();
                } catch (Exception e) {
                    BGAQRCodeUtil.e("识别失败!");
                }
                final ScanResult localScanResult = scanResult;
                if(!canceled){
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(!canceled){
                                successful = true;
                                onPostExecute(localScanResult);
                                if(onParsingListener != null){
                                    onParsingListener.onSuccess();
                                }
                            }
                        }
                    });
                }
            }
            finished = true;
        }

        public void cancel(){
            canceled = true;
        }

        public boolean isSuccessful() {
            return successful;
        }

        public boolean isRunning(){
            return started && !finished;
        }
    }

    public boolean isRunning(){
        for (InnerTask task: innerTasks){
            if(task.isRunning()){
                return true;
            }
        }
        return false;
    }

    public void cancel(){
        for (InnerTask task: innerTasks){
            task.cancel();
        }
    }

    public void pendTask(Camera camera, byte[] data, QRCodeView qrCodeView, boolean portrait) {
        InnerTask innerTask = new InnerTask(camera, data, qrCodeView, portrait);
        innerTask.setOnParsingListener(mOnParsingListener);
        innerTasks.add(innerTask);
        threadPoolExecutor.submit(innerTask);
    }
}
