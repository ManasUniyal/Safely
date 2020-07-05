package com.example.miniproject.Activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.miniproject.Camera.CameraErrorCallback;
import com.example.miniproject.Camera.FaceOverlayView;
import com.example.miniproject.NativeClasses.Native;
import com.example.miniproject.R;
import com.example.miniproject.SingletonClasses.JourneyStatus;
import com.example.miniproject.Utilities.CameraUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import kotlin.jvm.internal.Intrinsics;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback   {



    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int PERMISSION_REQUEST_CODE = 200;

    public static final String TAG = MainActivity.class.getSimpleName();

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
    private Camera.Face[] mFaces;

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
    private Camera.FaceDetectionListener faceDetectionListener = new Camera.FaceDetectionListener() {
        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            Log.d("onFaceDetection", "Number of Faces:" + faces.length);
            // Update the view now!
            mFaceView.setFaces(faces);
            Camera.Face bestface = null;
            int score=30;
            for(Camera.Face face: faces)
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
    private BottomNavigationView bottomNavigationView;
    private Button journeyStateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mView = (SurfaceView)findViewById(R.id.surface);

        mFaceView = new FaceOverlayView(MainActivity.this);
        addContentView(mFaceView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mView.setVisibility(View.GONE);

        mOrientationEventListener = new MainActivity.SimpleOrientationEventListener(MainActivity.this);
        mOrientationEventListener.enable();


        // Now create the OverlayView:



        //TODO: Handle back button
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.camera);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.camera:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.maps:
                        startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.logs:
                        startActivity(new Intent(getApplicationContext(), DrivingLogs.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });

        if(checkPermission()) {
            IntiateCamera();
        }else {
            requestPermission();
        }

        journeyStateButton = findViewById(R.id.journeyStateButton);
        JourneyStatus.getInstance(getApplicationContext()).setJourneyStateButton(journeyStateButton, bottomNavigationView,MainActivity.this);
        journeyStateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JourneyStatus.getInstance(MainActivity.this).updateJourneyLog(journeyStateButton, bottomNavigationView, MainActivity.this);
                startActivity(new Intent(getApplicationContext(), MapsActivity.class));
            }
        });

    }

    private void IntiateCamera() {

        //holder.setFixedSize(0,0);

        mView.setVisibility(View.VISIBLE);
        final SurfaceHolder holder = mView.getHolder();
        holder.addCallback(MainActivity.this);
//        mCamera = Camera.open(findFrontFacingCamera());
//        // mCamera.setPreviewCallbackWithBuffer(this);
//        mCamera.setFaceDetectionListener(faceDetectionListener);
//        mCamera.startFaceDetection();
//        try {
//            mCamera.setPreviewDisplay(holder);
//        } catch (Exception e) {
//            Log.e(TAG, "Could not preview the image.", e);
//        }

    }

    @Override
    protected void onPause() {
        mOrientationEventListener.disable();
        mCamera.release();
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
        //TODO: handle this method
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
        mDisplayRotation = CameraUtil.getDisplayRotation(MainActivity.this);
        mDisplayOrientation = CameraUtil.getDisplayOrientation(mDisplayRotation, findFrontFacingCamera());

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
        Camera.Size previewSize = CameraUtil.getOptimalPreviewSize(this, previewSizes, targetRatio);

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
            mOrientation = CameraUtil.roundOrientation(orientation, mOrientation);
            // When the screen is unlocked, display rotation may change. Always
            // calculate the up-to-date orientationCompensation.
            int orientationCompensation = mOrientation
                    + CameraUtil.getDisplayRotation(MainActivity.this);
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
    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false;
        }
        return true;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    IntiateCamera();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            showMessageOKCancel("You need to allow access permissions",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermission();
                                            }
                                        }
                                    });
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

}
