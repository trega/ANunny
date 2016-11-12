package camera_service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class CameraService extends Service {
    private static final String TAG = "CAM_SVC";
    private int CAMERA_CRIENTATION = CameraCharacteristics.LENS_FACING_BACK;
    private CameraDevice mCameraDevice;
    private CameraCharacteristics mCameraCharacteristics;
    private ImageReader mImageReader;
    private CameraCaptureSession mCameraCaptureSession;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "Entering onCreate()");
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            if (!checkCameraPermissions())
                return;
            manager.openCamera(getCamera(manager), new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice camera) {
                    Log.v(TAG,"Entering manager.openCamera.onOpened()");
                    mCameraDevice = camera;
                    takePicture();
                }

                @Override
                public void onDisconnected(CameraDevice camera) {
                    Log.v(TAG,"Entering onDisconnected()");
                }

                @Override
                public void onError(CameraDevice camera, int error) {
                    Log.e(TAG,"Camera could not be opened");
                }
            }, null);
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
            }, null);
        } catch (CameraAccessException e){
            Log.e(TAG, e.getMessage());
        }
        Log.v(TAG, "Leaving onCreate()");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "Entering onBind()");
        return null;
    }

    private boolean takePicture() {
        Log.v(TAG, "Entering takePicture()");
        if(null == mCameraDevice) {
            Log.e(TAG, "cameraDevice is null");
            return true;
        }
        try {
            mCameraDevice.createCaptureSession(Arrays.asList(mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    Log.v(TAG, "Entering CameraCaptureSession.onConfigured()");
                    mCameraCaptureSession = session;
                    try {
                        mCameraCaptureSession.capture(createCaptureRequest(), null, null);
                    } catch (CameraAccessException e){
                        Log.e(TAG, e.getMessage());
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Log.e(TAG, "Camera capture session: onConfigureFailed()");
                }
            }, null);
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
        OutputStream output = null;
        try {
            File file;
            if(Environment.isExternalStorageEmulated()) {
                Log.i(TAG, "Writing picture to external storage");
                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ANunnyPicture.jpg");
            }else{
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
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
    private boolean checkCameraPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Appropriate permission not present");
            return false;
        }
        Log.v(TAG,"Camera permissions ok");
        return true;
    }

    private String getCamera(CameraManager manager){
        try {
            for (String cameraId : manager.getCameraIdList()) {
                mCameraCharacteristics = manager.getCameraCharacteristics(cameraId);
                int cOrientation = mCameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if (cOrientation == CAMERA_CRIENTATION) {
                    return cameraId;
                }
            }
        } catch (CameraAccessException e){
            e.printStackTrace();
        }
        Log.e(TAG, "Camera not found");
        return null;
    }
}
