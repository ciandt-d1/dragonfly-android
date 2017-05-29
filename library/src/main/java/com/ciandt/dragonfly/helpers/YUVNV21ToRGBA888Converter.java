package com.ciandt.dragonfly.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;

/**
 * Created by iluz on 5/27/17.
 */
public class YUVNV21ToRGBA888Converter {

    private BitmapManager bitmapManager;
    RenderScript renderScript;

    public YUVNV21ToRGBA888Converter(Context context) {
        renderScript = RenderScript.create(context.getApplicationContext());
        bitmapManager = new BitmapManager();
    }

    public Bitmap convert(byte[] data, int width, int height, Bitmap.Config config, int rotation) {
        ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(renderScript, Element.U8_4(renderScript));

        Type.Builder yuvType = new Type.Builder(renderScript, Element.U8(renderScript)).setX(data.length);
        Allocation in = Allocation.createTyped(renderScript, yuvType.create(), Allocation.USAGE_SCRIPT);

        Type.Builder rgbaType = new Type.Builder(renderScript, Element.RGBA_8888(renderScript)).setX(width).setY(height);
        Allocation out = Allocation.createTyped(renderScript, rgbaType.create(), Allocation.USAGE_SCRIPT);

        in.copyFrom(data);

        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);

        Bitmap bitmap = bitmapManager.get(width, height, config);
        out.copyTo(bitmap);

        if (rotation != 0) {
            bitmap = rotate(bitmap, rotation);
        }

        return bitmap;
    }

    // TODO: replace with RenderScript alternative.
    private Bitmap rotate(Bitmap bitmap, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
