package com.example.miniproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button captureButton;
    private TextureView textureView;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray(4);
    static {
        ORIENTATIONS.append(Surface.ROTATION_0,90);
        ORIENTATIONS.append(Surface.ROTATION_90,0);
        ORIENTATIONS.append(Surface.ROTATION_180,270);
        ORIENTATIONS.append(Surface.ROTATION_270,180);
    }

    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private String cameraID;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private Size imageDimension;
    private CaptureRequest.Builder captureRequestBuilder;
    private ImageReader imageReader;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = findViewById(R.id.textureView);
        captureButton = findViewById(R.id.captureButton);

        textureView.setSurfaceTextureListener(textureListener);

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //take picture
                Log.d("Button pressed", "Image to be captured");
            }
        });
    }

    private void getCameraPreview(){
        if(cameraDevice == null)
            return;
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if(cameraCharacteristics != null){
                jpegSizes = cameraCharacteristics.get(cameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            int width = 640;
            int height = 480;
            if(jpegSizes != null && jpegSizes.length > 0){
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }

            ImageReader imageReader = ImageReader.newInstance(width,height,ImageFormat.JPEG,1);
            List<Surface> outputSurface = new ArrayList<>(2);
            outputSurface.add(imageReader.getSurface());
            outputSurface.add(new Surface(textureView.getSurfaceTexture()));

            //Camera set only for preview
            final CaptureRequest.Builder captureBuilder =  cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureBuilder.addTarget(imageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            //Check orientation based on device
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            ImageReader.OnImageAvailableListener readListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    image = reader.acquireLatestImage();
                    Log.i("Image", "available for preview in image reader");
                    if(image != null)
                        image.close();
                }
            };

            imageReader.setOnImageAvailableListener(readListener, mBackgroundHandler);


            //TODO: This is where the code for facial detection is to be implemented

            final CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
                    super.onCaptureProgressed(session, request, partialResult);
                    createCameraPreview();
                }

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                }
            };

            cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        cameraCaptureSession.capture(captureBuilder.build(),captureCallback,mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            },mBackgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreview() {
        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
        if (surfaceTexture != null){
            surfaceTexture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(surfaceTexture);
            try {
                captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                captureRequestBuilder.addTarget(surface);
                cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        if(cameraDevice == null)
                            return;
                        cameraCaptureSession = session;
                        updatePreview();
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        Toast.makeText(MainActivity.this,"Error",Toast.LENGTH_SHORT).show();
                    }
                },null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void updatePreview() {
        if(cameraDevice == null){
            Toast.makeText(this,"Error",Toast.LENGTH_SHORT).show();
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(),null,mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        CameraManager cameraManager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraID = cameraManager.getCameraIdList()[0]; // Get the front facing camera
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraID);
            StreamConfigurationMap streamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if(streamConfigurationMap != null){
                imageDimension = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }
                }
                cameraManager.openCamera(cameraID,stateCallback,null);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CAMERA_PERMISSION){
            if((grantResults != null) && (grantResults.length > 0) && (grantResults[0] != PackageManager.PERMISSION_GRANTED)){
                Toast.makeText(this,"Provide permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    @Override
    protected void onPostResume() {
        super.onPostResume();
        startBackgroundThread();
        if(textureView.isAvailable()){
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
}
