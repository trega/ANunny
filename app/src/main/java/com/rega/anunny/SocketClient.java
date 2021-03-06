package com.rega.anunny;


import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import CommonInterface.CommonInterface;

public class SocketClient {
    private static final String TAG = R.string.main_log_tag +  "_SOC_CLT";
    private int mServerPort = -1;
    private Socket mClientSocket;
    private String mServerIp = "localhost";
    private boolean clientRunning = false;


    public void initialize(int tcpport){
        Log.v(TAG, "Entering : SocketClient.initialize");
        mServerPort = tcpport;
        new Thread(new ClientThread()).start();
    }

    public void sendRequest(String msg){

        Log.v(TAG, "Entering : SocketClient.sendRequest()");
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

    public void stop() {
        Log.v(TAG, "Entering SocketClient.stop()");
        try {
            mClientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ClientThread implements Runnable {
        @Override
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(mServerIp);
                mClientSocket = new Socket(serverAddr, mServerPort);
                clientRunning = true;
                sendRequest(CommonInterface.CaptureModes.CAPTURE_ONCE);
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public boolean isSocketConnected(){
        return (mClientSocket != null) && (mClientSocket.isConnected());
    }
}
