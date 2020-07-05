package com.example.miniproject.Camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.view.View;

import com.example.miniproject.R;
import com.example.miniproject.Utilities.CameraUtil;

import org.jetbrains.annotations.NotNull;

import kotlin.jvm.internal.Intrinsics;

public class FaceOverlayView extends View {

    private Paint mPaint;
    private Paint mTextPaint;
    private int mDisplayOrientation;
    private int mOrientation;
    private Camera.Face[] mFaces;
    private long[] landmarks;

    public FaceOverlayView(Context context) {
        super(context);
        initialize();
    }

    private void initialize() {
        // We want a green box around the face:
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setAlpha(128);
        mPaint.setStrokeWidth(5);
        mPaint.setStyle(Paint.Style.STROKE);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setDither(true);
        mTextPaint.setTextSize(20);
        mTextPaint.setColor(Color.GREEN);
        mTextPaint.setStyle(Paint.Style.FILL);
    }

    public void setFaces(Camera.Face[] faces) {
        mFaces = faces;
        invalidate();
    }
    public void setLandmarks(long[] land){
        landmarks=land;
        invalidate();
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    public void setDisplayOrientation(int displayOrientation) {
        mDisplayOrientation = displayOrientation;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(landmarks!=null&&landmarks.length>0){
            Matrix matrix = new Matrix();
            CameraUtil.prepareMatrix(matrix, false, mDisplayOrientation, getWidth(), getHeight());
            canvas.save();

            matrix.postRotate(mOrientation);
            canvas.rotate(-mOrientation);
            RectF rectF = new RectF();
            Paint p = new Paint();
            p.setColor(Color.YELLOW);
            p.setStyle(Paint.Style.FILL);
            for(int i=0;i<landmarks.length;i+=2)
            {
                canvas.drawCircle(landmarks[i], landmarks[i+1], 5f, p);
            }
            canvas.restore();
        }
        if (mFaces != null && mFaces.length > 0) {
            Matrix matrix = new Matrix();
            CameraUtil.prepareMatrix(matrix, false, mDisplayOrientation, getWidth(), getHeight());
            canvas.save();
            matrix.postRotate(mOrientation);
            canvas.rotate(-mOrientation);
            RectF rectF = new RectF();
            for (Camera.Face face : mFaces) {
               // Rect rr = mapTo(face.rect,getWidth(),getHeight(),mOrientation);
                rectF.set(face.rect);
                matrix.mapRect(rectF);
                canvas.drawRect(face.rect, mPaint);
                canvas.drawText("Score " + face.score, rectF.right, rectF.top, mTextPaint);
            }
            canvas.restore();
        }



    }
    public final Rect mapTo(@NotNull Rect $this$mapTo, int width, int height, int rotation) {
        Intrinsics.checkParameterIsNotNull($this$mapTo, "$this$mapTo");

        float hw = (float)$this$mapTo.width() / 2.0F;
        float hh = (float)$this$mapTo.height() / 2.0F;
        float cx = (float)$this$mapTo.left + hw;
        float cy = (float)$this$mapTo.top + hh;
        float side = (hh + hw) / 2.0F;
        float left = cx - side;
        float right = cx + side;
        float top = cy - side;
        float bottom = cy + side;
        float l = (left + (float)1000) / 2000.0F;
        float r = (right + (float)1000) / 2000.0F;
        float t = (top + (float)1000) / 2000.0F;
        float b = (bottom + (float)1000) / 2000.0F;
        int w;
        int h;
        float wr;
        float hr;
        int x0;
        int y0;
        switch(rotation) {
            case 0:
                w = width < height ? height : width;
                h = width < height ? width : height;
                $this$mapTo.left = Math.round((float)w - (float)w * r);
                $this$mapTo.right = Math.round((float)w - (float)w * l);
                $this$mapTo.top = Math.round((float)h * t);
                $this$mapTo.bottom = Math.round((float)h * b);
                wr = (float)$this$mapTo.width() * 0.5F;
                hr = (float)$this$mapTo.height() * 0.5F;
                x0 = $this$mapTo.centerX();
                y0 = $this$mapTo.centerY();
                $this$mapTo.left = (int)((float)x0 - hr);
                $this$mapTo.right = (int)((float)x0 + hr);
                $this$mapTo.top = (int)((float)y0 - wr);
                $this$mapTo.bottom = (int)((float)y0 + wr);
                break;
            case 90:
                w = width > height ? height : width;
                h = width > height ? width : height;
                $this$mapTo.left = Math.round((float)w - (float)w * b);
                $this$mapTo.right = Math.round((float)w - (float)w * t);
                $this$mapTo.top = Math.round((float)h - (float)h * r);
                $this$mapTo.bottom = Math.round((float)h - (float)h * l);
                break;
            case 180:
                w = width < height ? height : width;
                h = width < height ? width : height;
                $this$mapTo.left = Math.round((float)w * l);
                $this$mapTo.right = Math.round((float)w * r);
                $this$mapTo.top = Math.round((float)h - (float)h * b);
                $this$mapTo.bottom = Math.round((float)h - (float)h * t);
                wr = (float)$this$mapTo.width() * 0.5F;
                hr = (float)$this$mapTo.height() * 0.5F;
                x0 = $this$mapTo.centerX();
                y0 = $this$mapTo.centerY();
                $this$mapTo.left = (int)((float)x0 - hr);
                $this$mapTo.right = (int)((float)x0 + hr);
                $this$mapTo.top = (int)((float)y0 - wr);
                $this$mapTo.bottom = (int)((float)y0 + wr);
        }

        return $this$mapTo;
    }
}
