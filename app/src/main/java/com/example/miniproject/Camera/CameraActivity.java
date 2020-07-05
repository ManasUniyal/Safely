package com.example.miniproject.Camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;

import com.example.miniproject.NativeClasses.Native;
import com.example.miniproject.R;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import kotlin.jvm.internal.Intrinsics;




public class CameraActivity extends Activity
        implements SurfaceHolder.Callback  {

    public static final String TAG = CameraActivity.class.getSimpleName();

    private Camera mCamera;
    private Boolean bProcessing=false;
    Handler mHandler = new Handler(Looper.getMainLooper());
    private byte[] frameData= null;
    private Rect rect;
    private int PreviewSizeWidth;
    private int PreviewSizeHeight;

    // We need the phone orientation to correctly draw the overlay:
    private int mOrientation;
    private int mOrientationCompensation;
    private OrientationEventListener mOrientationEventListener;

    // Let's keep track of the display rotation and orientation also:
    private int mDisplayRotation;
    private int mDisplayOrientation;

    // Holds the Face Detection result:
    private Face[] mFaces;

    // The surface view for the camera data
    private SurfaceView mView;

    // Draw rectangles and other fancy stuff:
    private FaceOverlayView mFaceView;

    // Log all errors:
    private final CameraErrorCallback mErrorCallback = new CameraErrorCallback();

    /**
     * Sets the faces for the overlay view, so it can be updated
     * and the face overlays will be drawn again.
     */
    private FaceDetectionListener faceDetectionListener = new FaceDetectionListener() {
        @Override
        public void onFaceDetection(Face[] faces, Camera camera) {
            Log.d("onFaceDetection", "Number of Faces:" + faces.length);
            // Update the view now!
            mFaceView.setFaces(faces);
            Face bestface = null;
            int score=30;
            for(Face face: faces)
            {
                if(face.score>score){
                    score=face.score;
                    bestface=face;

                }
            }

            if(bestface!=null)
            {   //Log.d(">>bestface metrics","bestface"+bestface.rect.top+bestface.rect.bottom+"-"+bestface.rect.left+"-"+bestface.rect.right);
                //Log.d("Display Metrics",""+context.getResources().getDisplayMetrics().widthPixels+""+context.getResources().getDisplayMetrics().heightPixels);
                rect = mapTo(bestface.rect,PreviewSizeWidth,PreviewSizeHeight,mDisplayOrientation);

                if(!bProcessing)
                {
                    mHandler.post(DoImageProcessing);
                }
            }
        }
    };


    private Runnable DoImageProcessing = new Runnable()
    {
        public void run()
        {
            Log.d("TAG", "DoImageProcessing():");
            bProcessing = true;
            long arr[]= Native.analiseFrame(frameData,mDisplayOrientation, PreviewSizeWidth,PreviewSizeHeight,rect);
            Log.d("JNI LandMarks", Arrays.toString(arr)+"-------"+arr.length);
            mFaceView.setLandmarks(arr);
            bProcessing = false;
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_camera);
        // Now create the OverlayView:
        mView = (SurfaceView)findViewById(R.id.surface);
        mFaceView = new FaceOverlayView(this);
        addContentView(mFaceView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        // Create and Start the OrientationListener:
        mOrientationEventListener = new SimpleOrientationEventListener(this);
        mOrientationEventListener.enable();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        SurfaceHolder holder = mView.getHolder();
        //holder.setFixedSize(0,0);
        holder.addCallback(this);
    }

    @Override
    protected void onPause() {
        mOrientationEventListener.disable();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mOrientationEventListener.enable();
        super.onResume();
    }
    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                Log.d("DEBUG_TAG", "Camera found");
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mCamera = Camera.open(findFrontFacingCamera());
       // mCamera.setPreviewCallbackWithBuffer(this);
        mCamera.setFaceDetectionListener(faceDetectionListener);
        mCamera.startFaceDetection();
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
        } catch (Exception e) {
            Log.e(TAG, "Could not preview the image.", e);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        // We have no surface, return immediately:
        if (surfaceHolder.getSurface() == null) {
            return;
        }
        // Try to stop the current preview:
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // Ignore...
        }

        configureCamera(width, height);
        setDisplayOrientation();
        setErrorCallback();
        mCamera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                frameData=data;
                Log.d("Preview Captured",data.toString());
            }

        });

        // Everything is configured! Finally start the camera preview again:
        mCamera.startPreview();
    }

    private void setErrorCallback() {
        mCamera.setErrorCallback(mErrorCallback);
    }

    private void setDisplayOrientation() {
        // Now set the display orientation:
        mDisplayRotation = Util.getDisplayRotation(CameraActivity.this);
        mDisplayOrientation = Util.getDisplayOrientation(mDisplayRotation, findFrontFacingCamera());

        mCamera.setDisplayOrientation(mDisplayOrientation);

        if (mFaceView != null) {
            mFaceView.setDisplayOrientation(mDisplayOrientation);
        }
    }

    private void configureCamera(int width, int height) {
        Camera.Parameters parameters = mCamera.getParameters();
        // Set the PreviewSize and AutoFocus:
        setOptimalPreviewSize(parameters, width, height);
        setAutoFocus(parameters);
        // And set the parameters:
        mCamera.setParameters(parameters);
    }

    private void setOptimalPreviewSize(Camera.Parameters cameraParameters, int width, int height) {
        List<Camera.Size> previewSizes = cameraParameters.getSupportedPreviewSizes();
        float targetRatio = (float) width / height;
        Camera.Size previewSize = Util.getOptimalPreviewSize(this, previewSizes, targetRatio);

        cameraParameters.setPreviewSize(previewSize.width, previewSize.height);
        PreviewSizeWidth = previewSize.width;
        PreviewSizeHeight = previewSize.height;
        Log.d("Preview H & W",PreviewSizeHeight+"-"+PreviewSizeWidth+"");
    }

    private void setAutoFocus(Camera.Parameters cameraParameters) {
        //cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mCamera.setPreviewCallback(null);
        mCamera.setFaceDetectionListener(null);
        mCamera.setErrorCallback(null);
        mCamera.release();
        mCamera = null;
    }


    /**
     * We need to react on OrientationEvents to rotate the screen and
     * update the views.
     */
    private class SimpleOrientationEventListener extends OrientationEventListener {

        public SimpleOrientationEventListener(Context context) {
            super(context, SensorManager.SENSOR_DELAY_NORMAL);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            // We keep the last known orientation. So if the user first orient
            // the camera then point the camera to floor or sky, we still have
            // the correct orientation.
            if (orientation == ORIENTATION_UNKNOWN) return;
            mOrientation = Util.roundOrientation(orientation, mOrientation);
            // When the screen is unlocked, display rotation may change. Always
            // calculate the up-to-date orientationCompensation.
            int orientationCompensation = mOrientation
                    + Util.getDisplayRotation(CameraActivity.this);
            if (mOrientationCompensation != orientationCompensation) {
                mOrientationCompensation = orientationCompensation;
                mFaceView.setOrientation(mOrientationCompensation);
            }
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