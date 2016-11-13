package camera_service;


import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {
    private static final String TAG = "SOC_SRV";
    public int mServerPort;
    private  Thread mServerThread = null;
    private ServerSocket mServerSocket;

    public void startServer(int port){
        Log.v(TAG, "Inside: " + Thread.currentThread().getStackTrace()[1].getMethodName() +
                ": " + Thread.currentThread().getStackTrace()[2].getLineNumber());
        mServerPort = port;
        this.mServerThread = new Thread(new ServerThread());
        this.mServerThread.start();
    }

    public void stopServer(){
        Log.v(TAG, "Inside: " + Thread.currentThread().getStackTrace()[1].getMethodName() +
                ": " + Thread.currentThread().getStackTrace()[2].getLineNumber());
        try {
            mServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ServerThread implements Runnable {
        public void run() {
            Log.v(TAG, "Inside: " + Thread.currentThread().getStackTrace()[1].getMethodName() +
                    ": " + Thread.currentThread().getStackTrace()[2].getLineNumber());
            Socket socket = null;
            try {
                mServerSocket = new ServerSocket(mServerPort);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    socket = mServerSocket.accept();
                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.v(TAG, "Inside: " + Thread.currentThread().getStackTrace()[1].getMethodName() +
                    ": " + Thread.currentThread().getStackTrace()[2].getLineNumber());
        }
    }

    class CommunicationThread implements Runnable {
        private Socket clientSocket;
        private BufferedReader input;
        public CommunicationThread(Socket clientSocket) {
            Log.v(TAG, "Inside: " + Thread.currentThread().getStackTrace()[1].getMethodName() +
                    ": " + Thread.currentThread().getStackTrace()[2].getLineNumber());
            this.clientSocket = clientSocket;
            try {
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String read = input.readLine();
                    Log.i(TAG, "Read: " + read);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
