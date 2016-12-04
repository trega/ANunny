package camera_service;


import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import CommonInterface.CommonInterface;

public class SocketServer {
    private static final String TAG = "SOC_SRV";
    public int mServerPort;
    private  Thread mServerThread = null;
    private ServerSocket mServerSocket;
    private CameraService itsCameraService;

    public SocketServer(CameraService service)
    {
        itsCameraService = service;
    }

    public void startServer(int port){
        Log.v(TAG, "Entering startServer()");
        mServerPort = port;
        this.mServerThread = new Thread(new ServerThread());
        this.mServerThread.start();
    }

    public void stopServer(){
        Log.v(TAG, "Entering stopServer");
        try {
            mServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ServerThread implements Runnable {
        public void run() {
            Log.v(TAG, "Entering ServerThread.run()");
            Socket socket = null;
            try {
                mServerSocket = new ServerSocket(mServerPort);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {

                    socket = mServerSocket.accept();
                    Log.d(TAG, "New connection accepted");
                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();
                } catch (IOException e) {
                    Log.e(TAG, "Exception while accepting connection from client");
                    e.printStackTrace();
                }
            }
        }
    }


    class CommunicationThread implements Runnable {
        private Socket clientSocket;
        private BufferedReader input;
        public CommunicationThread(Socket clientSocket) {
            Log.v(TAG, "Entering CommunicationThread");
            this.clientSocket = clientSocket;
            try {
                this.input = new BufferxedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            Log.v(TAG, "Entering CommunicationThread.run()");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String read = input.readLine();
                    Log.i(TAG, "Read: " + read);
                    processMessage(read);
                } catch (IOException e) {
                    e.printStackTrace();
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
