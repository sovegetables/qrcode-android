package cn.bingoogolapple.qrcode.zxing;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

import com.cv4j.core.datamodel.CV4JImage;
import com.cv4j.core.datamodel.Rect;
import com.cv4j.image.util.QRCodeScanner;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import cn.bingoogolapple.qrcode.core.BGAQRCodeUtil;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/4/8 下午11:22
 * 描述:解析二维码图片。一维条码、二维码各种类型简介 https://blog.csdn.net/xdg_blog/article/details/52932707
 */
public class QRCodeDecoder {
    static final Map<DecodeHintType, Object> ALL_HINT_MAP = new EnumMap<>(DecodeHintType.class);

    static {
        List<BarcodeFormat> allFormatList = new ArrayList<>();
        allFormatList.add(BarcodeFormat.AZTEC);
        allFormatList.add(BarcodeFormat.CODABAR);
        allFormatList.add(BarcodeFormat.CODE_39);
        allFormatList.add(BarcodeFormat.CODE_93);
        allFormatList.add(BarcodeFormat.CODE_128);
        allFormatList.add(BarcodeFormat.DATA_MATRIX);
        allFormatList.add(BarcodeFormat.EAN_8);
        allFormatList.add(BarcodeFormat.EAN_13);
        allFormatList.add(BarcodeFormat.ITF);
        allFormatList.add(BarcodeFormat.MAXICODE);
        allFormatList.add(BarcodeFormat.PDF_417);
        allFormatList.add(BarcodeFormat.QR_CODE);
        allFormatList.add(BarcodeFormat.RSS_14);
        allFormatList.add(BarcodeFormat.RSS_EXPANDED);
        allFormatList.add(BarcodeFormat.UPC_A);
        allFormatList.add(BarcodeFormat.UPC_E);
        allFormatList.add(BarcodeFormat.UPC_EAN_EXTENSION);

        // 可能的编码格式
        ALL_HINT_MAP.put(DecodeHintType.POSSIBLE_FORMATS, allFormatList);
        // 花更多的时间用于寻找图上的编码，优化准确性，但不优化速度
        ALL_HINT_MAP.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        // 复杂模式，开启 PURE_BARCODE 模式（带图片 LOGO 的解码方案）
        ALL_HINT_MAP.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
        // 编码字符集
        ALL_HINT_MAP.put(DecodeHintType.CHARACTER_SET, "utf-8");
    }

    static final Map<DecodeHintType, Object> ONE_DIMENSION_HINT_MAP = new EnumMap<>(DecodeHintType.class);

    static {
        List<BarcodeFormat> oneDimenFormatList = new ArrayList<>();
        oneDimenFormatList.add(BarcodeFormat.CODABAR);
        oneDimenFormatList.add(BarcodeFormat.CODE_39);
        oneDimenFormatList.add(BarcodeFormat.CODE_93);
        oneDimenFormatList.add(BarcodeFormat.CODE_128);
        oneDimenFormatList.add(BarcodeFormat.EAN_8);
        oneDimenFormatList.add(BarcodeFormat.EAN_13);
        oneDimenFormatList.add(BarcodeFormat.ITF);
        oneDimenFormatList.add(BarcodeFormat.PDF_417);
        oneDimenFormatList.add(BarcodeFormat.RSS_14);
        oneDimenFormatList.add(BarcodeFormat.RSS_EXPANDED);
        oneDimenFormatList.add(BarcodeFormat.UPC_A);
        oneDimenFormatList.add(BarcodeFormat.UPC_E);
        oneDimenFormatList.add(BarcodeFormat.UPC_EAN_EXTENSION);

        ONE_DIMENSION_HINT_MAP.put(DecodeHintType.POSSIBLE_FORMATS, oneDimenFormatList);
        ONE_DIMENSION_HINT_MAP.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        ONE_DIMENSION_HINT_MAP.put(DecodeHintType.CHARACTER_SET, "utf-8");
    }

    static final Map<DecodeHintType, Object> TWO_DIMENSION_HINT_MAP = new EnumMap<>(DecodeHintType.class);

    static {
        List<BarcodeFormat> twoDimenFormatList = new ArrayList<>();
        twoDimenFormatList.add(BarcodeFormat.AZTEC);
        twoDimenFormatList.add(BarcodeFormat.DATA_MATRIX);
        twoDimenFormatList.add(BarcodeFormat.MAXICODE);
        twoDimenFormatList.add(BarcodeFormat.QR_CODE);

        TWO_DIMENSION_HINT_MAP.put(DecodeHintType.POSSIBLE_FORMATS, twoDimenFormatList);
        TWO_DIMENSION_HINT_MAP.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        TWO_DIMENSION_HINT_MAP.put(DecodeHintType.CHARACTER_SET, "utf-8");
    }

    static final Map<DecodeHintType, Object> QR_CODE_HINT_MAP = new EnumMap<>(DecodeHintType.class);

    static {
        QR_CODE_HINT_MAP.put(DecodeHintType.POSSIBLE_FORMATS, Collections.singletonList(BarcodeFormat.QR_CODE));
        QR_CODE_HINT_MAP.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        QR_CODE_HINT_MAP.put(DecodeHintType.CHARACTER_SET, "utf-8");
    }

    static final Map<DecodeHintType, Object> CODE_128_HINT_MAP = new EnumMap<>(DecodeHintType.class);

