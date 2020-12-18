package cn.bingoogolapple.qrcode.zxingdemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.cv4j.core.datamodel.CV4JImage;
import com.cv4j.core.datamodel.Rect;
import com.cv4j.image.util.QRCodeScanner;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerActivity;
import cn.bingoogolapple.qrcode.core.BGAQRCodeUtil;

import static cn.bingoogolapple.qrcode.zxing.QRCodeDecoder.create;

public class TestActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY = 666;
    private ImageView iv_bitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        iv_bitmap = findViewById(R.id.iv_photo);
    }

    public void onClick(View v) {
        /*
                从相册选取二维码图片，这里为了方便演示，使用的是
                https://github.com/bingoogolapple/BGAPhotoPicker-Android
                这个库来从图库中选择二维码图片，这个库不是必须的，你也可以通过自己的方式从图库中选择图片
                 */
        Intent photoPickerIntent = new BGAPhotoPickerActivity.IntentBuilder(this)
                .cameraFileDir(null)
                .maxChooseCount(1)
                .selectedPhotos(null)
                .pauseOnScroll(false)
                .build();
        startActivityForResult(photoPickerIntent, REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY);
    }

    private void test_(Bitmap bitmap){
        CV4JImage cv4JImage = new CV4JImage(bitmap);
        QRCodeScanner qrCodeScanner = new QRCodeScanner();
        Rect rect = qrCodeScanner.findQRCodeBounding(cv4JImage.getProcessor(),1,6);
        Bitmap bm = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bm);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth((float) 10);
        paint.setStyle(Paint.Style.STROKE);
//        Bitmap bitmap2 = Bitmap.createBitmap(bitmap, rect.x - 20, rect.y - 20, rect.width + 45, rect.height + 45);
        Bitmap bitmap2 = null;
        try {
            bitmap2 = Bitmap.createBitmap(bitmap, 0, rect.y / 2, bm.getWidth(), bm.getHeight()- rect.y / 2);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        android.graphics.Rect androidRect = new android.graphics.Rect(rect.x-20,rect.y-20,rect.br().x+20,rect.br().y+20);
//        canvas.drawRect(androidRect,paint);
        iv_bitmap.setImageBitmap(bitmap2);
    }

    private static Bitmap zoomImg(Bitmap bm, int newWidth){
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        int newHeight = height * newWidth / width;
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY) {
            final String picturePath = BGAPhotoPickerActivity.getSelectedPhotos(data).get(0);
            // 本来就用到 QRCodeView 时可直接调 QRCodeView 的方法，走通用的回调
//            new ZXingView(this).decodeQRCode(picturePath);

            AsyncTask<Void, Void, Bitmap> asyncTask2 = new AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... params) {
                    Bitmap ableBitmap = BGAQRCodeUtil.getDecodeAbleBitmap(picturePath);
                    return zoomImg(ableBitmap, 800);
                }

                @Override
                protected void onPostExecute(Bitmap result) {
//                    iv_bitmap.setImageBitmap(result);
                    test_(result);
                }
            }.execute();


            AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(picturePath, options);
                    String textResult = null;
                    InputStream stream = null;
                    ByteArrayOutputStream outputStream = null;
                    try {
                        options.inJustDecodeBounds = false;
                        Bitmap bitmap = BitmapFactory.decodeFile(picturePath, options);
                        int[] data = new int[bitmap.getWidth() * bitmap.getHeight()];
                        bitmap.getPixels(data, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());


//                        stream = new FileInputStream(picturePath);
//                        outputStream = new ByteArrayOutputStream();
//                        byte[] b = new byte[4048];
//                        int len = 0;
//                        while (len != -1){
//                            len = stream.read(b);
//                            if(len != -1){
//                                outputStream.write(b, 0, len);
//                            }
//                        }
//                        outputStream.flush();
                        MultiFormatReader multiFormatReader = create();
//                        byte[] bytes = outputStream.toByteArray();
//                        int[] ps = new int[bytes.length];
//                        for (int i = 0, le = ps.length; i < le; i++){
//                            ps[i] = bytes[i];
//                        }

                        RGBLuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), data);
//                        Result result = multiFormatReader.decodeWithState(new BinaryBitmap(new HybridBinarizer(new BitmapLuminanceSource(outputStream.toByteArray(), options.outWidth, options.outHeight))));
                        Result result = multiFormatReader.decodeWithState(new BinaryBitmap(new HybridBinarizer(source)));
                        textResult = result.getText();
                    } catch (Exception e) {
                        Log.e("BitmapFactory", "Unable to decode stream: " + e);
                    } finally {
                        if (stream != null) {
                            try {
                                stream.close();
                            } catch (IOException e) {
                                // do nothing here
                            }
                        }
                    }

                    return textResult;
                }

                @Override
                protected void onPostExecute(String result) {
                    if (TextUtils.isEmpty(result)) {
                        Toast.makeText(TestActivity.this, "未发现二维码", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(TestActivity.this, result, Toast.LENGTH_SHORT).show();
                    }
                }
            }.execute();

        }
    }

}