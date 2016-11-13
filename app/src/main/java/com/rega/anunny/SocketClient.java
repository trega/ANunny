package com.rega.anunny;


import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketClient {
    private static final String TAG = "SOC_CLT";
    private int mServerPort = -1;
    private Socket mClientSocket;
    private String mServerIp = "localhost";


    public void initialize(int tcpport){
        Log.v(TAG, "Inside: " + Thread.currentThread().getStackTrace()[1].getMethodName() +
                ": " + Thread.currentThread().getStackTrace()[2].getLineNumber());
        mServerPort = tcpport;
        new Thread(new ClientThread()).start();
    }

    public void sendRequest(String msg){
        Log.v(TAG, "Inside: " + Thread.currentThread().getStackTrace()[1].getMethodName() +
                ": " + Thread.currentThread().getStackTrace()[2].getLineNumber());
        PrintWriter out = null;
        try {
            out = new PrintWriter(
                    new BufferedWriter(
                        new OutputStreamWriter(mClientSocket.getOutputStream())),
                        true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.println(msg);
    }

    class ClientThread implements Runnable {
        @Override
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(mServerIp);
                mClientSocket = new Socket(serverAddr, mServerPort);
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
