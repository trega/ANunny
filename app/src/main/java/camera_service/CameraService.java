package camera_service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.rega.anunny.SocketClient;

public class CameraService extends Service {
    private static final String TAG = "CAM_SVC";
    public static final int TCPPORT = 8765;
    CameraHandler mCameraHandler;
    SocketServer mSocketServer;
    SocketClient mSocketClient;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "Entering onCreate()");
        mSocketServer = new SocketServer();
        mSocketServer.startServer(TCPPORT);
        mSocketClient = new SocketClient();
        mSocketClient.initialize(TCPPORT);
        mCameraHandler = new CameraHandler(this);
//        mCameraHandler.initialize();
        mSocketClient.sendRequest("Message for CameraService");
        Log.v(TAG, "Leaving onCreate()");
    }

    @Override
    public void onDestroy() {
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
}
