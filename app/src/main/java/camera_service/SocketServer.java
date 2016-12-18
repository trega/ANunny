package camera_service;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.rega.anunny.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import CommonInterface.CommonInterface;

class SocketServer {
    private static final String TAG = R.string.main_log_tag +  "_SOC_SRV";
    private int mServerPort;
    private  Thread mServerThread = null;
    private ServerSocket mServerSocket;
    private CameraService itsCameraService;

    SocketServer(CameraService service)
    {
        itsCameraService = service;
    }

    void startServer(int port){
        Log.v(TAG, "Entering startServer()");
        mServerPort = port;
        this.mServerThread = new Thread(new ServerThread());
        this.mServerThread.start();
    }

    void stopServer(){
        Log.v(TAG, "Entering stopServer");
        try {
            mServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ServerThread implements Runnable {
        public void run() {
            Log.v(TAG, "Entering ServerThread.run()");
            Socket socket;
            try {
                mServerSocket = new ServerSocket(mServerPort);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {

                    socket = mServerSocket.accept();
                    Log.d(TAG, "New connection accepted");
                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String read = input.readLine();
                    Log.i(TAG, "Read: " + read);
                    processMessage(read);
                } catch (IOException e) {
                    Log.e(TAG, "Exception while accepting connection from client");
                    e.printStackTrace();
                    System.exit(-1);
                }
            }

        }
    }

    private void processMessage(String read) {
        Log.v(TAG, "Entering processMessage()");
        switch (read){
            case CommonInterface.CaptureModes.CAPTURE_ONCE:
                itsCameraService.captureOnce();
                break;
            default:
                break;
        }
    }
}