    static {
        CODE_128_HINT_MAP.put(DecodeHintType.POSSIBLE_FORMATS, Collections.singletonList(BarcodeFormat.CODE_128));
        CODE_128_HINT_MAP.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        CODE_128_HINT_MAP.put(DecodeHintType.CHARACTER_SET, "utf-8");
    }

    static final Map<DecodeHintType, Object> EAN_13_HINT_MAP = new EnumMap<>(DecodeHintType.class);

    static {
        EAN_13_HINT_MAP.put(DecodeHintType.POSSIBLE_FORMATS, Collections.singletonList(BarcodeFormat.EAN_13));
        EAN_13_HINT_MAP.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        EAN_13_HINT_MAP.put(DecodeHintType.CHARACTER_SET, "utf-8");
    }

    static final Map<DecodeHintType, Object> HIGH_FREQUENCY_HINT_MAP = new EnumMap<>(DecodeHintType.class);

    static {
        List<BarcodeFormat> highFrequencyFormatList = new ArrayList<>();
        highFrequencyFormatList.add(BarcodeFormat.QR_CODE);
        highFrequencyFormatList.add(BarcodeFormat.UPC_A);
        highFrequencyFormatList.add(BarcodeFormat.EAN_13);
        highFrequencyFormatList.add(BarcodeFormat.CODE_128);

        HIGH_FREQUENCY_HINT_MAP.put(DecodeHintType.POSSIBLE_FORMATS, highFrequencyFormatList);
        HIGH_FREQUENCY_HINT_MAP.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        HIGH_FREQUENCY_HINT_MAP.put(DecodeHintType.CHARACTER_SET, "utf-8");
    }

    private QRCodeDecoder() {
    }

    /**
     * 同步解析本地图片二维码。该方法是耗时操作，请在子线程中调用。
     *
     * @param picturePath 要解析的二维码图片本地路径
     * @return 返回二维码图片里的内容 或 null
     */
    public static String syncDecodeQRCode(String picturePath) {
        return syncDecodeQRCode(BGAQRCodeUtil.getDecodeAbleBitmap(picturePath));
    }

    /**
     * 同步解析bitmap二维码。该方法是耗时操作，请在子线程中调用。
     *
     * @param bitmap 要解析的二维码图片
     * @return 返回二维码图片里的内容 或 null
     */
    public static String syncDecodeQRCode(Bitmap bitmap) {
        String textResult = null;
        try {
            MultiFormatReader multiFormatReader = create();
            Result result = multiFormatReader.decodeWithState(new BinaryBitmap(new HybridBinarizer(new BitmapLuminanceSource(bitmap))));
            textResult = result.getText();
        } catch (Exception e) {
            Log.w("syncDecodeQRCode1", e);
            if(bitmap != null){
                try {
                    MultiFormatReader multiFormatReader = create();
                    int bitmapWidth = bitmap.getWidth();
                    int bitmapHeight = bitmap.getHeight();
                    int[] data = new int[bitmapWidth * bitmapHeight];
                    byte[] bitmapPixels = new byte[bitmapWidth * bitmapHeight];
                    bitmap.getPixels(data, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight);
                    for (int i = 0; i < data.length; i++) {
                        bitmapPixels[i] = (byte) data[i];
                    }
                    int width = bitmapWidth;
                    int height = bitmapHeight;
                    byte[] data2 = new byte[bitmapPixels.length];
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            data2[x * height + height - y - 1] = bitmapPixels[x + y * width];
                        }
                    }
                    int tmp = width;
                    width = height;
                    height = tmp;
                    Result result = multiFormatReader.decodeWithState(new BinaryBitmap(new HybridBinarizer(new BitmapLuminanceSource(data2, width, height))));
                    textResult = result.getText();
                } catch (Exception ex) {
                    Log.w("syncDecodeQRCode2", ex);
                    try {
                        CV4JImage cv4JImage = new CV4JImage(bitmap);
                        QRCodeScanner qrCodeScanner = new QRCodeScanner();
                        Rect rect = qrCodeScanner.findQRCodeBounding(cv4JImage.getProcessor(),1,6);
                        int y = Math.max(rect.y - 20, 0);
                        int height = Math.min(bitmap.getHeight() - rect.y + 20, bitmap.getHeight() - y);
                        Bitmap bitmap2 = Bitmap.createBitmap(bitmap, 0, y, bitmap.getWidth(), height);
                        Result result = create().decodeWithState(new BinaryBitmap(new HybridBinarizer(new BitmapLuminanceSource(bitmap2))));
                        textResult = result.getText();
                    } catch (Exception ex2) {
                        Log.w("syncDecodeQRCode3", ex2);
                        ex2.printStackTrace();
                    }
                }
            }
        }
        return textResult;
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

    public static MultiFormatReader create() {
        MultiFormatReader multiFormatReader = new MultiFormatReader();
        // 解码的参数
        Hashtable<DecodeHintType, Object> hints = new Hashtable<>(2);
        // 可以解析的编码类型
        Vector<BarcodeFormat> decodeFormats = new Vector<>();
        decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
        decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
        decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
//        hints.put(DecodeHintType.PURE_BARCODE, true);
        // 设置继续的字符编码格式为UTF8
        // hints.put(DecodeHintType.CHARACTER_SET, "UTF8");
        // 设置解析配置参数
        multiFormatReader.setHints(hints);
        return multiFormatReader;
    }
}