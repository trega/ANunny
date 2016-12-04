package camera_service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import CommonInterface.CommonInterface;

public class CameraService extends Service {
    private static final String TAG = "CAM_SVC";
    CameraHandler mCameraHandler;
    SocketServer mSocketServer;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "Entering onCreate()");
        mSocketServer = new SocketServer(this);
        mSocketServer.startServer(CommonInterface.CAMERA_SVC_TCP_PORT);
        mCameraHandler = new CameraHandler(this);
        Log.v(TAG, "Leaving onCreate()");
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "Entering onDestroy()");
        mSocketServer.stopServer();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "Entering onBind()");
        return null;
    }

    private void showToast(String text) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, TAG + text, duration);
        toast.show();
    }

    public String getWifiIpAddress(){
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
    }

    public void captureOnce() {
        Log.v(TAG, "Entering captureOnce()");
        mCameraHandler.initialize();
//        showToast(CommonInterface.CaptureModes.CAPTURE_ONCE);
        Log.i(TAG,"CommonInterface.CaptureModes.CAPTURE_ONCE");
    }
}
