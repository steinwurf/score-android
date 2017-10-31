package com.steinwurf.score;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

class Client {

    private static final String TAG = SenderActivity.class.getSimpleName();

    interface IClientHandler
    {
        void onStarted();
        void onStopped();
        void onMessage(String message);
    }

    private IClientHandler handler;

    private Socket clientSocket = null;
    private Thread connectionHandler = null;
    private boolean running = false;

    void setHandler(IClientHandler handler)
    {
        this.handler = handler;
    }

    void start(final InetAddress ip, final int port) {
        connectionHandler = new Thread(new Runnable() {
            @Override
            public void run() {
                if (handler != null)
                    handler.onStarted();
                try {
                    clientSocket = new Socket(ip, port);
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String message = in.readLine();
                    Log.d(TAG, "message: " + message);
                    if (handler != null)
                        handler.onMessage(message);

                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (handler != null)
                    handler.onStopped();
            }
        });
        connectionHandler.start();
    }

    void stop() throws IOException {
        clientSocket.close();
        try {
            connectionHandler.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
