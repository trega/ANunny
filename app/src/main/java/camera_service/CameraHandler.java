package camera_service;


import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.widget.Toast;

import com.rega.anunny.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static android.content.Context.CAMERA_SERVICE;

class CameraHandler {
    private static final String TAG = R.string.main_log_tag +  "_CAM_HND";
    private CameraDevice mCameraDevice;
    private CameraCharacteristics mCameraCharacteristics;
    private ImageReader mImageReader;
    private CameraCaptureSession mCameraCaptureSession;
    private Service mCameraService;
    private Handler mHandler = null;

    CameraHandler(Service cameraService) {
        mCameraService = cameraService;
    }

    void initialize(Handler handler){
        Log.v(TAG, "Entering initialize()");
        mHandler = handler;
        CameraManager manager = (CameraManager) mCameraService.getSystemService(CAMERA_SERVICE);
        try {
            if (!checkCameraPermissions()){
                Log.e(TAG,"Camera permissions not granted, returning");
                return;
            }

            String camera = getCamera(manager);
            if (camera == null)
            {
                Log.e(TAG,"getCamera returned null, exiting");
                System.exit(-2);
            }
            CameraDevice.StateCallback callback = new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    Log.v(TAG,"Entering manager.openCamera.onOpened()");
                    mCameraDevice = camera;
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    Log.e(TAG,"Entering onDisconnected()");
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    Log.e(TAG,"Camera could not be opened");
                }
            };
            Log.d(TAG, "mCameraService = " +  this.mCameraService);
            manager.registerAvailabilityCallback(new CameraManager.AvailabilityCallback() {
                @Override
                public void onCameraUnavailable(@NonNull String cameraId)
                {
                    Log.e(TAG, "entering onCameraUnavailable()");
                    super.onCameraUnavailable(cameraId);
                }
                @Override
                public void onCameraAvailable(@NonNull String cameraId)                {
                    Log.d(TAG, "entering onCameraAvailable()");
                    super.onCameraAvailable(cameraId);
                }
            }, mHandler);
            manager.openCamera(camera, callback, handler);
            Size[] jpegSizes = mCameraCharacteristics.get(CameraCharacteristics.
                    SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            Log.d(TAG,"jpegSizes[0].getWidth()=" + jpegSizes[0].getWidth() +
                    ", jpegSizes[0].getHeight()=" + jpegSizes[0].getHeight());
            mImageReader = ImageReader.newInstance(jpegSizes[0].getWidth(), jpegSizes[0].getHeight(),
                    ImageFormat.JPEG, 1);
            mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Log.v(TAG,"Entering mImageReader.onImageAvailable()");
                    Image img = reader.acquireLatestImage();
                    processImage(img);
                    img.close();
                }
            }, mHandler);
        } catch (CameraAccessException e){
            Log.e(TAG, e.getMessage());
        }
    }

    private boolean checkCameraPermissions() {
        if (ActivityCompat.checkSelfPermission(mCameraService, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Appropriate permission not present");
            return false;
        }
        Log.v(TAG,"Camera permissions ok");
        return true;
    }

    private String getCamera(CameraManager manager){
        Log.v(TAG, "Entering getCamera()");
        try {
            for (String cameraId : manager.getCameraIdList()) {
                mCameraCharacteristics = manager.getCameraCharacteristics(cameraId);
                int cOrientation = mCameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if (cOrientation == CameraCharacteristics.LENS_FACING_BACK) {
                    Log.d(TAG, "Camera found:" + cameraId);
                    return cameraId;
                }
            }
        } catch (CameraAccessException e){
            e.printStackTrace();
        }
        Log.e(TAG, "Camera not found");
        return null;
    }

    boolean takePicture() {
        Log.v(TAG, "Entering takePicture()");
        if(null == mCameraDevice) {
            Log.e(TAG, "cameraDevice is null");
            return true;
        }
        try {
            Log.v(TAG, "About to call createCaptureSession(), mHandler=" + mHandler);
            mCameraDevice.createCaptureSession(Arrays.asList(mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    Log.v(TAG, "Entering CameraCaptureSession.onConfigured()");
                    mCameraCaptureSession = session;
                    try {
                        mCameraCaptureSession.capture(createCaptureRequest(), null, null);
                    } catch (CameraAccessException e){
                        Log.e(TAG, e.getMessage());
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.e(TAG, "Camera capture session: onConfigureFailed()");
                }
            }, mHandler);
        } catch (CameraAccessException e){
            Log.e(TAG, e.getMessage());
        }
        return false;
    }

    private CaptureRequest createCaptureRequest() {
        Log.v(TAG, "Entering createCaptureRequest()");
        try {
            CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            builder.addTarget(mImageReader.getSurface());
            return builder.build();
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    private void processImage(Image image) {
        Log.i(TAG, "New picture taken");
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        OutputStream output;
        try {
            File file;
            if (Environment.isExternalStorageEmulated()) {
                Log.i(TAG, "Writing picture to external storage");
                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ANunnyPicture.jpg");
            } else {
                Log.i(TAG, "Writing picture to root directory");
                file = new File(Environment.getRootDirectory(), "ANunnyPicture.jpg");
            }
            output = new FileOutputStream(file);
            output.write(bytes);
            output.close();
            showToast("Picture saved to:" + file.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showToast(String text) {
        Context context = mCameraService.getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, TAG +text, duration);
        toast.show();
    }
}
