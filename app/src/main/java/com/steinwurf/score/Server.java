package com.steinwurf.score;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

class Server
{
    private static final String TAG = Server.class.getSimpleName();

    interface IServerHandler
    {
        void onStarted();
        void onError(String reason);
        void onStopped();
    }

    private IServerHandler handler;
    private MulticastSocket socket;
    private int port = 0;
    private InetAddress ip = null;

    void setHandler(IServerHandler handler)
    {
        this.handler = handler;
    }

    void start(final String ipString, final String portString) {
        new Thread(new Runnable()
        {
            @Override
            public void run() {
                try {
                    port = Integer.parseInt(portString);
                    ip = InetAddress.getByName(ipString);
                    socket = new MulticastSocket(port);
                    socket.joinGroup(ip);

                    Log.d(TAG, "started");
                    if (handler != null)
                        handler.onStarted();

                } catch (IOException | NumberFormatException e) {
                    e.printStackTrace();
                    Log.d(TAG, "stopped");
                    if (handler != null) {
                        handler.onError(e.toString());
                        handler.onStopped();
                    }
                }
            }
        }).start();
    }

    void sendData(byte[] data) throws IOException {
        if (socket != null)
        {
            DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
            socket.send(packet);
        }
    }

    void stop() {
        Log.d(TAG, "stopped");
        if (handler != null)
            handler.onStopped();
    }
}
