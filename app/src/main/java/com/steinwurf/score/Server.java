package com.steinwurf.score;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

class Server
{
    private static final String TAG = Server.class.getSimpleName();

    interface IServerHandler
    {
        void onStarted();
        void onStopped();
        void onConnection(String hostAddress);
    }

    private IServerHandler handler;
    private ServerSocket serverSocket = null;
    private Thread connectionHandler = null;

    void setHandler(IServerHandler handler)
    {
        this.handler = handler;
    }

    void start(final int port) {
        connectionHandler = new Thread(new Runnable()
        {
            @Override
            public void run() {
                if (handler != null)
                    handler.onStarted();
                try {
                    serverSocket = new ServerSocket(port);
                    while (true) {
                        final Socket socket = serverSocket.accept();
                        Log.d(TAG, "Got connection!");
                        if (handler != null)
                            handler.onConnection(socket.getInetAddress().getHostAddress());
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    BufferedWriter out = new BufferedWriter(
                                            new OutputStreamWriter(socket.getOutputStream()));
                                    out.write("Test message");
                                    out.newLine();
                                    socket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (IOException ignored) { }
                if (handler != null)
                    handler.onStopped();
            }
        });

        connectionHandler.start();
    }

    void stop() throws IOException {
        serverSocket.close();
        try {
            connectionHandler.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
